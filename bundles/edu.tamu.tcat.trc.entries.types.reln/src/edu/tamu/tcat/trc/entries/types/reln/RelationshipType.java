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
package edu.tamu.tcat.trc.entries.types.reln;

public interface RelationshipType
{

   /**
    * Returns a string that uniquely identifies this relationship. Identifiers must be
    * unique across the universe of relationships are are intended to be used
    * across systems and applications. Consequently, well-known URI's are strongly
    * encouraged. For example: {@code http://sda.bodleian.ox.ac.uk/catalog/schema/reln#influence}
    * rather than {@code influence}.
    *
    * <p>
    * Note that these URLs do not need to resolve to specific resources, but they should
    * be known and documented by the implementing application. In general, well known
    * relationship schemes should be used in preference to idiomatic types.
    *
    * TODO document normative type schemas.
    *
    * @return the identifier for this relationship type.
    */
   String getIdentifier();

   /**
    * @return The title of this relationship for display. For directed relationships,
    *       this title communicates the relationship from the source entity to the
    *       the target entity. For example "[source work] influenced [target work]".
    */
   String getTitle();

   /**
    * @return The reverse reading direction title of this relationship for display. For
    *       directed relationships only, this title communicates the relationship from
    *       the target entity to the the source entity. For example, "[target work] was
    *       influenced by [source work]". For undirected relationships, the value of this
    *       property is undefined.
    */
   String getReverseTitle();

   /**
    * @return A description of the intended meaning of this relationship type.
    */
   String getDescription();


   /**
    * @return {@code true} If this relationship is directed, {@code false} otherwise.
    */
   boolean isDirected();

}
