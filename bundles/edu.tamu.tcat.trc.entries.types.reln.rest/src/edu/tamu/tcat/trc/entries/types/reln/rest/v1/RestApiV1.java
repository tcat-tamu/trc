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
package edu.tamu.tcat.trc.entries.types.reln.rest.v1;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.BadRequestException;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.base.Joiner;

import edu.tamu.tcat.trc.entries.types.reln.search.RelationshipDirection;

public class RestApiV1
{
   /**
    * Simple data vehicle for reporting relationship identifiers as JSON objects.
    */
   public static class RelationshipId
   {
      public String id;
   }

   /**
    *  A JSON serializable representation of a {@link edu.tamu.tcat.trc.entries.types.reln.RelationshipType} for use in the REST API.
    */
   @JsonIgnoreProperties(ignoreUnknown = true)
   public static class RelationshipType
   {
      /**
      *  A string that uniquely identifies this relationship.
      *  @see RelationshipType#getIdentifier()
      */
      public String identifier;

      /**
      * The title of this relationship for display.
      * @see RelationshipType#getTitle()
      */
      public String title;

      /**
      * The reverse reading direction title of this relationship for display.
      * @see RelationshipType#getReverseTitle()
      */
      public String reverseTitle;

      /**
      * A textual description of the intended meaning of this relationship type.
      * @see RelationshipType#getDescription()
      */
      public String description;

      /**
      * {@code true} If this relationship is directed, {@code false} otherwise.
      * @see RelationshipType#isDirected()
      */
      public boolean isDirected;
   }

   @JsonIgnoreProperties(ignoreUnknown = true)
   public static class Relationship
   {
      public String id;
      public String typeId;
      public String description;
      public Set<Anchor> relatedEntities;
      public Set<Anchor> targetEntities;
   }

   @JsonIgnoreProperties(ignoreUnknown = true)
   public static class Anchor
   {
      public String label;
      public Set<String> entryUris;
   }

   /**
    * A DTO to be used as a REST query or path parameter. This class parses the String
    * sent (using the REST API format) as the parameter value into a {@link RelationshipDirection}.
    */
   @JsonIgnoreProperties(ignoreUnknown = true)
   public static class RelDirection
   {
      public final RelationshipDirection dir;

      public RelDirection(String d)
      {
         if (d == null || d.trim().isEmpty())
         {
            dir = RelationshipDirection.any;
         }
         else
         {
            try
            {
               dir = RelationshipDirection.valueOf(d.toLowerCase());
            }
            catch (Exception iea)
            {
               // TODO use String.join
               Joiner joiner = Joiner.on(", ");
               //FIXME: this needs to build a Response to properly report to the client
               throw new BadRequestException("Invalid value for query parameter 'direction' [" + d + "]. Must be one of the following: " + joiner.join(RelationshipDirection.values()));
            }
         }
      }

      public String toValue()
      {
         return dir.toString();
      }
   }

   @JsonIgnoreProperties(ignoreUnknown = true)
   public static class RelationshipSearchResultSet
   {
      public List<RelationshipSearchResult> items;
      /** The querystring that resulted in this result set */
      public String qs;
      public String qsNext;
      public String qsPrev;
   }

   @JsonIgnoreProperties(ignoreUnknown = true)
   public static class RelationshipSearchResult
   {
      public String id;
      public String typeId;
      public String description;
      public String descriptionMimeType;
      public Set<Anchor> relatedEntities;
      public Set<Anchor> targetEntities;
   }

   /**
    * Given a referent entry, this result set will contain all corresponding relationships grouped
    * by type and further by the directionality relative to the referent entry.
    */
   @JsonIgnoreProperties(ignoreUnknown = true)
   public static class GroupedSearchResultSet
   {
      public String referent;
      public List<RelationshipTypeGroup> types = new ArrayList<>();
   }

   /**
    * Represents a collection of relationships of a certain type.
    * If the type is directed, then {@code out} and {@code in} will contain all of the referent's
    * outgoing and incoming relationships, respectively.
    * If the type is undirected, then {@code none} will contain all of the referent's relationships.
    */
   @JsonIgnoreProperties(ignoreUnknown = true)
   public static class RelationshipTypeGroup
   {
      // TODO needs property level documentation
      public String id;
      public String description;
      public boolean directed;
      public DirectionalRelationshipGroup out;
      public DirectionalRelationshipGroup in;
      public DirectionalRelationshipGroup none;
   }

   /**
    * Labeled container for a collection of relationships grouped by type and potentially direction.
    */
   @JsonIgnoreProperties(ignoreUnknown = true)
   public static class DirectionalRelationshipGroup
   {
      public String label;
      // TODO seems like the wrong structure
      public Set<RelationshipSearchResult> relationships = new HashSet<>();
   }
}
