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

import java.net.URI;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import edu.tamu.tcat.trc.entries.types.reln.RelationshipType;
import edu.tamu.tcat.trc.entries.types.reln.rest.v1.RestApiV1.RelationshipTypeGroup;
import edu.tamu.tcat.trc.entries.types.reln.search.RelnSearchProxy;

/**
 * An encapsulation of adapter methods to convert between the search API and
 * the {@link RestApiV1} schema DTOs.
 */
public class SearchAdapter
{
   private static final Logger logger = Logger.getLogger(SearchAdapter.class.getName());

   public static List<RestApiV1.RelationshipSearchResult> toDTO(List<RelnSearchProxy> origList)
   {
      if (origList == null)
         return null;

      return origList.stream()
            .map(SearchAdapter::toDTO)
            .collect(Collectors.toList());
   }

   public static RestApiV1.RelationshipSearchResult toDTO(RelnSearchProxy orig)
   {
      if (orig == null)
      {
         return null;
      }

      RestApiV1.RelationshipSearchResult dto = new RestApiV1.RelationshipSearchResult();
      dto.id = orig.id;
      dto.description = orig.description;
      dto.descriptionMimeType = orig.descriptionMimeType;
      dto.typeId = orig.typeId;
      dto.provenance = RepoAdapter.toDTO(orig.provenance);

      if (orig.relatedEntities != null)
      {
         dto.relatedEntities = orig.relatedEntities.stream()
               .map(RepoAdapter::toDTO)
               .collect(Collectors.toSet());
      }

      if (orig.targetEntities != null)
      {
         dto.targetEntities = orig.targetEntities.stream()
               .map(RepoAdapter::toDTO)
               .collect(Collectors.toSet());
      }

      return dto;
   }

   /**
    * Arranges a list of relationship search results into a grouped result set relative to the
    * provided referent entry URI.
    * @param referent The URI to the query's base entry
    * @param relns
    * @param lookupType Provides a means to resolve {@link RelationshipType} instances by id
    * @return
    */
   public static RestApiV1.GroupedSearchResultSet groupByType(URI referent, List<RestApiV1.RelationshipSearchResult> relns, Function<String, RelationshipType> lookupType)
   {
      RestApiV1.GroupedSearchResultSet resultSet = new RestApiV1.GroupedSearchResultSet();

      resultSet.referent = referent.toString();

      // group by type id, then transform the groups into RestApiV1.RelationshipTypeGroup instances
      resultSet.types = relns.stream()
         .filter(r -> {
            if (r.typeId == null) {
               logger.log(Level.WARNING, MessageFormat.format("Skipping malformed relationship {0}", r.id));
               return false;
            }

            return true;
         })
         .collect(Collectors.groupingBy(r -> r.typeId)).entrySet().stream()
         .map(e -> groupByDirection(referent, lookupType.apply(e.getKey()), e.getValue()))
         .collect(Collectors.toList());

      return resultSet;
   }

   /**
    * Arranges a list of relationships all belonging to the given type by direction if that type is
    * directed and constructs a new {@link RelationshipTypeGroup}.
    *
    * @param referent Entry reference to establish an "in"/"out" perspective (for directed relationship types)
    * @param type
    * @param relns
    * @return
    */
   public static RestApiV1.RelationshipTypeGroup groupByDirection(URI referent, RelationshipType type, List<RestApiV1.RelationshipSearchResult> relns)
   {
      RestApiV1.RelationshipTypeGroup group = new RestApiV1.RelationshipTypeGroup();

      group.id = type.getIdentifier();
      group.description = type.getDescription();
      group.directed = type.isDirected();

      if (type.isDirected())
      {
         Set<RestApiV1.RelationshipSearchResult> outSet = new HashSet<>();
         group.out = new RestApiV1.DirectionalRelationshipGroup();
         group.out.label = type.getTitle();
         group.out.relationships = outSet;

         Set<RestApiV1.RelationshipSearchResult> inSet = new HashSet<>();
         group.in = new RestApiV1.DirectionalRelationshipGroup();
         group.in.label = type.getReverseTitle();
         group.out.relationships = inSet;

         relns.forEach(reln -> {
            Set<RestApiV1.RelationshipSearchResult> bin = contains(referent, reln.relatedEntities) ? outSet : inSet;
            bin.add(reln);
         });
      }
      else
      {
         Set<RestApiV1.RelationshipSearchResult> relnSet = new HashSet<>();
         group.none = new RestApiV1.DirectionalRelationshipGroup();
         group.none.label = type.getTitle();
         group.none.relationships = relnSet;
         relns.forEach(relnSet::add);
      }

      return group;
   }

   /**
    * Searches for a referent URI in a set of anchors.
    *
    * @param referent
    * @param anchors
    * @return {@code true} if the referent entry (or any sub-entries) are contained within the anchor set.
    */
   private static boolean contains(URI referent, Set<RestApiV1.Anchor> anchors)
   {
      Pattern referentPattern = Pattern.compile("^" + referent.toString() + "(?:/|$)");
      return anchors.stream().anyMatch(a ->
            a.entryUris.stream().anyMatch(uri ->
                  referentPattern.matcher(uri).matches()));
   }
}
