package edu.tamu.tcat.trc.entries.types.reln.search;

import java.util.HashSet;
import java.util.Set;

import edu.tamu.tcat.trc.entries.types.reln.Anchor;
import edu.tamu.tcat.trc.entries.types.reln.AnchorSet;
import edu.tamu.tcat.trc.entries.types.reln.Relationship;
import edu.tamu.tcat.trc.entries.types.reln.dto.AnchorDTO;
import edu.tamu.tcat.trc.entries.types.reln.dto.ProvenanceDTO;

/**
 * JSON serializable summary information about a relationship entry.
 * Intended to be returned when only a brief summary is required to save
 * data transfer and parsing resources.
 */
public class RelnSearchProxy
{
   public String id;
   public String typeId;
   public String description;
   public String descriptionMimeType;
   public ProvenanceDTO provenance;
   public Set<AnchorDTO> relatedEntities;
   public Set<AnchorDTO> targetEntities;

   public static RelnSearchProxy create(Relationship reln)
   {
      RelnSearchProxy result = new RelnSearchProxy();
      result.id = reln.getId();
      result.typeId = reln.getType().getIdentifier();
      result.description = reln.getDescription();
      result.descriptionMimeType = reln.getDescriptionFormat();

      result.provenance = ProvenanceDTO.create(reln.getProvenance());

      AnchorSet related = reln.getRelatedEntities();
      if (related != null)
      {
         result.relatedEntities = new HashSet<>();
         for (Anchor anchor : related.getAnchors())
         {
            result.relatedEntities.add(AnchorDTO.create(anchor));
         }
      }

      AnchorSet target = reln.getTargetEntities();
      if (target != null)
      {
         result.targetEntities = new HashSet<>();
         for (Anchor anchor : target.getAnchors())
         {
            result.targetEntities.add(AnchorDTO.create(anchor));
         }
      }

      return result;
   }
}
