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
package edu.tamu.tcat.trc.entries.types.bio.rest;

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.Path;

import edu.tamu.tcat.account.Account;
import edu.tamu.tcat.osgi.config.ConfigurationProperties;
import edu.tamu.tcat.trc.entries.core.repo.EntryRepositoryRegistry;
import edu.tamu.tcat.trc.entries.types.bio.impl.search.BioSearchStrategy;
import edu.tamu.tcat.trc.entries.types.bio.repo.BiographicalEntryRepository;
import edu.tamu.tcat.trc.entries.types.bio.rest.v1.PeopleResource;
import edu.tamu.tcat.trc.entries.types.bio.search.BioEntryQueryCommand;
import edu.tamu.tcat.trc.resolver.EntryResolverRegistry;
import edu.tamu.tcat.trc.search.solr.QueryService;
import edu.tamu.tcat.trc.search.solr.SearchServiceManager;
import edu.tamu.tcat.trc.services.TrcServiceManager;

@Path("/")
public class PeopleResourceService
{

   private final static Logger logger = Logger.getLogger(PeopleResourceService.class.getName());
   // TODO make this core application logic

   private EntryRepositoryRegistry registry;
   private SearchServiceManager searchMgr;
   private ConfigurationProperties config;
   private QueryService<BioEntryQueryCommand> queryService;
   private TrcServiceManager serviceMgr;

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

   public void setServiceMgr(TrcServiceManager serviceMgr)
   {
      this.serviceMgr = serviceMgr;
   }

   // called by DS
   public void activate()
   {
      logger.info("Activating " + getClass().getSimpleName());

      try
      {
         Objects.requireNonNull(searchMgr, "No search service configured");
         Objects.requireNonNull(serviceMgr, "No service manager configured");

         BioSearchStrategy indexCfg = new BioSearchStrategy(config);
         queryService = searchMgr.getQueryService(indexCfg);
      }
      catch (Exception ex)
      {
         logger.log(Level.SEVERE, "Failed to activate " + getClass().getSimpleName(), ex);
         throw ex;
      }
   }

   // called by DS
   public void dispose()
   {

   }

   // Would be better to handle token security here before delegating to the sub-resource,
   // but that would require evaluating the TokenSecurityObjectFilter during @PreMatching,
   // which needs some more investigation.
   //@TokenSecured(payloadType=TrcAccount.class)
   //public PeopleResource delgate(@BeanParam ContextBean bean)
   //{
   //   TrcAccount account = bean.get(TrcAccount.class);    // TODO get this from the request if this is possible here.
   @Path("/people")
   public PeopleResource delgate()
   {
      Account account = null;    // TODO get this from the request if this is possible here.

      BiographicalEntryRepository repo = registry.getRepository(account, BiographicalEntryRepository.class);
      EntryResolverRegistry resolverRegistry = registry.getResolverRegistry();
      return new PeopleResource(repo, queryService, serviceMgr, resolverRegistry);
   }
}
