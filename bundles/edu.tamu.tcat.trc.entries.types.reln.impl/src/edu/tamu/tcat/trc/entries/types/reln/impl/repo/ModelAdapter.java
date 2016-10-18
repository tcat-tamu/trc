package edu.tamu.tcat.trc.entries.types.reln.impl.repo;

import java.util.HashSet;
import java.util.Set;

import edu.tamu.tcat.trc.entries.types.reln.Anchor;
import edu.tamu.tcat.trc.entries.types.reln.AnchorSet;
import edu.tamu.tcat.trc.entries.types.reln.Relationship;
import edu.tamu.tcat.trc.entries.types.reln.RelationshipType;
import edu.tamu.tcat.trc.entries.types.reln.dto.AnchorDTO;
import edu.tamu.tcat.trc.entries.types.reln.dto.RelationshipDTO;
import edu.tamu.tcat.trc.entries.types.reln.internal.dto.BasicAnchorSet;
import edu.tamu.tcat.trc.entries.types.reln.internal.dto.BasicRelationship;
import edu.tamu.tcat.trc.entries.types.reln.repo.RelationshipException;
import edu.tamu.tcat.trc.entries.types.reln.repo.RelationshipTypeRegistry;

public class ModelAdapter
{
   public static Relationship adapt(RelationshipDTO data, RelationshipTypeRegistry typeReg)
   {
      if (data == null)
         return null;

      String id = data.id;
      RelationshipType type = null;

      try
      {
         type = typeReg.resolve(data.typeId);
      }
      catch (RelationshipException e)
      {
         throw new IllegalStateException("Registry not found");
      }

      String desc = data.description;
      AnchorSet related = createAnchorSet(data.relatedEntities);
      AnchorSet target = createAnchorSet(data.targetEntities);

      return new BasicRelationship(id, type, desc, related, target);

   }

   private static BasicAnchorSet createAnchorSet(Set<AnchorDTO> entities)
   {
      if (entities.isEmpty())
         return new BasicAnchorSet(new HashSet<>());

      Set<Anchor> anchors = new HashSet<>();
      for (AnchorDTO anchorData : entities)
      {
         anchors.add(AnchorDTO.instantiate(anchorData));
      }

      return new BasicAnchorSet(anchors);
   }
}
