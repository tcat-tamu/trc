package edu.tamu.tcat.trc.entries.reln.model;

import java.util.HashSet;
import java.util.Set;

import edu.tamu.tcat.trc.entries.reln.Anchor;
import edu.tamu.tcat.trc.entries.reln.AnchorSet;
import edu.tamu.tcat.trc.entries.reln.Provenance;
import edu.tamu.tcat.trc.entries.reln.Relationship;
import edu.tamu.tcat.trc.entries.reln.RelationshipException;
import edu.tamu.tcat.trc.entries.reln.RelationshipType;
import edu.tamu.tcat.trc.entries.reln.RelationshipTypeRegistry;
import edu.tamu.tcat.trc.entries.reln.model.internal.BasicAnchorSet;
import edu.tamu.tcat.trc.entries.reln.model.internal.BasicProvenance;
import edu.tamu.tcat.trc.entries.reln.model.internal.BasicRelationship;

public class RelationshipDV
{
   public String id;
   public String typeId;
   public String description;
   public String descriptionMimeType;
   public ProvenanceDV provenance;
   public Set<AnchorDV> relatedEntities = new HashSet<>();
   public Set<AnchorDV> targetEntities = new HashSet<>();

   public static RelationshipDV create(Relationship reln)
   {
      RelationshipDV result = new RelationshipDV();
      result.id = reln.getId();
      result.typeId = reln.getType().getIdentifier();
      result.description = reln.getDescription();
      result.descriptionMimeType = reln.getDescriptionFormat();

      // TODO provide better support for error messaging.
      result.provenance = ProvenanceDV.create(reln.getProvenance());

      AnchorSet related = reln.getRelatedEntities();
      if (related != null)
      {
         for (Anchor anchor : related.getAnchors())
         {
            result.relatedEntities.add(AnchorDV.create(anchor));
         }
      }

      AnchorSet target = reln.getTargetEntities();
      if (target != null)
      {
         for (Anchor anchor : target.getAnchors())
         {
            result.targetEntities.add(AnchorDV.create(anchor));
         }
      }

      return result;
   }

   /**
    *
    * @param data
    * @param registry
    * @return
    * @throws RelationshipException If the supplied data cannot be parsed into a valid {@link Relationship}.
    */
   public static Relationship instantiate(RelationshipDV data, RelationshipTypeRegistry registry) throws RelationshipException
   {
      String id = data.id;
      RelationshipType type = registry.resolve(data.typeId);
      String desc = data.description;
      String descType = data.descriptionMimeType;
      Provenance prov = (data.provenance != null) ? ProvenanceDV.instantiate(data.provenance) : new BasicProvenance();
      AnchorSet related = createAnchorSet(data.relatedEntities);
      AnchorSet target = createAnchorSet(data.targetEntities);

      return new BasicRelationship(id, type, desc, descType, prov, related, target);
   }

   private static BasicAnchorSet createAnchorSet(Set<AnchorDV> entities)
   {
      if (entities.isEmpty())
         return new BasicAnchorSet(new HashSet<>());

      Set<Anchor> anchors = new HashSet<>();
      for (AnchorDV anchorData : entities)
      {
         anchors.add(AnchorDV.instantiate(anchorData));
      }

      return new BasicAnchorSet(anchors);
   }
}
