package edu.tamu.tcat.trc.entries.types.biblio.postgres;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.function.Function;

import edu.tamu.tcat.trc.entries.types.biblio.dto.AuthorReferenceDTO;
import edu.tamu.tcat.trc.entries.types.biblio.dto.CopyReferenceDTO;
import edu.tamu.tcat.trc.entries.types.biblio.dto.EditionDTO;
import edu.tamu.tcat.trc.entries.types.biblio.dto.TitleDTO;
import edu.tamu.tcat.trc.entries.types.biblio.dto.WorkDTO;
import edu.tamu.tcat.trc.entries.types.biblio.repo.CopyReferenceMutator;
import edu.tamu.tcat.trc.entries.types.biblio.repo.EditWorkCommand;
import edu.tamu.tcat.trc.entries.types.biblio.repo.EditionMutator;
import edu.tamu.tcat.trc.entries.types.biblio.search.WorkIndexService;
import edu.tamu.tcat.trc.repo.BasicChangeSet;
import edu.tamu.tcat.trc.repo.ChangeSet;
import edu.tamu.tcat.trc.repo.EditCommandFactory;
import edu.tamu.tcat.trc.repo.IdFactory;
import edu.tamu.tcat.trc.repo.IdFactoryProvider;
import edu.tamu.tcat.trc.repo.UpdateContext;

public class EditWorkCommandFactoryImpl implements EditCommandFactory<WorkDTO, EditWorkCommand>
{
   private final IdFactoryProvider idFactoryProvider;
   private final WorkIndexService indexService;

   private final IdFactory copyRefIds;
   private final IdFactory editionIds;

   public EditWorkCommandFactoryImpl(IdFactoryProvider idFactoryProvider, WorkIndexService indexService)
   {
      this.idFactoryProvider = idFactoryProvider;
      this.indexService = indexService;

      this.editionIds = idFactoryProvider.getIdFactory(WorkRepositoryImpl.ID_CONTEXT_EDITIONS);
      this.copyRefIds = idFactoryProvider.getIdFactory(WorkRepositoryImpl.ID_CONTEXT_COPIES);
   }

   @Override
   public EditWorkCommand create(String id, EditCommandFactory.UpdateStrategy<WorkDTO> context)
   {
      return new EditWorkCommandImpl(id, context);
   }

   @Override
   public EditWorkCommand edit(String id, EditCommandFactory.UpdateStrategy<WorkDTO> context)
   {
      return new EditWorkCommandImpl(id, context);
   }

   private class EditWorkCommandImpl implements EditWorkCommand
   {
      EditCommandFactory.UpdateStrategy<WorkDTO> context;

      private final String workId;
      private final ChangeSet<WorkDTO> changes = new BasicChangeSet<>();

      public EditWorkCommandImpl(String id, EditCommandFactory.UpdateStrategy<WorkDTO> context)
      {
         this.workId = id;
         this.context = context;
      }

      private Function<WorkDTO, CopyReferenceDTO> makeCopySelector(String id)
      {
         return (dto) -> dto.copyReferences.stream()
               .filter(ref -> Objects.equals(id, ref.id))
               .findFirst()
               .orElseThrow(() -> new IllegalStateException("Cannot find copy reference with id {" + id + "}."));
      }

      private Function<WorkDTO, EditionDTO> makeEditionSelector(String id)
      {
         return (dto) -> dto.editions.stream()
               .filter(ed -> Objects.equals(id, ed.id))
               .findFirst()
               .orElseThrow(() -> new IllegalStateException("Cannot find editon with id {" + id + "}."));
      }

      @Override
      public String getId()
      {
         return workId;
      }

      @Override
      @Deprecated
      public void setType(String type)
      {
         changes.add("type", dto -> dto.type = type);
      }

      @Override
      public void setAuthors(List<AuthorReferenceDTO> authors)
      {
         changes.add("authors", dto -> dto.authors = authors);
      }

      @Override
      public void setTitles(Collection<TitleDTO> titles)
      {
         changes.add("titles", dto -> dto.titles = titles);
      }

      @Override
      public void setOtherAuthors(List<AuthorReferenceDTO> authors)
      {
         throw new UnsupportedOperationException();
      }

      @Override
      public void setSeries(String series)
      {
         changes.add("series", dto -> dto.series = series);
      }

      @Override
      public void setSummary(String summary)
      {
         changes.add("summary", dto -> dto.summary = summary);
      }

      @Override
      public EditionMutator editEdition(String id)
      {
         ChangeSet<EditionDTO> edChanges = changes.partial("edition." + id, makeEditionSelector(id));
         return new EditionMutatorImpl(id, edChanges, idFactoryProvider);
      }

      @Override
      public EditionMutator createEdition()
      {
         String id = editionIds.get();
         changes.add("edition." + id + "[create]", dto -> {
            EditionDTO edition = new EditionDTO();
            edition.id = id;
            dto.editions.add(edition);
         });

         ChangeSet<EditionDTO> edChanges = changes.partial("edition." + id, makeEditionSelector(id));
         return new EditionMutatorImpl(id, edChanges, idFactoryProvider);
      }

      @Override
      public void removeEdition(String editionId)
      {
         changes.add("edition [remove]", dto ->
            dto.editions.removeIf(edition -> Objects.equals(edition.id, editionId))
         );
      }

      @Override
      public Set<String> retainAllEditions(Set<String> editionIds)
      {
         Objects.requireNonNull(editionIds);

         changes.add("edition [retain some]", dto -> {
            dto.editions.removeIf(edition -> !editionIds.contains(edition.id));
         });

         // FIXME violates contract. Need to rethink API
         return Collections.emptySet();
      }

      @Override
      public void setDefaultCopyReference(String refId)
      {
         changes.add("defaultCopy", dto -> {
            dto.defaultCopyReferenceId = refId;
         });
      }

      @Override
      public CopyReferenceMutator createCopyReference()
      {
         String refId = copyRefIds.get();
         changes.add("copy." + refId + " [create]", dto -> {
            CopyReferenceDTO ref = new CopyReferenceDTO();
            ref.id = refId;
            dto.copyReferences.add(ref);
         });

         ChangeSet<CopyReferenceDTO> refChanges = changes.partial("copy." + refId, makeCopySelector(refId));
         return new CopyReferenceMutatorImpl(refId, refChanges);
      }

      @Override
      public CopyReferenceMutator editCopyReference(String id)
      {
         ChangeSet<CopyReferenceDTO> refChanges = changes.partial("copy." + id, makeCopySelector(id));
         return new CopyReferenceMutatorImpl(id, refChanges);
      }

      @Override
      public void removeCopyReference(String id)
      {
         Objects.requireNonNull(id, "Must supply non-null id");

         changes.add("copy." + id + " [remove]", dto -> {
            dto.copyReferences.removeIf(ref -> !id.equals(ref.id));
         });
      }

      @Override
      public Set<String> retainAllCopyReferences(Set<String> copyReferenceIds)
      {
         changes.add("copy" + "[retain some]", dto -> {
            dto.copyReferences.removeIf(ref -> copyReferenceIds.contains(ref.id));
         });

         // TODO remove this and update API
         return Collections.emptySet();
      }

      @Override
      public Future<String> execute()
      {
         CompletableFuture<WorkDTO> modified = context.update(ctx -> {
            WorkDTO dto = prepModifiedData(ctx);
            return changes.apply(dto);
         });

         return modified
               .thenApply(this::index)             // TODO move to plugable task listening on repo
               .thenApply(dto -> dto.id);
      }

      private WorkDTO prepModifiedData(UpdateContext<WorkDTO> ctx)
      {
         WorkDTO dto = null;
         WorkDTO original = ctx.getOriginal();
         if (original == null)
         {
            dto = new WorkDTO();
            dto.id = this.workId;
         }
         else
         {
            dto = new WorkDTO(original);
         }
         return dto;
      }

      public WorkDTO index(WorkDTO dto)
      {
         // TODO do we really need to adapt?
         if (indexService != null)
            indexService.index(ModelAdapter.adapt(dto));

         return dto;
      }
   }
}