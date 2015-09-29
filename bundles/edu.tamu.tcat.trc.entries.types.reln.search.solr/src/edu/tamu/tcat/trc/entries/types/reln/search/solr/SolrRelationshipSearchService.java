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
package edu.tamu.tcat.trc.entries.types.reln.search.solr;

import java.io.IOException;
import java.net.URI;
import java.text.MessageFormat;
import java.util.Objects;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;

import edu.tamu.tcat.osgi.config.ConfigurationProperties;
import edu.tamu.tcat.trc.entries.repo.CatalogRepoException;
import edu.tamu.tcat.trc.entries.types.reln.Relationship;
import edu.tamu.tcat.trc.entries.types.reln.repo.RelationshipChangeEvent;
import edu.tamu.tcat.trc.entries.types.reln.repo.RelationshipRepository;
import edu.tamu.tcat.trc.entries.types.reln.repo.RelationshipTypeRegistry;
import edu.tamu.tcat.trc.entries.types.reln.search.RelationshipQueryCommand;
import edu.tamu.tcat.trc.entries.types.reln.search.RelationshipSearchIndexManager;
import edu.tamu.tcat.trc.entries.types.reln.search.RelationshipSearchService;
import edu.tamu.tcat.trc.search.SearchException;
import edu.tamu.tcat.trc.search.solr.impl.TrcQueryBuilder;

public class SolrRelationshipSearchService implements RelationshipSearchIndexManager, RelationshipSearchService
{
   private final static Logger logger = Logger.getLogger(SolrRelationshipSearchService.class.getName());

   /** Configuration property key that defines the URI for the Solr server. */
   public static final String SOLR_API_ENDPOINT = "solr.api.endpoint";

   /** Configuration property key that defines Solr core to be used for relationships. */
   public static final String SOLR_CORE = "catalogentries.relationships.solr.core";

   private RelationshipRepository repo;
   private AutoCloseable registration;
   private SolrServer solr;
   private ConfigurationProperties config;
   private RelationshipTypeRegistry typeReg;

   // HACK: Relationships are set to the db in an asynchronous matter. It can not be quarenteed that db operations will be
   //       completed before the next operation starts, causing an error. The create/update/delete process "should" not have
   //       that many requests to see this occur.
   public void setRelationshipRepo(RelationshipRepository repo)
   {
      this.repo = repo;
   }

   public void setConfiguration(ConfigurationProperties config)
   {
      this.config = config;
   }

   public void setTypeRegistry(RelationshipTypeRegistry typeReg)
   {
      this.typeReg = typeReg;
   }

   public void activate()
   {
      logger.fine("Activating SolrRelationshipSearchService");
      Objects.requireNonNull(repo, "No relationship repository supplied.");
      registration = repo.addUpdateListener(this::onUpdate);

      // construct Solr core
      URI solrBaseUri = config.getPropertyValue(SOLR_API_ENDPOINT, URI.class);
      String solrCore = config.getPropertyValue(SOLR_CORE, String.class);

      URI coreUri = solrBaseUri.resolve(solrCore);
      logger.info("Connecting to Solr Service [" + coreUri + "]");

      solr = new HttpSolrServer(coreUri.toString());
   }

   public void deactivate()
   {
      logger.info("Deactivating SolrRelationshipSearchService");

      unregisterRepoListener();
      releaseSolrConnection();
   }

   @Override
   public RelationshipQueryCommand createQueryCommand() throws SearchException
   {
      return new RelationshipSolrQueryCommand(solr, typeReg, new TrcQueryBuilder(solr, new RelnSolrConfig()));
   }

   private void unregisterRepoListener()
   {
      if (registration != null)
      {
         try
         {
            registration.close();
         }
         catch (Exception e)
         {
            logger.log(Level.WARNING, "Failed to unregister update listener on relationship repository.", e);
         }
         finally {
            registration = null;
         }
      }
   }

   private void releaseSolrConnection()
   {
      logger.fine("Releasing connection to Solr server");
      if (solr != null)
      {
         try
         {
            solr.shutdown();
         }
         catch (Exception e)
         {
            logger.log(Level.WARNING, "Failed to cleanly shut down connection to Solr server.", e);
         }
      }
   }

   private void onUpdate(RelationshipChangeEvent evt)
   {
      // NOTE: since this is an event listener, it serves as a fault barrier
      try
      {
         String id = evt.getEntityId();
         switch (evt.getUpdateAction())
         {
            case CREATE:
               index(id, RelnDocument::create);
               break;
            case UPDATE:
               index(id, RelnDocument::update);
               break;
            case DELETE:
               solr.deleteById(id);
               break;
            default:
               logger.log(Level.INFO, "Unexpected relationship change event " + evt);
         }

         solr.commit();
      }
      catch (Exception ex)
      {
         logger.log(Level.WARNING, "Failed to update search indices following a change to relationship: " + evt, ex);
      }
   }

   private void index(String id, Function<Relationship, RelnDocument> adapter)
         throws CatalogRepoException, SolrServerException, IOException
   {
      Relationship relationship = repo.get(id);
      Objects.requireNonNull(relationship,
            MessageFormat.format("Failed to retrieve relationship with id {0}", id));

      RelnDocument proxy = adapter.apply(relationship);
      solr.add(proxy.getDocument());
   }
}
