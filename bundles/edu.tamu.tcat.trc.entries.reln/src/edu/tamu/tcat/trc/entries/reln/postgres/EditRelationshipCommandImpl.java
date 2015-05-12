package edu.tamu.tcat.trc.entries.reln.postgres;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.Collectors;

import edu.tamu.tcat.catalogentries.IdFactory;
import edu.tamu.tcat.trc.entries.reln.Anchor;
import edu.tamu.tcat.trc.entries.reln.AnchorSet;
import edu.tamu.tcat.trc.entries.reln.EditRelationshipCommand;
import edu.tamu.tcat.trc.entries.reln.RelationshipType;
import edu.tamu.tcat.trc.entries.reln.model.AnchorDV;
import edu.tamu.tcat.trc.entries.reln.model.ProvenanceDV;
import edu.tamu.tcat.trc.entries.reln.model.RelationshipDV;
import edu.tamu.tcat.trc.entries.reln.model.internal.BasicAnchorSet;


public class EditRelationshipCommandImpl implements EditRelationshipCommand
{
   private final RelationshipDV relationship;

   private Function<RelationshipDV, Future<String>> commitHook;

   public EditRelationshipCommandImpl(RelationshipDV relationship, IdFactory idFactory)
   {
      this.relationship = relationship;
   }

   public void setCommitHook(Function<RelationshipDV, Future<String>> hook)
   {
      commitHook = hook;
   }

   @Override
   public void setAll(RelationshipDV relationship)
   {
       setTypeId(relationship.typeId);
       setDescription(relationship.description);
       setDescriptionFormat(relationship.descriptionMimeType);
       setProvenance(relationship.provenance);
       setTargetEntities(createAnchorSet(relationship.targetEntities));
       setRelatedEntities(createAnchorSet(relationship.relatedEntities));
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

   @Override
   public void setTypeId(String typeId)
   {
      relationship.typeId = typeId;
   }

   @Override
   public void setType(RelationshipType typeRelationship)
   {
      // TODO
   }

   @Override
   public void setDescription(String description)
   {
      relationship.description = description;
   }

   @Override
   public void setDescriptionFormat(String descriptionFormat)
   {
      relationship.descriptionMimeType = descriptionFormat;
   }

   @Override
   public void setProvenance(ProvenanceDV provenance)
   {
      relationship.provenance = provenance;
   }

   @Override
   public void setRelatedEntities(AnchorSet related)
   {
      if (related == null)
         return;

      relationship.relatedEntities = related.getAnchors().parallelStream()
                       .map(anchor -> AnchorDV.create(anchor))
                       .collect(Collectors.toSet());
   }

   @Override
   public void addRelatedEntity(AnchorDV anchor)
   {
      relationship.relatedEntities.add(anchor);
   }

   @Override
   public void addRelatedEntities(Set<AnchorDV> anchor)
   {
      relationship.relatedEntities.addAll(anchor);
   }

   @Override
   public void removeRelatedEntity(AnchorDV anchor)
   {
      relationship.relatedEntities.remove(anchor);
   }

   @Override
   public void setTargetEntities(AnchorSet target)
   {
      if (target == null)
         return;

      relationship.targetEntities = target.getAnchors().parallelStream()
            .map(anchor -> AnchorDV.create(anchor))
            .collect(Collectors.toSet());
   }

   @Override
   public void addTargetEntity(AnchorDV anchor)
   {
      relationship.targetEntities.add(anchor);
   }

   @Override
   public void addTargetEntities(Set<AnchorDV> anchor)
   {
      relationship.targetEntities.addAll(anchor);
   }

   @Override
   public void removeTargetEntity(AnchorDV anchor)
   {
      relationship.targetEntities.remove(anchor);
   }

   @Override
   public Future<String> execute()
   {
      Objects.requireNonNull(commitHook, "");

      return commitHook.apply(relationship);
   }

}
