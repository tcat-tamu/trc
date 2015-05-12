package edu.tamu.tcat.trc.entries.reln;

import java.util.Set;
import java.util.concurrent.Future;

import edu.tamu.tcat.trc.entries.reln.model.AnchorDV;
import edu.tamu.tcat.trc.entries.reln.model.ProvenanceDV;
import edu.tamu.tcat.trc.entries.reln.model.RelationshipDV;

/**
 * Gives the ability to edit a {@link Relationship}. This will allow the clients
 * to update a {@Relationship} between a set of {@Anchor}'s.
 */
public interface EditRelationshipCommand
{
   /**
    * Set all properties defined in a {@link RelationshipDV}
    * @param realtionship The Relationship to be edited.
    */
   void setAll(RelationshipDV realtionship);

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
   void setProvenance(ProvenanceDV provenance);

   /**
    *
    * @param related
    */
   void setRelatedEntities(AnchorSet related);

   /**
    * @param related
    */
   void addRelatedEntities(Set<AnchorDV> related);

   /**
    *
    * @param anchor
    */
   void addRelatedEntity(AnchorDV anchor);

   /**
    *
    * @param anchor
    */
   void removeRelatedEntity(AnchorDV anchor);

   /**
    *
    * @param target
    */
   void setTargetEntities(AnchorSet target);

   /**
    * @param target
    */
   void addTargetEntities(Set<AnchorDV> target);

   /**
    *
    * @param anchor
    */
   void addTargetEntity(AnchorDV anchor);

   /**
    *
    * @param anchor
    */
   void removeTargetEntity(AnchorDV anchor);

   /**
    *
    * @return The id of the created {@link Relationship}
    */
   Future<String> execute();
}
