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
package edu.tamu.tcat.trc.entries.types.biblio.copies.search;

import edu.tamu.tcat.trc.entries.types.biblio.search.WorkQueryCommand;
import edu.tamu.tcat.trc.search.SearchException;

public interface PageSearchCommand
{

   /*
    * In keeping with the "spirit of search", the window (offset + length) and other paramters
    * are configured in the query itself and not in a result with a long lifecycle.
    */
   PageSearchResult execute() throws SearchException;

   /**
    * Supply a "basic" free-text, keyword query to be executed. In general, the supplied query will
    * be executed against a wide range of fields (e.g., author, title, abstract, publisher, etc.)
    * with different fields being assigned different levels of boosting (per-field weights).
    * The specific fields to be searched and the relative weights associated with different
    * fields is implementation-dependent.
    *
    * @param basicQueryString The "basic" query string. May be {@code null} or empty.
    */
   void query(String basicQueryString) throws SearchException;

   void addVolumeFilter(String volumeId) throws SearchException;

   /**
    * Sets the index offset of the first result to be returned. Useful in conjunction with
    * {@link WorkQueryCommand#setMaxResults(int) } to support result paging. Note that
    * implementations are <em>strongly</em> encouraged to make a best-effort attempt to
    * preserve result order across multiple invocations of the same query.  In general, this
    * is a challenging problem in the face of updates to the underlying index and implementations
    * are not required to guarantee result order consistency of result order across multiple
    * calls.
    *
    * @param offset
    */
   void setOffset(int offset);

   /**
    * Specify the maximum number of results to be returned. Implementations may return fewer
    * results but must not return more.
    * <p>
    * If not specified, the default is 25.
    *
    * @param count
    */
   void setMaxResults(int count);
}
