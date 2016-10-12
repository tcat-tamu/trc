package edu.tamu.tcat.trc.impl.psql.entries;

import static java.text.MessageFormat.format;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.StreamSupport;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;

import edu.tamu.tcat.trc.TrcException;
import edu.tamu.tcat.trc.entries.core.repo.EntryRepository;
import edu.tamu.tcat.trc.entries.core.repo.EntryUpdateRecord;
import edu.tamu.tcat.trc.search.solr.IndexService;
import edu.tamu.tcat.trc.search.solr.IndexServiceStrategy;

public class SolrSearchSupport<EntryType>
{
   private final static Logger logger = Logger.getLogger(SolrSearchSupport.class.getName());

   private final IndexService<EntryType> indexSvc;
   private IndexServiceStrategy<EntryType, ?> strategy;

   public SolrSearchSupport(IndexService<EntryType> indexSvc, IndexServiceStrategy<EntryType, ?> strategy)
   {
      this.indexSvc = indexSvc;
      this.strategy = strategy;

      try
      {
         Class.forName("org.apache.solr.client.solrj.SolrClient");
      }
      catch (Exception ex)
      {
         throw new TrcException("Solr-based search has not been configured", ex);
      }
   }

   public void handleUpdate(EntryUpdateRecord<EntryType> ctx)
   {
      switch(ctx.getAction())
      {
         case CREATE:
            indexSvc.index(ctx.getModifiedState());
            break;
         case UPDATE:
            // TODO perform update
            indexSvc.index(ctx.getModifiedState());
            break;
         case REMOVE:
            indexSvc.remove(ctx.getEntryReference().id);
            break;
      }
   }

   public void reIndex(EntryRepository<EntryType> repo)
   {
      SolrClient solr = indexSvc.getSolrClient();
      Iterable<EntryType> entries = () -> repo.listAll();

      try
      {
         logger.log(Level.INFO, format("Reindexing all documents for " + repo.getClass().getSimpleName()));

         solr.deleteByQuery("*:*");
         StreamSupport.stream(entries.spliterator(), false)
            .map(strategy::getDocument)
            .forEach(this::index);

         solr.commit();
         logger.log(Level.INFO, format("Finished reindexing all documents for " + repo.getClass().getSimpleName()));
      }
      catch (SolrServerException | IOException ex)
      {
         logger.log(Level.SEVERE, "Failed to commit index changes", ex);
      }
   }

   private void index(SolrInputDocument doc)
   {
      SolrClient solr = indexSvc.getSolrClient();
      try
      {
         // TODO return some handle so that we can aggregate result - probably response
         solr.add(doc);
      }
      catch (Exception ex)
      {
         logger.log(Level.SEVERE, format("Failed to index document {0}", doc.getFieldValue("id")), ex);
      }
   }
}
