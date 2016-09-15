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
package edu.tamu.tcat.trc.entries.types.bio.search;

import java.util.List;

/**
 * The result set of person entries matched by a {@link BioEntryQueryCommand}.
 * A result set has no functionality other than retrieving matched results from an executed
 * query. It should be considered "stale" as soon as it is acquired due to the inherently
 * unstable nature of a search framework.
 */
public interface PersonSearchResult
{
   /**
    * Get the {@link BioEntryQueryCommand} which executed to provide this result.
    */
   BioEntryQueryCommand getCommand();

   /**
    * @return Proxies for the people that match the current search.
    */
   List<BioSearchProxy> get();

   //TODO: add support for retrieving facet information
}
