package edu.tamu.tcat.trc.entries.types.reln.impl.repo;

import static java.text.MessageFormat.format;
import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import edu.tamu.tcat.trc.entries.types.reln.RelationshipException;
import edu.tamu.tcat.trc.entries.types.reln.RelationshipType;
import edu.tamu.tcat.trc.entries.types.reln.impl.repo.DataModelV1.Anchor;
import edu.tamu.tcat.trc.entries.types.reln.impl.repo.DataModelV1.Relationship;
import edu.tamu.tcat.trc.entries.types.reln.repo.AnchorMutator;
import edu.tamu.tcat.trc.entries.types.reln.repo.EditRelationshipCommand;
import edu.tamu.tcat.trc.entries.types.reln.repo.RelationshipTypeRegistry;
import edu.tamu.tcat.trc.repo.BasicChangeSet;
import edu.tamu.tcat.trc.repo.ChangeSet;
import edu.tamu.tcat.trc.repo.ChangeSet.ApplicableChangeSet;
import edu.tamu.tcat.trc.repo.EditCommandFactory;
import edu.tamu.tcat.trc.repo.ExecutableUpdateContext;
import edu.tamu.tcat.trc.resolver.EntryId;
import edu.tamu.tcat.trc.resolver.EntryResolverRegistry;

public class EditRelationshipCommandFactory implements EditCommandFactory<DataModelV1.Relationship, EditRelationshipCommand>
{
   private final RelationshipTypeRegistry typeReg;
   private final EntryResolverRegistry resolvers;

   public EditRelationshipCommandFactory(RelationshipTypeRegistry typeReg, EntryResolverRegistry resolvers)
   {
      this.typeReg = typeReg;
      this.resolvers = resolvers;
   }

   @Override
   public EditRelationshipCommand create(ExecutableUpdateContext<DataModelV1.Relationship> ctx)
   {
      return new EditRelationshipCommandImpl(ctx);
   }

   @Override
   public Relationship initialize(String id, Optional<DataModelV1.Relationship> original)
   {
      return original.map(EditRelationshipCommandFactory::clone)
               .orElseGet(() -> {
                  DataModelV1.Relationship dto = new DataModelV1.Relationship();
                  dto.id = id;

                  return dto;

               });
   }

   private static DataModelV1.Relationship clone(DataModelV1.Relationship orig)
   {
      DataModelV1.Relationship dto = new DataModelV1.Relationship();
      dto.id = orig.id;
      dto.typeId = orig.typeId;
      dto.description = orig.description;
      dto.related = orig.related.stream().map(EditRelationshipCommandFactory::clone).collect(toList());
      dto.targets = orig.targets.stream().map(EditRelationshipCommandFactory::clone).collect(toList());

      return dto;
   }

   private static DataModelV1.Anchor clone(DataModelV1.Anchor orig)
   {
      DataModelV1.Anchor dto = new DataModelV1.Anchor();
      dto.ref = orig.ref;
      dto.properties = new HashMap<>(orig.properties);

      return dto;

   }
   private class EditRelationshipCommandImpl implements EditRelationshipCommand
   {
      private final ExecutableUpdateContext<DataModelV1.Relationship> context;

      private final ApplicableChangeSet<DataModelV1.Relationship> changes = new BasicChangeSet<>();


      public EditRelationshipCommandImpl(ExecutableUpdateContext<DataModelV1.Relationship> ctx)
      {
         this.context = ctx;
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
         changes.add("typeId", dto -> dto.typeId = typeId);
      }

      @Override
      public void setDescription(String description)
      {
         changes.add("description", dto -> dto.description = description);
      }

      @Override
      public AnchorMutator editRelatedEntry(EntryId ref)
      {
         String token = resolvers.tokenize(ref);
         ChangeSet<Anchor> partial = changes.partial(format("related [EDIT {0}]", token), (dto) -> {
            return dto.related.stream()
                        .filter(a -> a.ref.equals(token))
                        .findAny()
                        .map(q -> { System.out.println("Found: " + token); return q; } )
                        .orElseGet(() -> makeAnchor(dto.related, token));
         });

         return new AnchorMutatorImpl(partial);
      }

      @Override
      public void removeRelatedEntry(EntryId ref)
      {
         String token = resolvers.tokenize(ref);
         changes.add(format("related [REMOVE {0}]", token), (dto) -> {
                  dto.related = dto.related.stream()
                     .filter(a -> !a.ref.equals(token))
                     .collect(toList());
               });
      }

      @Override
      public void clearRelatedEntries()
      {
         changes.add("related [CLEAR]", dto -> dto.related = new ArrayList<>());
      }

      @Override
      public AnchorMutator editTargetEntry(EntryId ref)
      {
         String token = resolvers.tokenize(ref);
         ChangeSet<Anchor> partial = changes.partial(format("targets [EDIT {0}]", token), (dto) -> {
            return dto.targets.stream()
                        .filter(a -> a.ref.equals(token))
                        .findAny()
                        .orElseGet(() -> makeAnchor(dto.targets, token));
         });

         return new AnchorMutatorImpl(partial);
      }

      @Override
      public void removeTargetEntry(EntryId ref)
      {
         String token = resolvers.tokenize(ref);
         changes.add(format("targets [REMOVE {0}]", token), (dto) -> {
                  dto.targets = dto.targets.stream()
                     .filter(a -> !a.ref.equals(token))
                     .collect(toList());
               });
      }

      @Override
      public void clearTargetEntries()
      {
         changes.add("targets [CLEAR]", dto -> dto.targets = new ArrayList<>());
      }

      @Override
      public CompletableFuture<String> execute()
      {
         return context.update(dto -> {
            DataModelV1.Relationship result = changes.apply(dto);
            checkTypeId(result.typeId, msg -> new RelationshipException(msg));

            return result;
         }).thenApply(dto -> dto.id);
      }

      private void checkTypeId(String typeId, Function<String, RuntimeException> generator)
      {
         try
         {
            Objects.requireNonNull(typeId);
            typeReg.resolve(typeId);
         }
         catch (RelationshipException | NullPointerException e)
         {
            String msg = "The supplied type id {0} could not be found. This relationship type has not been configured.";
            throw generator.apply(format(msg, typeId));
         }
      }

      private DataModelV1.Anchor makeAnchor(List<DataModelV1.Anchor> anchors, String token)
      {
         DataModelV1.Anchor dto = new DataModelV1.Anchor();
         dto.ref = token;
         anchors.add(dto);
         return dto;
      }


   }
}
