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
package edu.tamu.tcat.trc.entries.types.reln.search;

import java.net.URI;

import edu.tamu.tcat.trc.SearchException;

public interface RelationshipQueryCommand
{
   RelationshipSearchResult execute() throws SearchException;

   //TODO: convert this query to use edismax and rename methods to proper pattern
   @Deprecated // rename to "query*" or "filter*"
   void forEntity(URI entity, RelationshipDirection direction) throws SearchException;

   @Deprecated // rename to "query*" or "filter*"
   void byType(String typeId) throws SearchException;

   void setOffset(int start);
   void setMaxResults(int rows);
}
