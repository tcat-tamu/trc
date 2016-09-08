package edu.tamu.tcat.trc.search.solr.impl;

import static java.text.MessageFormat.format;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrInputDocument;

import edu.tamu.tcat.trc.search.solr.IndexService;

public class BasicIndexService<T> implements IndexService<T>
{
   private final static Logger logger = Logger.getLogger(BasicIndexService.class.getName());

   private final URI solrBaseUri;
   private final String core;
   private final Function<T, SolrInputDocument> adapter;
   private final Function<T, String> idProvider;

   private final HttpSolrClient solr;
   private final AtomicBoolean stopping = new AtomicBoolean(false);


   public BasicIndexService(URI solrBaseUri,
                            String core,
                            Function<T, SolrInputDocument> adapter,
                            Function<T, String> idProvider)
   {
      // TODO should provide a factory that supports basic configuration and generates index managers per-core
      this.solrBaseUri = solrBaseUri;
      this.core = core;

      this.adapter = adapter;
      this.idProvider = idProvider;

      URI coreUri = solrBaseUri.resolve(core);
      solr = new HttpSolrClient(coreUri.toString());
   }

   public void close()
   {
      if (!stopping.compareAndSet(false, true))
         return;        // TODO otherwise, block until stopped?

      try
      {
         solr.close();
      }
      catch (Exception e)
      {
         logger.log(Level.SEVERE, "Problems shutting down index service", e);
      }
   }


   @Override
   public boolean isIndexed(T instance)
   {
      return isIndexed(idProvider.apply(instance));
   }

   @Override
   public void index(T instance)
   {
      this.index(adapter.apply(instance));
   }

   @Override
   public void remove(T instance)
   {
      this.remove(idProvider.apply(instance));
   }

   public void remove(String... ids)
   {
      if (stopping.get())
         throw new IllegalStateException("The index service has been stopped.");
   
      addIndexTask(() -> {
         try {
            solr.deleteById(Arrays.asList(ids));
         } catch (Exception e) {
            String template = "Failed to delete document [{0}] from Solr core {1} [host: {2}].";
            String msg = format(template, String.join(", ", ids), core, solrBaseUri);
            logger.log(Level.SEVERE, msg, e);
         }
      });
   }

   private boolean isIndexed(String id)
   {
      if (stopping.get())
         throw new IllegalStateException("The index service has been stopped.");

      SolrQuery query = new SolrQuery();
      query.setQuery("id:" + id);
      try
      {
         QueryResponse response = solr.query(query);
         return  !response.getResults().isEmpty();
      }
      catch (IOException | SolrServerException e)
      {
         logger.log(Level.SEVERE, "Failed to query the work id: [" + id + "] from the SOLR server. " + e);
         return false;
      }
   }

   private void index(SolrInputDocument... docs)
   {
      if (stopping.get())
         throw new IllegalStateException("The index service has been stopped.");

      addIndexTask(() -> {
         try
         {
            solr.add(Arrays.asList(docs));
         }
         catch (SolrServerException | IOException ex)
         {
            List<String> ids = Arrays.stream(docs).map(doc -> {
               return (String)doc.getField("id").getValue();
            }).collect(Collectors.toList());

            String template = "Failed to update document(s) [{0}] from Solr core {1} [host: {2}].";
            String msg = format(template, String.join(", ", ids), core, solrBaseUri);
            logger.log(Level.SEVERE, msg, ex);
         }
      });
   }

   private void addIndexTask(Runnable task)
   {
      // TODO HACK execute immediately, commit every so often
      task.run();
      try {
         solr.commit();
      } catch (Exception ex) {
         logger.log(Level.SEVERE, "Failed to commit changes to Solr", ex);
      }
   }
}
