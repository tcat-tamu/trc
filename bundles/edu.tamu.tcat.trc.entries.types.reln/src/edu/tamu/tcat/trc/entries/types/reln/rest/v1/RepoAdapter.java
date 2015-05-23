package edu.tamu.tcat.trc.entries.types.reln.rest.v1;

import java.net.URI;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;

import edu.tamu.tcat.trc.entries.reln.model.URIParseHelper;
import edu.tamu.tcat.trc.entries.types.reln.Anchor;
import edu.tamu.tcat.trc.entries.types.reln.AnchorSet;
import edu.tamu.tcat.trc.entries.types.reln.Provenance;
import edu.tamu.tcat.trc.entries.types.reln.Relationship;
import edu.tamu.tcat.trc.entries.types.reln.RelationshipType;
import edu.tamu.tcat.trc.entries.types.reln.dto.AnchorDV;
import edu.tamu.tcat.trc.entries.types.reln.dto.ProvenanceDV;
import edu.tamu.tcat.trc.entries.types.reln.dto.RelationshipDV;

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
   
   public static RelationshipDV toRepo(RestApiV1.Relationship orig)
   {
      if (orig == null)
         return null;
      RelationshipDV dto = new RelationshipDV();
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

   private static AnchorDV toRepo(RestApiV1.Anchor orig)
   {
      if (orig == null)
         return null;
      AnchorDV dto = new AnchorDV();
      if (orig.entryUris != null)
         dto.entryUris = new HashSet<>(orig.entryUris);

      return dto;
   }

   private static ProvenanceDV toRepo(RestApiV1.Provenance orig)
   {
      if (orig == null)
         return null;
      ProvenanceDV dto = new ProvenanceDV();
      dto.dateCreated = orig.dateCreated;
      dto.dateModified = orig.dateModified;
      if (orig.creatorUris != null)
         dto.creatorUris = new HashSet<>(orig.creatorUris);
      
      return dto;
   }
}
