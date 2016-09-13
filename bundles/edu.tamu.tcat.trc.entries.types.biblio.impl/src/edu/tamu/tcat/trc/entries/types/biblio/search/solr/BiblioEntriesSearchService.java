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

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.solr.client.solrj.SolrClient;

import edu.tamu.tcat.osgi.config.ConfigurationProperties;
import edu.tamu.tcat.trc.entries.core.repo.EntryRepository.ObserverRegistration;
import edu.tamu.tcat.trc.entries.core.repo.EntryRepositoryRegistry;
import edu.tamu.tcat.trc.entries.core.repo.EntryUpdateRecord;
import edu.tamu.tcat.trc.entries.core.search.SolrSearchMediator;
import edu.tamu.tcat.trc.entries.types.biblio.BibliographicEntry;
import edu.tamu.tcat.trc.entries.types.biblio.repo.BibliographicEntryRepository;
import edu.tamu.tcat.trc.entries.types.biblio.search.WorkQueryCommand;
import edu.tamu.tcat.trc.entries.types.biblio.search.WorkSearchService;
import edu.tamu.tcat.trc.search.SearchException;
import edu.tamu.tcat.trc.search.solr.IndexService;
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
   public static final String SOLR_CORE = "biblio";

   private ConfigurationProperties config;
   private SolrClient solr;

   private BibliographicEntryRepository repo;

   private ObserverRegistration registration;

   private IndexService<BibliographicEntry> indexSvc;

   /**
    * @param cp configuration properties. These are required at initialization.
    */
   public void setConfiguration(ConfigurationProperties cp)
   {
      this.config = cp;
   }

   public void setRepoRegistry(EntryRepositoryRegistry registry)
   {
      this.repo = registry.getRepository(null, BibliographicEntryRepository.class);
   }

   public void activate()
   {
      logger.info("Activating " + getClass().getSimpleName());

      try {
         doActivation();
      } catch (Exception ex) {
         logger.log(Level.SEVERE, "Failed to activate" + getClass().getSimpleName(), ex);
         throw ex;
      }
   }

   private void doActivation()
   {
      Objects.requireNonNull(repo, "No bibliographic entry repository configured");
      Objects.requireNonNull(config, "No configuration properties provided.");

      // construct Solr core
      BasicIndexService.Builder<BibliographicEntry> indexBuilder = new BasicIndexService.Builder<>(config, SOLR_CORE);
      indexSvc = indexBuilder.setDataAdapter(IndexAdapter::createWork)
                  .setIdProvider(entry -> entry.getId())
                  .build();

      registration = repo.onUpdate(this::index);
      this.solr = indexSvc.getSolrClient();
   }

   public void deactivate()
   {
      logger.info("Deactivating " + getClass().getSimpleName());
      if (registration != null)
         registration.close();

      registration = null;
   }

   private void index(EntryUpdateRecord<BibliographicEntry> ctx)
   {
      SolrSearchMediator.index(indexSvc, ctx);
   }

   @Override
   public WorkQueryCommand createQueryCommand() throws SearchException
   {
      // TODO this could be moved into index service
      TrcQueryBuilder builder = new TrcQueryBuilder(new BiblioSolrConfig());
      return new WorkSolrQueryCommand(solr, builder);
   }
}
