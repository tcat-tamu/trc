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
package edu.tamu.tcat.trc.digires.rest.books.v1;

import java.util.Collection;

import edu.tamu.tcat.trc.digires.books.discovery.CopySearchResult;
import edu.tamu.tcat.trc.digires.books.discovery.DigitalCopyProxy;

public class SearchResult
{
   // Return q with proxy
   // { q: { },
   //   resutls: [ { these are the DigitalCopyProxy's} }
   public CopyQueryDTO query;
   public Collection<DigitalCopyProxy> copies;

   public SearchResult(CopySearchResult result, CopyQueryDTO query)
   {
      this.query = query;
      this.copies = result.asCollection();
   }

}
