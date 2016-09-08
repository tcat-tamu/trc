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
package edu.tamu.tcat.trc.entries.types.biblio.search.solr;

import static java.text.MessageFormat.format;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;

import edu.tamu.tcat.osgi.config.ConfigurationProperties;
import edu.tamu.tcat.trc.entries.types.biblio.dto.WorkDTO;
import edu.tamu.tcat.trc.entries.types.biblio.search.WorkQueryCommand;
import edu.tamu.tcat.trc.entries.types.biblio.search.WorkSearchService;
import edu.tamu.tcat.trc.search.SearchException;
import edu.tamu.tcat.trc.search.solr.impl.BasicIndexService;
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

   private ConfigurationProperties config;
   private SolrClient solr;

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

      // construct Solr core
      URI solrBaseUri = config.getPropertyValue(SOLR_API_ENDPOINT, URI.class);
      String solrCore = config.getPropertyValue(SOLR_CORE, String.class);
      BasicIndexService<WorkDTO> indexSvc = new BasicIndexService<>(
            config.getPropertyValue(SOLR_API_ENDPOINT, URI.class),
            config.getPropertyValue(SOLR_CORE, String.class),
            BiblioEntriesSearchService::adapt,
            entry -> entry.id);

      URI coreUri = solrBaseUri.resolve(solrCore);
      logger.info("Connecting to Solr Service [" + coreUri + "]");

      solr = new HttpSolrClient(coreUri.toString());
   }

   public void deactivate()
   {
      logger.info("Deactivating BiblioEntriesSearchService");

      releaseSolrConnection();
   }

   @Override
   public WorkQueryCommand createQueryCommand() throws SearchException
   {
      TrcQueryBuilder builder = new TrcQueryBuilder(new BiblioSolrConfig());
      return new WorkSolrQueryCommand(solr, builder);
   }

   private void releaseSolrConnection()
   {
      logger.fine("Releasing connection to Solr server");

      if (solr == null)
      {
         return;
      }

      try
      {
         solr.close();
      }
      catch (Exception e)
      {
         logger.log(Level.WARNING, "Failed to cleanly shut down connection to Solr server.", e);
      }
   }

   private static SolrInputDocument adapt(WorkDTO entry)
   {
      try
      {
         return IndexAdapter.createWork(entry);
      }
      catch (SearchException ex)
      {
         throw new IllegalStateException(format("Failed to adapt bibliographic entry [{0}]", entry.id), ex);
      }
   }

   /**
    * @deprecated The indexing capabilities are in the process of being replaced with a new model. The old
    *       model, however, is significantly more complex and requires special handling to index and remove
    *       objects that is not currently supported by the general purpose tooling. This will be removed once
    *       the underlying models have been updated.
    */
   @Deprecated // the indexing
   public  void remove(String id)
   {
      if (isIndexed(id))
      {
         removeWork(id);
      }
   }

   /**
    * Determines whether a work with the given ID exists in the search index
    *
    * @param id
    * @return
    */
   private boolean isIndexed(String id)
   {
      Objects.requireNonNull(solr, "The connection to Solr server is not available.");

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

   /**
    * Deletes work from index, along with all editions and volumes.
    *
    * @param id ID of the owning work.
    */
   private void removeWork(String id)
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
}
