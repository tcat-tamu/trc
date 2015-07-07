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
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;

import edu.tamu.tcat.trc.entries.types.reln.Anchor;
import edu.tamu.tcat.trc.entries.types.reln.AnchorSet;
import edu.tamu.tcat.trc.entries.types.reln.Provenance;
import edu.tamu.tcat.trc.entries.types.reln.Relationship;
import edu.tamu.tcat.trc.entries.types.reln.RelationshipType;
import edu.tamu.tcat.trc.entries.types.reln.URIParseHelper;
import edu.tamu.tcat.trc.entries.types.reln.dto.AnchorDTO;
import edu.tamu.tcat.trc.entries.types.reln.dto.ProvenanceDTO;
import edu.tamu.tcat.trc.entries.types.reln.dto.RelationshipDTO;

/**
 * An encapsulation of adapter methods to convert between the repository API and
 * the {@link RestApiV1} schema DTOs.
 */
public class RepoAdapter
{
   public static RestApiV1.Relationship toDTO(Relationship orig)
   {
      if (orig == null)
         return null;
      RestApiV1.Relationship dto = new RestApiV1.Relationship();
      dto.id = orig.getId();
      dto.typeId = orig.getType().getIdentifier();
      dto.description = orig.getDescription();
      dto.descriptionMimeType = orig.getDescriptionFormat();

      // TODO provide better support for error messaging.
      dto.provenance = toDTO(orig.getProvenance());

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
      dto.descriptionMimeType = orig.descriptionMimeType;

      // TODO provide better support for error messaging.
      dto.provenance = toDTO(orig.provenance);

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

   public static RestApiV1.Provenance toDTO(Provenance orig)
   {
      if (orig == null)
         return null;
      RestApiV1.Provenance dto = new RestApiV1.Provenance();
      Instant created = orig.getDateCreated();
      dto.dateCreated = (created != null) ? DateTimeFormatter.ISO_INSTANT.format(created) : null;

      Instant modified = orig.getDateModified();
      dto.dateModified = (modified != null) ? DateTimeFormatter.ISO_INSTANT.format(modified) : null;

      dto.creatorUris = URIParseHelper.toStringSet(orig.getCreators());

      return dto;
   }

   public static RestApiV1.Provenance toDTO(ProvenanceDTO orig)
   {
      if (orig == null)
         return null;
      RestApiV1.Provenance dto = new RestApiV1.Provenance();
      dto.dateCreated = orig.dateCreated;
      dto.dateModified = orig.dateModified;

      dto.creatorUris = new HashSet<>();
      if (orig.creatorUris != null)
         dto.creatorUris.addAll(orig.creatorUris);

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
      dto.descriptionMimeType = orig.descriptionMimeType;

      dto.provenance = toRepo(orig.provenance);

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

   private static ProvenanceDTO toRepo(RestApiV1.Provenance orig)
   {
      if (orig == null)
         return null;
      ProvenanceDTO dto = new ProvenanceDTO();
      dto.dateCreated = orig.dateCreated;
      dto.dateModified = orig.dateModified;
      if (orig.creatorUris != null)
         dto.creatorUris = new HashSet<>(orig.creatorUris);

      return dto;
   }
}
