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
package edu.tamu.tcat.trc.entries.types.bio.impl.search;

import java.util.List;

import edu.tamu.tcat.trc.entries.types.bio.search.BioSearchProxy;
import edu.tamu.tcat.trc.entries.types.bio.search.BioEntryQueryCommand;
import edu.tamu.tcat.trc.entries.types.bio.search.PersonSearchResult;

public class SolrPersonResults implements PersonSearchResult
{
   // TODO seems like this needs to be re-thought and extended. As it is, it provides little value
   //      other than to store a list of results. Notably, it seems like we ought to
   //      - distinguish between the query command and a serializable query.
   //      - provide better support for pagable results
   //      - support facetted queries
   //
   private List<BioSearchProxy> items;
   private PeopleSolrQueryCommand cmd;

   SolrPersonResults(PeopleSolrQueryCommand cmd, List<BioSearchProxy> items)
   {
      this.cmd = cmd;
      this.items = items;
   }

   @Override
   public List<BioSearchProxy> get()
   {
      return items;
   }

   @Override
   public BioEntryQueryCommand getCommand()
   {
      return cmd;
   }
}
