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
package edu.tamu.tcat.trc.entries.types.bio.rest.v1;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.Path;

import edu.tamu.tcat.account.Account;
import edu.tamu.tcat.osgi.config.ConfigurationProperties;
import edu.tamu.tcat.trc.entries.core.repo.EntryRepositoryRegistry;
import edu.tamu.tcat.trc.entries.types.bio.impl.search.BioSearchStrategy;
import edu.tamu.tcat.trc.entries.types.bio.repo.BiographicalEntryRepository;
import edu.tamu.tcat.trc.entries.types.bio.search.BioEntryQueryCommand;
import edu.tamu.tcat.trc.search.solr.QueryService;
import edu.tamu.tcat.trc.search.solr.SearchServiceManager;

@Path("/")
public class PeopleResourceService
{

   private final static Logger logger = Logger.getLogger(PeopleResourceService.class.getName());
   // TODO make this core application logic

   private EntryRepositoryRegistry registry;
   private SearchServiceManager searchMgr;
   private ConfigurationProperties config;
   private QueryService<BioEntryQueryCommand> queryService;

   public void setConfig(ConfigurationProperties config)
   {
      this.config = config;
   }

   public void setRepoRegistry(EntryRepositoryRegistry registry)
   {
      this.registry = registry;
   }

   public void setSearchSvcMgr(SearchServiceManager searchMgr)
   {
      this.searchMgr = searchMgr;
   }

   // called by DS
   public void activate()
   {
      logger.info("Activating " + getClass().getSimpleName());
      if (searchMgr == null)
      {
         logger.warning("No search service has provided to " + getClass().getSimpleName());
         return;
      }

      try
      {
         BioSearchStrategy indexCfg = new BioSearchStrategy(config);
         queryService = searchMgr.getQueryService(indexCfg);
      }
      catch (Exception ex)
      {
         logger.log(Level.SEVERE, "Failed to load query service for bibographical entries REST servivce", ex);
         throw ex;
      }
   }

   // called by DS
   public void dispose()
   {

   }

   @Path("/people")
   public PeopleResource delgate()
   {
      Account account = null;    // TODO get this from the request if this is possible here.

      BiographicalEntryRepository repo = registry.getRepository(account, BiographicalEntryRepository.class);
      return new PeopleResource(repo, queryService);
   }
}
