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

import edu.tamu.tcat.trc.search.SearchException;

/**
 * Command for use in querying the associated {@link PeopleSearchService} which provides
 * search proxy instances.
 *
 * <p>
 * A {@code PeopleQueryCommand} is intended to be initialized, executed a single time, provide
 * results, and be discarded.
 *
 * <p>
 * The various "query" methods are intended to be for user-entered criteria which results in "like",
 * wildcard, or otherwise interpreted query criteria which may apply to multiple fields of the index.
 * Alternately, the various "filter" methods are intended for specific criteria which typically
 * applies to faceted searching or to known criteria for specific stored data.
 */
public interface PeopleQueryCommand
{
   // TODO need to be able to create serializable representation.
   // TODO need to document methods

   PersonSearchResult execute() throws SearchException;

   void query(String q) throws SearchException;

   void queryAll() throws SearchException;

   void queryFamilyName(String familyName) throws SearchException;

   void setOffset(int start);

   void setMaxResults(int max);

}
