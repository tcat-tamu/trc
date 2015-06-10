package edu.tamu.tcat.trc.entries.types.reln.search;

import java.util.HashSet;
import java.util.Set;

import edu.tamu.tcat.trc.entries.types.reln.Anchor;
import edu.tamu.tcat.trc.entries.types.reln.AnchorSet;
import edu.tamu.tcat.trc.entries.types.reln.Relationship;
import edu.tamu.tcat.trc.entries.types.reln.dto.AnchorDV;
import edu.tamu.tcat.trc.entries.types.reln.dto.ProvenanceDV;

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
   public ProvenanceDV provenance;
   public Set<AnchorDV> relatedEntities;
   public Set<AnchorDV> targetEntities;

   public static RelnSearchProxy create(Relationship reln)
   {
      RelnSearchProxy result = new RelnSearchProxy();
      result.id = reln.getId();
      result.typeId = reln.getType().getIdentifier();
      result.description = reln.getDescription();
      result.descriptionMimeType = reln.getDescriptionFormat();

      result.provenance = ProvenanceDV.create(reln.getProvenance());

      AnchorSet related = reln.getRelatedEntities();
      if (related != null)
      {
         result.relatedEntities = new HashSet<>();
         for (Anchor anchor : related.getAnchors())
         {
            result.relatedEntities.add(AnchorDV.create(anchor));
         }
      }

      AnchorSet target = reln.getTargetEntities();
      if (target != null)
      {
         result.targetEntities = new HashSet<>();
         for (Anchor anchor : target.getAnchors())
         {
            result.targetEntities.add(AnchorDV.create(anchor));
         }
      }

      return result;
   }
}
