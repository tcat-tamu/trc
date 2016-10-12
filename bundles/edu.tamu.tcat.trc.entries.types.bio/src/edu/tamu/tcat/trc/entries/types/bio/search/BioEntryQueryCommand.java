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

import java.util.concurrent.CompletableFuture;

/**
 * Command for use in querying the associated {@link BioEntrySearchService} which provides
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
public interface BioEntryQueryCommand
{
   /**
    * Submit a query with no query parameters. This will return the first results upto
    * the specified (or default) number of results.
    */
   void queryAll();

   /**
    * Supply a free-text query string to search the underlying database.
    *
    * @param q The user-supplied query string.
    * @throws SearchException
    */
   void query(String q);

   /**
    * Query for biographical entries based on the family or last name of the
    * individual. This will search over the canonical and all all alternative names.
    *
    * @param familyName The name to search for.
    */
   void queryFamilyName(String familyName);

   /**
    * Set the offset of the first record to return. Used for executing paged query requests.
    * @param start The offset of the first record.
    */
   void setOffset(int start);

   /**
    * @param max the maximum number of results to return.
    */
   void setMaxResults(int max);

   /**
    * Execute the query command.
    * @return The matching search results.
    * @throws SearchException If errors are encountered executing the supplied query.
    * @deprecated Use {@link #execute()} instead.
    */
   @Deprecated
   PersonSearchResult executeSync();

   default CompletableFuture<PersonSearchResult> execute()
   {
      CompletableFuture<PersonSearchResult> results = new CompletableFuture<>();

      try
      {
         PersonSearchResult rs = this.executeSync();
         results.complete(rs);
      }
      catch (Exception ex)
      {
         results.completeExceptionally(ex);
      }

      return results;
   }
}
