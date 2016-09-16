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

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.solr.client.solrj.SolrClient;

import edu.tamu.tcat.osgi.config.ConfigurationProperties;
import edu.tamu.tcat.trc.entries.core.repo.EntryRepository;
import edu.tamu.tcat.trc.entries.core.repo.EntryRepositoryRegistry;
import edu.tamu.tcat.trc.entries.core.repo.EntryUpdateRecord;
import edu.tamu.tcat.trc.entries.core.search.SolrSearchMediator;
import edu.tamu.tcat.trc.entries.types.reln.Relationship;
import edu.tamu.tcat.trc.entries.types.reln.repo.RelationshipRepository;
import edu.tamu.tcat.trc.entries.types.reln.search.RelationshipQueryCommand;
import edu.tamu.tcat.trc.entries.types.reln.search.RelationshipSearchIndexManager;
import edu.tamu.tcat.trc.entries.types.reln.search.RelationshipSearchService;
import edu.tamu.tcat.trc.search.SearchException;
import edu.tamu.tcat.trc.search.solr.impl.BasicIndexService;
import edu.tamu.tcat.trc.search.solr.impl.BasicIndexSvcBuilder;
import edu.tamu.tcat.trc.search.solr.impl.TrcQueryBuilder;

public class SolrRelationshipSearchService implements RelationshipSearchIndexManager, RelationshipSearchService
{
   private final static Logger logger = Logger.getLogger(SolrRelationshipSearchService.class.getName());

   /** Configuration property key that defines Solr core to be used for relationships. */
   public static final String SOLR_CORE = "reln";

   private RelationshipRepository repo;
   private EntryRepository.ObserverRegistration registration;
   private ConfigurationProperties config;

   private SolrClient solr;

   private BasicIndexService<Relationship> indexSvc;

   public void setConfiguration(ConfigurationProperties config)
   {
      this.config = config;
   }

   public void setRepoRegistry(EntryRepositoryRegistry registry)
   {
      this.repo = registry.getRepository(null, RelationshipRepository.class);
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
      Objects.requireNonNull(repo, "No relationship repository configured");
      Objects.requireNonNull(config, "No configuration properties provided.");

      // construct Solr core
      BasicIndexSvcBuilder<Relationship> indexBuilder = new BasicIndexSvcBuilder<>(config, SOLR_CORE);
      indexSvc = indexBuilder.setDataAdapter(RelnDocument::create)
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

   private void index(EntryUpdateRecord<Relationship> ctx)
   {
      SolrSearchMediator.index(indexSvc, ctx);
   }


   @Override
   public RelationshipQueryCommand createQueryCommand() throws SearchException
   {
      TrcQueryBuilder builder = new TrcQueryBuilder(new RelnSolrConfig());
      return new RelationshipSolrQueryCommand(solr, builder);
   }

}
