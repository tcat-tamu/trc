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
package edu.tamu.tcat.trc.digires.books.discovery;

import edu.tamu.tcat.trc.digires.books.resolve.ResourceAccessException;

/**
 * Allows clients to search across multiple data sources to identify potentially relevant
 * digital copies of a bibliographic item. The search service accepts simple key word queries
 * and also allows for date range and author filters. In general, the search implementation
 * will prioritize results based on the date range and author filters but will not strictly
 * remove non-matching entities since not all data sources provide this information.
 *
 * <p>
 * Search results return serializable instances of {@link DigitalCopyProxy}s. This objects are
 * intended to be lightweight identifiers that support basic display information so users can
 * readily identify copies that are of potential interest and uniquely identify those copies
 * in order to retrieve the full record corresponding to the proxy.
 */
public interface CopySearchService
{
   /**
    * Attempts to find digital copies within the repository mediated this
    *
    * @param query The query to be executed. Note that not all underlying search service
    *       implementations support all search query fields.
    * @return The results for the supplied query.
    * @throws ResourceAccessException If there are un-recoverable errors attempting to execute
    *       the supplied search. In general, implementations should attempt to provide partial
    *       results along with an error message indicating any failures if possible.
    */
   CopySearchResult find(ContentQuery query) throws ResourceAccessException;
}
