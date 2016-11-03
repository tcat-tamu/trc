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

import edu.tamu.tcat.trc.TrcApplication;
import edu.tamu.tcat.trc.entries.types.bio.rest.v1.PeopleResource;

@Path("/")
public class PeopleResourceService
{
   private final static Logger logger = Logger.getLogger(PeopleResourceService.class.getName());

   private TrcApplication trcMgr;

   public void setTrcApplication(TrcApplication trcMgr)
   {
      this.trcMgr = trcMgr;
   }

   // called by DS
   public void activate()
   {
      logger.info("Activating " + getClass().getSimpleName());

      try
      {
         Objects.requireNonNull(trcMgr, "No search service configured");
      }
      catch (Exception ex)
      {
         logger.log(Level.SEVERE, "Failed to activate " + getClass().getSimpleName(), ex);
         throw ex;
      }
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
      return new PeopleResource(trcMgr);
   }
}
