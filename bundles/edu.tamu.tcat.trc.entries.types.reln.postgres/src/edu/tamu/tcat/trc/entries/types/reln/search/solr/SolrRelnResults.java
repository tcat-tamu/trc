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

import java.util.List;

import edu.tamu.tcat.trc.entries.types.reln.search.RelationshipSearchResult;
import edu.tamu.tcat.trc.entries.types.reln.search.RelnSearchProxy;

public class SolrRelnResults implements RelationshipSearchResult
{
   private List<RelnSearchProxy> items;
   private RelationshipSolrQueryCommand cmd;

   SolrRelnResults(RelationshipSolrQueryCommand cmd, List<RelnSearchProxy> items)
   {
      this.cmd = cmd;
      this.items = items;
   }

   //HACK this is a degenerate impl for current puproses
   //TODO: what does this hack message mean? --pb
   @Override
   public List<RelnSearchProxy> get()
   {
      return items;
   }

   @Override
   public RelationshipSolrQueryCommand getCommand()
   {
      return cmd;
   }
}
