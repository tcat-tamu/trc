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
package edu.tamu.tcat.trc.entries.types.biblio.search.solr.copies;

import java.util.List;

import edu.tamu.tcat.trc.entries.types.biblio.copies.search.PageSearchCommand;
import edu.tamu.tcat.trc.entries.types.biblio.copies.search.PageSearchProxy;
import edu.tamu.tcat.trc.entries.types.biblio.copies.search.PageSearchResult;

public class SolrPageResults implements PageSearchResult
{
   private PageSolrSearchCommand cmd;
   private List<PageSearchProxy> page;

   public SolrPageResults(PageSolrSearchCommand cmd, List<PageSearchProxy> page)
   {
      this.cmd = cmd;
      this.page = page;
   }

   @Override
   public PageSearchCommand getCommand()
   {
      return cmd;
   }

   @Override
   public List<PageSearchProxy> get()
   {
      return page;
   }

}
