/*
 * Copyright 2015 Texas A&M Engineering Experiment Station
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.tamu.tcat.trc.entries.types.bib.search.solr;

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

import edu.tamu.tcat.osgi.config.ConfigurationProperties;
import edu.tamu.tcat.trc.entries.notification.UpdateEvent;
import edu.tamu.tcat.trc.entries.types.biblio.Edition;
import edu.tamu.tcat.trc.entries.types.biblio.Volume;
import edu.tamu.tcat.trc.entries.types.biblio.Work;
import edu.tamu.tcat.trc.entries.types.biblio.repo.WorkChangeEvent;
import edu.tamu.tcat.trc.entries.types.biblio.repo.WorkRepository;
import edu.tamu.tcat.trc.entries.types.biblio.search.WorkQueryCommand;
import edu.tamu.tcat.trc.entries.types.biblio.search.WorkSearchService;
import edu.tamu.tcat.trc.search.SearchException;
import edu.tamu.tcat.trc.search.solr.impl.TrcQueryBuilder;

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
      logger.fine("Activating " + getClass().getSimpleName());
      // Check here is only necessary for test cases which instantiate the service; DS will ensure it is not null
      Objects.requireNonNull(repo, "No work repository supplied.");

      // configure handlers
      EntryChangeHandlers<WorkChangeEvent> updateHandlers = new EntryChangeHandlers<WorkChangeEvent>();
      updateHandlers.register(UpdateEvent.UpdateAction.CREATE, this::onCreate);
      updateHandlers.register(UpdateEvent.UpdateAction.UPDATE, this::onWorkUpdate);
      updateHandlers.register(UpdateEvent.UpdateAction.DELETE, this::onDelete);
      registration = repo.addUpdateListener(updateHandlers::handle);

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
   public WorkQueryCommand createQueryCommand() throws SearchException
   {
      return new WorkSolrQueryCommand(solr, new TrcQueryBuilder(solr, new BiblioSolrConfig()));
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

   private Void onCreate(WorkChangeEvent evt)
   {
      Work work;
      try
      {
         work = evt.getWork();
      }
      catch (Exception ex)
      {
         logger.log(Level.WARNING, "Failed accessing work after create " + evt, ex);
         return null;
      }

      removeIfPresent(work.getId());

      Collection<SolrInputDocument> solrDocs = new ArrayList<>();
      try
      {
         BiblioDocument workDoc = BiblioDocument.createWork(work);
         solrDocs.add(workDoc.getDocument());

         for(Edition edition : work.getEditions())
         {
            BiblioDocument editionDoc = BiblioDocument.createEdition(work.getId(), edition);
            solrDocs.add(editionDoc.getDocument());

            for(Volume volume : edition.getVolumes())
            {
               BiblioDocument volumeDoc = BiblioDocument.createVolume(work.getId(), edition, volume);
               solrDocs.add(volumeDoc.getDocument());
            }
         }
      }
      catch (Exception e)
      {
         logger.log(Level.SEVERE, "Failed to adapt created Work to indexable data transfer objects for work id: [" + work.getId() + "]", e);
         return null;
      }

      try
      {
         solr.add(solrDocs);
         solr.commit();
      }
      catch (SolrServerException | IOException e)
      {
         logger.log(Level.SEVERE, "Failed to commit the work id: [" + work.getId() + "] to the SOLR server.", e);
      }

      return null;

   }

   private Void onWorkUpdate(WorkChangeEvent workEvt)
   {
      //HACK: Until granular Change notifications are implemented we will remove all works and corresponding editions / volumes.
      //      Once removed we will re-add all entities from the work.
      return onCreate(workEvt);
   }

   private Void onDelete(WorkChangeEvent workEvt)
   {
      String id = workEvt.getEntityId();
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

   private static class EntryChangeHandlers<EVENT extends WorkChangeEvent>
   {
      // TODO to make this more general purpose, change to ChangeEvent.
      Map<UpdateEvent.UpdateAction, Function<EVENT, Void>> changeHandlers = new HashMap<>();

      public void register(UpdateEvent.UpdateAction type, Function<EVENT, Void> handler)
      {
         changeHandlers.put(type, handler);
      }

      public synchronized void handle(EVENT event)
      {
         UpdateEvent.UpdateAction type = event.getUpdateAction();
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
            logger.log(Level.SEVERE, "Failed to handle event " + event, ex);
         }
      }
   }
}
