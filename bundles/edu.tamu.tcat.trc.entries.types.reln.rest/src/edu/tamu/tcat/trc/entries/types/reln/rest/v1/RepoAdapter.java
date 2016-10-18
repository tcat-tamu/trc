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

import static java.util.stream.Collectors.toSet;

import java.net.URI;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import edu.tamu.tcat.trc.entries.types.reln.Anchor;
import edu.tamu.tcat.trc.entries.types.reln.AnchorSet;
import edu.tamu.tcat.trc.entries.types.reln.Relationship;
import edu.tamu.tcat.trc.entries.types.reln.RelationshipType;
import edu.tamu.tcat.trc.entries.types.reln.dto.AnchorDTO;
import edu.tamu.tcat.trc.entries.types.reln.dto.RelationshipDTO;

/**
 * An encapsulation of adapter methods to convert between the repository API and
 * the {@link RestApiV1} schema DTOs.
 */
public class RepoAdapter
{

   public static AnchorSet adapt(Set<RestApiV1.Anchor> dto)
   {
      Set<Anchor> anchors = (dto == null) ? Collections.emptySet() :
         dto.stream()
            .map(RepoAdapter::adapt)
            .collect(toSet());

      return () -> anchors;
   }

   public static Anchor adapt(RestApiV1.Anchor dto)
   {
      Set<URI> uris = dto.entryUris.stream().map(URI::create).collect(toSet());
      return () -> uris;
   }

   public static RestApiV1.Relationship toDTO(Relationship orig)
   {
      if (orig == null)
         return null;
      RestApiV1.Relationship dto = new RestApiV1.Relationship();
      dto.id = orig.getId();
      dto.typeId = orig.getType().getIdentifier();
      dto.description = orig.getDescription();

      AnchorSet related = orig.getRelatedEntities();
      if (related != null)
      {
         dto.relatedEntities = new HashSet<>();
         for (Anchor anchor : related.getAnchors())
         {
            dto.relatedEntities.add(toDTO(anchor));
         }
      }

      AnchorSet target = orig.getTargetEntities();
      if (target != null)
      {
         dto.targetEntities = new HashSet<>();
         for (Anchor anchor : target.getAnchors())
         {
            dto.targetEntities.add(toDTO(anchor));
         }
      }

      return dto;
   }

   public static RestApiV1.Relationship toDTO(RelationshipDTO orig)
   {
      if (orig == null)
         return null;
      RestApiV1.Relationship dto = new RestApiV1.Relationship();
      dto.id = orig.id;
      dto.typeId = orig.typeId;
      dto.description = orig.description;

      if (orig.relatedEntities != null)
      {
         dto.relatedEntities = new HashSet<>();
         for (AnchorDTO anchor : orig.relatedEntities)
         {
            dto.relatedEntities.add(toDTO(anchor));
         }
      }

      if (orig.targetEntities != null)
      {
         dto.targetEntities = new HashSet<>();
         for (AnchorDTO anchor : orig.targetEntities)
         {
            dto.targetEntities.add(toDTO(anchor));
         }
      }

      return dto;
   }

   public static RestApiV1.Anchor toDTO(Anchor orig)
   {
      if (orig == null)
         return null;
      RestApiV1.Anchor dto = new RestApiV1.Anchor();
      dto.entryUris = new HashSet<>();
      for (URI uri : orig.getEntryIds())
      {
         dto.entryUris.add(uri.toASCIIString());
      }

      return dto;
   }

   public static RestApiV1.Anchor toDTO(AnchorDTO orig)
   {
      if (orig == null)
         return null;
      RestApiV1.Anchor dto = new RestApiV1.Anchor();
      if (orig.entryUris != null)
      {
         dto.entryUris = new HashSet<>(orig.entryUris);
      }

      return dto;
   }

   public static RestApiV1.RelationshipType toDTO(RelationshipType orig)
   {
      if (orig == null)
         return null;
      RestApiV1.RelationshipType dto = new RestApiV1.RelationshipType();
      dto.identifier = orig.getIdentifier();
      dto.title = orig.getTitle();
      dto.reverseTitle = orig.getReverseTitle();
      dto.description = orig.getDescription();
      dto.isDirected = orig.isDirected();

      return dto;
   }

   public static RelationshipDTO toRepo(RestApiV1.Relationship orig)
   {
      if (orig == null)
         return null;
      RelationshipDTO dto = new RelationshipDTO();
      dto.id = orig.id;
      dto.typeId = orig.typeId;

      dto.description = orig.description;

      if (orig.relatedEntities != null)
      {
         dto.relatedEntities = new HashSet<>();
         for (RestApiV1.Anchor anchor : orig.relatedEntities)
         {
            dto.relatedEntities.add(toRepo(anchor));
         }
      }

      if (orig.targetEntities != null)
      {
         dto.targetEntities = new HashSet<>();
         for (RestApiV1.Anchor anchor : orig.targetEntities)
         {
            dto.targetEntities.add(toRepo(anchor));
         }
      }

      return dto;
   }

   private static AnchorDTO toRepo(RestApiV1.Anchor orig)
   {
      if (orig == null)
         return null;
      AnchorDTO dto = new AnchorDTO();
      if (orig.entryUris != null)
         dto.entryUris = new HashSet<>(orig.entryUris);

      return dto;
   }

}
