package edu.tamu.tcat.trc.entries.reln.model.internal;

import edu.tamu.tcat.trc.entries.reln.AnchorSet;
import edu.tamu.tcat.trc.entries.reln.Provenance;
import edu.tamu.tcat.trc.entries.reln.Relationship;
import edu.tamu.tcat.trc.entries.reln.RelationshipType;

public class BasicRelationship implements Relationship
{
   private final String id;
   private final RelationshipType type;
   private final String desc;
   private final String descType;
   private final Provenance prov;
   private final AnchorSet related;
   private final AnchorSet target;

   public BasicRelationship(String id, RelationshipType type,
                             String desc, String descType, Provenance prov,
                             AnchorSet related, AnchorSet target) {
      this.id = id;
      this.type = type;
      this.desc = desc;
      this.descType = descType;
      this.prov = prov;
      this.related = related;
      this.target = target;
   }

   @Override
   public String getId()
   {
      return id;
   }

   @Override
   public RelationshipType getType()
   {
      return type;
   }

   @Override
   public String getDescription()
   {
      return desc;
   }

   @Override
   public String getDescriptionFormat()
   {
      return descType;
   }

   @Override
   public Provenance getProvenance()
   {
      return prov;
   }

   @Override
   public AnchorSet getRelatedEntities()
   {
      return related;
   }

   @Override
   public AnchorSet getTargetEntities()
   {
      return target;
   }

}