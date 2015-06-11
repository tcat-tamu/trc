package edu.tamu.tcat.trc.entries.types.reln.repo;

import java.util.Set;
import java.util.concurrent.Future;

import edu.tamu.tcat.trc.entries.types.reln.AnchorSet;
import edu.tamu.tcat.trc.entries.types.reln.Relationship;
import edu.tamu.tcat.trc.entries.types.reln.RelationshipType;
import edu.tamu.tcat.trc.entries.types.reln.dto.AnchorDTO;
import edu.tamu.tcat.trc.entries.types.reln.dto.ProvenanceDTO;
import edu.tamu.tcat.trc.entries.types.reln.dto.RelationshipDTO;

/**
 * Gives the ability to edit a {@link Relationship}. This will allow the clients
 * to update a {@Relationship} between a set of {@Anchor}'s.
 */
public interface EditRelationshipCommand
{
   /**
    * Set all properties defined in a {@link RelationshipDTO}
    * @param realtionship The Relationship to be edited.
    */
   void setAll(RelationshipDTO realtionship);

   /**
    * Set the {@link RelationshipType} of the {@link Relationship}.
    * @param typeRelationship
    */
   void setType(RelationshipType typeRelationship);

   /**
    * Set the {@link RelationshipType.typeId} for the {@link Relationship}.
    * @param typeId
    */
   void setTypeId(String typeId);

   /**
    *
    * @param description
    */
   void setDescription(String description);

   /**
    *
    * @param descriptionFormat
    */
   void setDescriptionFormat(String descriptionFormat);

   /**
    *
    * @param provenance
    */
   void setProvenance(ProvenanceDTO provenance);

   /**
    *
    * @param related
    */
   void setRelatedEntities(AnchorSet related);

   /**
    * @param related
    */
   void addRelatedEntities(Set<AnchorDTO> related);

   /**
    *
    * @param anchor
    */
   void addRelatedEntity(AnchorDTO anchor);

   /**
    *
    * @param anchor
    */
   void removeRelatedEntity(AnchorDTO anchor);

   /**
    *
    * @param target
    */
   void setTargetEntities(AnchorSet target);

   /**
    * @param target
    */
   void addTargetEntities(Set<AnchorDTO> target);

   /**
    *
    * @param anchor
    */
   void addTargetEntity(AnchorDTO anchor);

   /**
    *
    * @param anchor
    */
   void removeTargetEntity(AnchorDTO anchor);

   /**
    *
    * @return The id of the created {@link Relationship}
    */
   Future<String> execute();
}
