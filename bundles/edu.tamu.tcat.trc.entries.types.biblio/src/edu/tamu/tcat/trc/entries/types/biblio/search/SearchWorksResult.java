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
package edu.tamu.tcat.trc.entries.types.biblio.search;

import java.util.List;

import edu.tamu.tcat.trc.entries.types.biblio.BibliographicEntry;

/**
 * The result set of {@link BibliographicEntry}s matched by a {@link WorkQueryCommand}.
 * A result set has no functionality other than retrieving matched results from an executed
 * query. It should be considered "stale" as soon as it is acquired due to the inherently
 * unstable nature of a search framework.
 */
public interface SearchWorksResult
{
   /**
    * Get the {@link WorkQueryCommand} which executed to provide this result.
    */
   WorkQueryCommand getCommand();

   /**
    * @return Proxies for the works that match the current search.
    */
   List<BiblioSearchProxy> get();

   //TODO: add support for retrieving facet information
}
