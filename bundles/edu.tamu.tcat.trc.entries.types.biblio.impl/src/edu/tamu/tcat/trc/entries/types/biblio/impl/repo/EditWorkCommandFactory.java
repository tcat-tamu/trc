package edu.tamu.tcat.trc.entries.types.biblio.impl.repo;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import edu.tamu.tcat.trc.entries.types.biblio.dto.AuthorReferenceDTO;
import edu.tamu.tcat.trc.entries.types.biblio.dto.TitleDTO;
import edu.tamu.tcat.trc.entries.types.biblio.impl.repo.DataModelV1.WorkDTO;
import edu.tamu.tcat.trc.entries.types.biblio.repo.BibliographicEntryRepository;
import edu.tamu.tcat.trc.entries.types.biblio.repo.CopyReferenceMutator;
import edu.tamu.tcat.trc.entries.types.biblio.repo.EditBibliographicEntryCommand;
import edu.tamu.tcat.trc.entries.types.biblio.repo.EditionMutator;
import edu.tamu.tcat.trc.repo.BasicChangeSet;
import edu.tamu.tcat.trc.repo.ChangeSet;
import edu.tamu.tcat.trc.repo.ChangeSet.ApplicableChangeSet;
import edu.tamu.tcat.trc.repo.EditCommandFactory;
import edu.tamu.tcat.trc.repo.ExecutableUpdateContext;
import edu.tamu.tcat.trc.repo.id.IdFactory;
import edu.tamu.tcat.trc.repo.id.IdFactoryProvider;

public class EditWorkCommandFactory implements EditCommandFactory<DataModelV1.WorkDTO, EditBibliographicEntryCommand>
{
   private final IdFactoryProvider idFactoryProvider;

   private final IdFactory copyRefIds;
   private final IdFactory editionIds;

   public EditWorkCommandFactory(IdFactoryProvider idFactoryProvider)
   {
      this.idFactoryProvider = idFactoryProvider;

      this.editionIds = idFactoryProvider.getIdFactory(BibliographicEntryRepository.ID_CONTEXT_EDITIONS);
      this.copyRefIds = idFactoryProvider.getIdFactory(BibliographicEntryRepository.ID_CONTEXT_COPIES);
   }

   @Override
   public EditBibliographicEntryCommand create(ExecutableUpdateContext<DataModelV1.WorkDTO> ctx)
   {
      return new EditWorkCommandImpl(ctx);
   }

   @Override
   public WorkDTO initialize(String id, Optional<DataModelV1.WorkDTO> original)
   {
      return original.map(DataModelV1::copy)
         .orElseGet(() -> {
            DataModelV1.WorkDTO dto = new DataModelV1.WorkDTO();
            dto.id = id;
            return dto;
         });
   }

   private class EditWorkCommandImpl implements EditBibliographicEntryCommand
   {
      ExecutableUpdateContext<DataModelV1.WorkDTO> context;

      private final String workId;
      private final ApplicableChangeSet<DataModelV1.WorkDTO> changes = new BasicChangeSet<>();

      public EditWorkCommandImpl(ExecutableUpdateContext<DataModelV1.WorkDTO> ctx)
      {
         this.workId = ctx.getId();
         this.context = ctx;
      }

      private Function<DataModelV1.WorkDTO, DataModelV1.CopyReferenceDTO> makeCopySelector(String id)
      {
         return (dto) -> dto.copyReferences.stream()
               .filter(ref -> Objects.equals(id, ref.id))
               .findFirst()
               .orElseThrow(() -> new IllegalStateException("Cannot find copy reference with id {" + id + "} on work {" + workId + "}."));
      }

      private Function<DataModelV1.WorkDTO, DataModelV1.EditionDTO> makeEditionSelector(String id)
      {
         return (dto) -> dto.editions.stream()
               .filter(ed -> Objects.equals(id, ed.id))
               .findFirst()
               .orElseThrow(() -> new IllegalStateException("Cannot find editon with id {" + id + "} on work {" + workId + "}."));
      }

      @Override
      public String getId()
      {
         return workId;
      }

      @Override
      public void setType(String type)
      {
         changes.add("type", dto -> dto.type = type);
      }

      @Override
      public void setAuthors(List<AuthorReferenceDTO> authors)
      {
         List<DataModelV1.AuthorReferenceDTO> copied = ModelAdapter.adaptAuthors(authors);
         changes.add("authors", dto -> dto.authors = copied);
      }

      @Override
      public void setTitles(Collection<TitleDTO> titles)
      {
         List<DataModelV1.TitleDTO> copied = ModelAdapter.adaptTitles(titles);
         changes.add("titles", dto -> dto.titles = copied);
      }

      @Override
      public void setOtherAuthors(List<AuthorReferenceDTO> otherAuthors)
      {
         List<DataModelV1.AuthorReferenceDTO> copied = ModelAdapter.adaptAuthors(otherAuthors);
         changes.add("authors", dto -> dto.otherAuthors = copied);
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
         // NOTE if the edition does not exist, this will fail at execution time.
         //      It would be nice to fail fast, but we don't have a copy of the object
         //      we are editing.
         ChangeSet<DataModelV1.EditionDTO> edChanges = changes.partial("edition." + id, makeEditionSelector(id));
         return new EditionMutatorImpl(id, edChanges, idFactoryProvider);
      }

      @Override
      public EditionMutator createEdition()
      {
         String id = editionIds.get();
         changes.add("edition." + id + "[create]", dto -> {
            DataModelV1.EditionDTO edition = new DataModelV1.EditionDTO();
            edition.id = id;
            dto.editions.add(edition);
         });

         ChangeSet<DataModelV1.EditionDTO> edChanges = changes.partial("edition." + id, makeEditionSelector(id));
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
            DataModelV1.CopyReferenceDTO ref = new DataModelV1.CopyReferenceDTO();
            ref.id = refId;
            dto.copyReferences.add(ref);
         });

         ChangeSet<DataModelV1.CopyReferenceDTO> refChanges = changes.partial("copy." + refId, makeCopySelector(refId));
         return new CopyReferenceMutatorImpl(refId, refChanges);
      }

      @Override
      public CopyReferenceMutator editCopyReference(String id)
      {
         ChangeSet<DataModelV1.CopyReferenceDTO> refChanges = changes.partial("copy." + id, makeCopySelector(id));
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
            dto.copyReferences.removeIf(ref -> !copyReferenceIds.contains(ref.id));
         });

         // TODO remove this and update API
         return Collections.emptySet();
      }

      @Override
      public CompletableFuture<String> execute()
      {
         return context.update(changes::apply).thenApply(dto -> dto.id);
      }
   }
}