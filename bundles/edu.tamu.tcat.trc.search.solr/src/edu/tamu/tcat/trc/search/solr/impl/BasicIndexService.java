package edu.tamu.tcat.trc.search.solr.impl;

import static java.text.MessageFormat.format;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrInputDocument;

import edu.tamu.tcat.osgi.config.ConfigurationProperties;
import edu.tamu.tcat.trc.search.solr.IndexService;

public class BasicIndexService<T> implements IndexService<T>
{
   private final static Logger logger = Logger.getLogger(BasicIndexService.class.getName());

   /** Configuration property key that defines the URI for the Solr server. */
   public static final String SOLR_API_ENDPOINT = "trc.search.solr.url";
   public static final String SOLR_ENABLED = "trc.search.solr.enabled";
   public static final String SOLR_USERNAME = "trc.search.solr.username";
   public static final String SOLR_PASSWORD = "trc.search.solr.password";

   public static final String SOLR_CORE_ID = "trc.search.solr.cores.{0}.id";
   public static final String SOLR_CORE_ENABLED = "trc.search.solr.cores.{0}.enabled";

   private final HttpSolrClient solr;
   private final Function<T, SolrInputDocument> adapter;
   private final Function<T, String> idProvider;

   private final AtomicBoolean enabled = new AtomicBoolean(false);

   // used for debug purposes
   private URI solrBaseUri;
   private String core;

   public BasicIndexService(HttpSolrClient client,
                            Function<T, SolrInputDocument> adapter,
                            Function<T, String> idProvider)
   {
      this.solr = client;
      this.adapter = adapter;
      this.idProvider = idProvider;

      if (this.solr == null)
         enabled.set(false);
   }

   @Override
   public void shutdown(long time, TimeUnit units)
   {
      // TODO enforce timeout
      if (!enabled.compareAndSet(false, true))
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
   public SolrClient getSolrClient()
   {
      return solr;
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

   @Override
   public void remove(String... ids)
   {
      if (enabled.get())
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
      if (enabled.get())
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
      if (enabled.get())
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

   /**
    * The core key will be used to lookup configuration properties.
    * General properties will be defined as:
    *
    * <ul>
    *   <li>trc.search.solr.url</li>
    *   <li>trc.search.solr.enabled{=true}</li>
    *   <li>trc.search.solr.username</li>
    *   <li>trc.search.solr.password</li>
    * </ul>
    *
    * @param key
    * @return
    */
   public static class Builder<Entry>
   {
      private final ConfigurationProperties config;

      private Function<Entry, SolrInputDocument> adapter;
      private Function<Entry, String> idProvider;

      private String coreId;

      /**
       * The core key will be used to lookup configuration properties. Properties will
       * be defined as:
       *
       * <ul>
       *   <li>trc.search.solr.core.{key}.id</li>
       *   <li>trc.search.solr.core.{key}.enabled{=true}</li>
       * </ul>
       *
       * @param key
       * @return
       */
      public Builder(ConfigurationProperties config, String coreId)
      {
         this.config = Objects.requireNonNull(config);
         this.coreId = Objects.requireNonNull(coreId);
      }

      public Builder<Entry> setDataAdapter(Function<Entry, SolrInputDocument> adapter)
      {
         this.adapter = adapter;
         return this;
      }

      public Builder<Entry> setIdProvider(Function<Entry, String> idProvider)
      {
         this.idProvider = idProvider;
         return this;
      }

      public BasicIndexService<Entry> build()
      {
         Objects.requireNonNull(adapter, format("No data adapter supplied for core {0}", coreId));
         Objects.requireNonNull(idProvider, format("No id provider supplied for core {0}", coreId));

         URI solrBaseUri = config.getPropertyValue(SOLR_API_ENDPOINT, URI.class);
         String core = config.getPropertyValue(format(SOLR_CORE_ID, coreId), String.class);
         boolean enabled = config.getPropertyValue(format(SOLR_CORE_ENABLED, coreId), Boolean.class, true);

         URI coreUri = solrBaseUri.resolve(core);

         HttpSolrClient client = enabled ? new HttpSolrClient(coreUri.toString()) : null;
         BasicIndexService<Entry> service = new BasicIndexService<>(client, adapter, idProvider);

         service.solrBaseUri = solrBaseUri;
         service.core = core;

         return service;
      }
   }
}
