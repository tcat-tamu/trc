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

import java.util.logging.Logger;

import javax.ws.rs.Path;

import edu.tamu.tcat.trc.entries.types.bio.repo.PeopleRepository;
import edu.tamu.tcat.trc.entries.types.bio.search.PeopleSearchService;

public class PeopleResourceService
{
   // records internal errors accessing the REST
   static final Logger errorLogger = Logger.getLogger(PeopleResourceService.class.getName());

   private PeopleRepository repo;
   private PeopleSearchService peopleSearchService;

   private PeopleResource resource;

   // called by DS
   public void setRepository(PeopleRepository repo)
   {
      this.repo = repo;
   }

   public void setPeopleService(PeopleSearchService service)
   {
      this.peopleSearchService = service;
   }

   // called by DS
   public void activate()
   {
      resource = new PeopleResource(repo, peopleSearchService);
   }

   // called by DS
   public void dispose()
   {

   }

   @Path("/people")
   public PeopleResource delgate()
   {
      // TODO add version selection on this?
      return resource;
   }
}
