package edu.tamu.tcat.trc.entries.bib.search.solr;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.tamu.tcat.osgi.config.ConfigurationProperties;
import edu.tamu.tcat.trc.entries.bib.Edition;
import edu.tamu.tcat.trc.entries.bib.Volume;
import edu.tamu.tcat.trc.entries.bib.Work;
import edu.tamu.tcat.trc.entries.bib.WorkNotAvailableException;
import edu.tamu.tcat.trc.entries.bib.WorkRepository;
import edu.tamu.tcat.trc.entries.bib.WorksChangeEvent;
import edu.tamu.tcat.trc.entries.bib.WorksChangeEvent.ChangeType;
import edu.tamu.tcat.trc.entries.bib.search.WorkQueryCommand;
import edu.tamu.tcat.trc.entries.bib.search.WorkSearchService;

/**
 * Provides a service to support SOLR backed searching over bibliographic entries.
 *
 */
public class BiblioEntriesSearchService implements WorkSearchService
{
   private final static Logger logger = Logger.getLogger(BiblioEntriesSearchService.class.getName());

   /** Configuration property key that defines the URI for the Solr server. */
   public static final String SOLR_API_ENDPOINT = "solr.api.endpoint";

   /** Configuration property key that defines Solr core to be used for relationships. */
   public static final String SOLR_CORE = "catalogentries.works.solr.core";      // TODO rename to trc.biblio.works.solr.core

   private WorkRepository repo;
   private ConfigurationProperties config;
   private SolrServer solr;
   private AutoCloseable registration;
   
   /** configured here for use by other classes in this package - these classes are effectively
    * delegates of this service's responsibilities. This is a method to create a new one and not
    * a field to force thread-safety, deduplication, and other concerns on the caller.
    */
   /*package*/ static ObjectMapper getMapper()
   {
      ObjectMapper mapper = new ObjectMapper();
      mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
      return mapper;
   }

   /**
    * @param repo The repository that is used to manage persistence of bibliographic entries.
    */
   public void setWorksRepo(WorkRepository repo)
   {
      this.repo = repo;
   }

   /**
    * @param cp configuration properties. These are required at initialization.
    */
   public void setConfiguration(ConfigurationProperties cp)
   {
      // TODO allow the config props to come and go. If reset, may need to restart this service.
      this.config = cp;
   }

   public void activate()
   {
      logger.fine("Activating SolrRelationshipSearchService");
      //TODO: the check is helpful, but if required it should be statically bound in the DS configuration
      Objects.requireNonNull(repo, "No work repository supplied.");

      // configure handlers
      EntryChangeHandlers<WorksChangeEvent> updateHandlers = new EntryChangeHandlers<WorksChangeEvent>();
      updateHandlers.register(WorksChangeEvent.ChangeType.CREATED, this::onCreate);
      updateHandlers.register(WorksChangeEvent.ChangeType.MODIFIED, this::onWorkUpdate);
      updateHandlers.register(WorksChangeEvent.ChangeType.DELETED, this::onDelete);
      registration = repo.addAfterUpdateListener(updateHandlers::handle);

      // construct Solr core
      URI solrBaseUri = config.getPropertyValue(SOLR_API_ENDPOINT, URI.class);
      String solrCore = config.getPropertyValue(SOLR_CORE, String.class);

      URI coreUri = solrBaseUri.resolve(solrCore);
      logger.info("Connecting to Solr Service [" + coreUri + "]");

      solr = new HttpSolrServer(coreUri.toString());


   }

   public void deactivate()
   {
      logger.info("Deactivating BiblioEntriesSearchService");

      unregisterRepoListener();
      releaseSolrConnection();
   }

   @Override
   public WorkQueryCommand createQueryCommand()
   {
      return new WorkSolrQueryCommand(solr);
   }

   public boolean isIndexed(String id)
   {
      Objects.requireNonNull(solr, "The connection to Solr server is not available.");
   
      SolrQuery query = new SolrQuery();
      query.setQuery("id:" + id);
      try
      {
         QueryResponse response = solr.query(query);
         return  !response.getResults().isEmpty();
      }
      catch (SolrServerException e)
      {
         logger.log(Level.SEVERE, "Failed to query the work id: [" + id + "] from the SOLR server. " + e);
         return false;
      }
   }

   public void removeWork(String id)
   {
      List<String> deleteIds = new ArrayList<>();
      deleteIds.add(id);
   
      SolrQuery query = new SolrQuery();
      query.setQuery("id:" + id + "\\:*");
      try
      {
         QueryResponse response = solr.query(query);
         SolrDocumentList results = response.getResults();
         for(SolrDocument doc : results)
         {
            deleteIds.add(doc.getFieldValue("id").toString());
         }
         solr.deleteById(deleteIds);
         solr.commit();
      }
      catch (SolrServerException | IOException e)
      {
         logger.log(Level.SEVERE, "Failed to delete the work id: [" + id + "] from the the SOLR server. " + e);
      }
   
   }

   private void unregisterRepoListener()
   {
      if (registration == null)
         return;

      try
      {
         registration.close();
      }
      catch (Exception e)
      {
         logger.log(Level.WARNING, "Failed to unregister update listener on works repository.", e);
      }
      finally {
         registration = null;
      }
   }

   private void releaseSolrConnection()
   {
      logger.fine("Releasing connection to Solr server");
      if (solr == null)
         return;

      try
      {
         solr.shutdown();
      }
      catch (Exception e)
      {
         logger.log(Level.WARNING, "Failed to cleanly shut down connection to Solr server.", e);
      }
   }

   private Void onCreate(WorksChangeEvent evt)
   {
      Work work;
      try
      {
         work = evt.getWorkEvt();
      }
      catch (WorkNotAvailableException ex)
      {

         return null;
      }

      removeIfPresent(work.getId());

      Collection<SolrInputDocument> solrDocs = new ArrayList<>();
      WorkSolrProxy workProxy = WorkSolrProxy.createWork(work);
      solrDocs.add(workProxy.getDocument());

      for(Edition edition : work.getEditions())
      {
         WorkSolrProxy editionProxy = WorkSolrProxy.createEdition(work.getId(), edition);
         solrDocs.add(editionProxy.getDocument());

         for(Volume volume : edition.getVolumes())
         {
            WorkSolrProxy volumeProxy = WorkSolrProxy.createVolume(work.getId(), edition, volume);
            solrDocs.add(volumeProxy.getDocument());
         }
      }

      try
      {
         solr.add(solrDocs);
         solr.commit();
      }
      catch (SolrServerException | IOException e)
      {
         logger.log(Level.SEVERE, "Failed to commit the work id: [" + work.getId() + "] to the SOLR server. " + e);
      }

      return null;

   }

   private Void onWorkUpdate(WorksChangeEvent workEvt)
   {
      //HACK: Until granular Change notifications are implemented we will remove all works and corresponding editions / volumes.
      //      Once removed we will re-add all entities from the work.
      return onCreate(workEvt);
   }

   private Void onDelete(WorksChangeEvent workEvt)
   {
      String id = workEvt.getWorkId();
      try
      {
         solr.deleteById(id);
         solr.commit();
      }
      catch (SolrServerException | IOException e)
      {
         logger.log(Level.SEVERE, "Failed to delete the work id: [" + id + "] from the SOLR server. " + e);
      }

      return null;
   }

   private void removeIfPresent(String id)
   {
      if (isIndexed(id))
         removeWork(id);
   }

   private static class EntryChangeHandlers<EVENT extends WorksChangeEvent>
   {
      // TODO to make this more general purpose, change to ChangeEvent.
      Map<WorksChangeEvent.ChangeType, Function<EVENT, Void>> changeHandlers = new HashMap<>();

      public void register(WorksChangeEvent.ChangeType type, Function<EVENT, Void> handler)
      {
         changeHandlers.put(type, handler);
      }

      public synchronized void handle(EVENT event)
      {
         ChangeType type = event.getChangeType();
         Function<EVENT, Void> handler = changeHandlers.get(type);
         if (handler == null)
         {
            logger.info("No handler registered for [" + type + "]");
            return;
         }

         try
         {
            handler.apply(event);
         }
         catch (Exception ex)
         {
            logger.log(Level.SEVERE, "Failed to handle " + type + " event for [" + event.getWorkId() + "] ", ex);
         }
      }
   }
}
