package edu.tamu.tcat.trc.entries.types.reln.postgres;

import static java.text.MessageFormat.format;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.Collectors;

import edu.tamu.tcat.trc.entries.types.reln.AnchorSet;
import edu.tamu.tcat.trc.entries.types.reln.RelationshipType;
import edu.tamu.tcat.trc.entries.types.reln.dto.AnchorDTO;
import edu.tamu.tcat.trc.entries.types.reln.dto.ProvenanceDTO;
import edu.tamu.tcat.trc.entries.types.reln.dto.RelationshipDTO;
import edu.tamu.tcat.trc.entries.types.reln.repo.EditRelationshipCommand;
import edu.tamu.tcat.trc.entries.types.reln.repo.RelationshipException;
import edu.tamu.tcat.trc.entries.types.reln.repo.RelationshipTypeRegistry;
import edu.tamu.tcat.trc.repo.BasicChangeSet;
import edu.tamu.tcat.trc.repo.ChangeSet.ApplicableChangeSet;
import edu.tamu.tcat.trc.repo.EditCommandFactory;
import edu.tamu.tcat.trc.repo.UpdateContext;

public class EditRelationshipCommandFactory implements EditCommandFactory<RelationshipDTO, EditRelationshipCommand>
{
   private final RelationshipTypeRegistry typeReg;

   public EditRelationshipCommandFactory(RelationshipTypeRegistry typeReg)
   {
      this.typeReg = typeReg;
   }

   @Override
   public EditRelationshipCommand create(String id, EditCommandFactory.UpdateStrategy<RelationshipDTO> context)
   {
      return new EditRelationshipCommandImpl(id, context);
   }

   @Override
   public EditRelationshipCommand edit(String id, EditCommandFactory.UpdateStrategy<RelationshipDTO> context)
   {
      return new EditRelationshipCommandImpl(id, context);
   }

   private class EditRelationshipCommandImpl implements EditRelationshipCommand
   {
      EditCommandFactory.UpdateStrategy<RelationshipDTO> context;

      private final String id;
      private final ApplicableChangeSet<RelationshipDTO> changes = new BasicChangeSet<>();


      public EditRelationshipCommandImpl(String id, EditCommandFactory.UpdateStrategy<RelationshipDTO> context)
      {
         this.id = id;
         this.context = context;
      }

      @Override
      public void setAll(RelationshipDTO realtionship)
      {
         throw new UnsupportedOperationException();
      }

      @Override
      public void setType(RelationshipType typeRelationship)
      {
         setTypeId(typeRelationship.getIdentifier());
      }

      @Override
      public void setTypeId(String typeId)
      {
         checkTypeId(typeId, msg -> new IllegalArgumentException(msg));
         changes.add("type id", dto -> dto.typeId = typeId);
      }

      private void checkTypeId(String typeId, Function<String, RuntimeException> generator)
      {
         try
         {
            typeReg.resolve(typeId);
         }
         catch (RelationshipException | NullPointerException e)
         {
            String msg = format("The supplied type id {0} could not be found. This relationship type has not been configured.", typeId);
            throw generator.apply(msg);
         }
      }

      @Override
      public void setDescription(String description)
      {
         changes.add("description", dto -> dto.description = description);
      }

      @Override
      public void setDescriptionFormat(String descriptionFormat)
      {
         changes.add("Mime type", dto -> dto.descriptionMimeType = descriptionFormat);
      }

      @Override
      public void setProvenance(ProvenanceDTO provenance)
      {
         changes.add("Provenance", dto -> dto.provenance = provenance);
      }

      @Override
      public void setRelatedEntities(AnchorSet related)
      {
         if (related == null)
            return;

         changes.add("Set Related Entities", dto -> dto.relatedEntities = related.getAnchors().parallelStream()
                                                                             .map(anchor -> AnchorDTO.create(anchor))
                                                                             .collect(Collectors.toSet()));
      }

      @Override
      public void addRelatedEntities(Set<AnchorDTO> related)
      {
         changes.add("Add Related Entities", dto -> dto.relatedEntities.addAll(related));
      }

      @Override
      public void addRelatedEntity(AnchorDTO anchor)
      {
         changes.add("Add Related Entitie", dto -> dto.relatedEntities.add(anchor));
      }

      @Override
      public void removeRelatedEntity(AnchorDTO anchor)
      {
         changes.add("Remove Related Entitie", dto -> dto.relatedEntities.remove(anchor));
      }

      @Override
      public void setTargetEntities(AnchorSet target)
      {
         if (target == null)
            return;

         changes.add("Set Target Entities", dto -> dto.targetEntities = target.getAnchors().parallelStream()
                                                                          .map(anchor -> AnchorDTO.create(anchor))
                                                                          .collect(Collectors.toSet()));
      }

      @Override
      public void addTargetEntities(Set<AnchorDTO> target)
      {
         changes.add("Add Target Entities", dto -> dto.targetEntities.addAll(target));
      }

      @Override
      public void addTargetEntity(AnchorDTO anchor)
      {
         changes.add("Add Target Entitie", dto -> dto.targetEntities.add(anchor));
      }

      @Override
      public void removeTargetEntity(AnchorDTO anchor)
      {
         changes.add("Remove Target Entitie", dto -> dto.targetEntities.remove(anchor));
      }

      @Override
      public Future<String> execute()
      {
         CompletableFuture<RelationshipDTO> modified = context.update(ctx -> {
             RelationshipDTO dto = preModifiedData(ctx);
             RelationshipDTO result = changes.apply(dto);
             checkTypeId(result.typeId, msg -> new IllegalStateException(msg));

             return result;
         });
         return modified.thenApply(dto -> dto.id);
      }

      private RelationshipDTO preModifiedData(UpdateContext<RelationshipDTO> ctx)
      {
         RelationshipDTO orig = ctx.getOriginal();

         if (orig != null)
            return new RelationshipDTO(orig);

         RelationshipDTO dto = new RelationshipDTO();
         dto.id = this.id;

         return dto;
      }
   }
}
