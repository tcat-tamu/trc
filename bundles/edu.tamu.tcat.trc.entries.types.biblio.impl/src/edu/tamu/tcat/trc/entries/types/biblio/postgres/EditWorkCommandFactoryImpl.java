package edu.tamu.tcat.trc.entries.types.biblio.postgres;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Future;
import java.util.function.Supplier;

import com.google.common.util.concurrent.JdkFutureAdapters;
import com.google.common.util.concurrent.ListenableFuture;

import edu.tamu.tcat.trc.entries.types.biblio.Work;
import edu.tamu.tcat.trc.entries.types.biblio.dto.AuthorReferenceDTO;
import edu.tamu.tcat.trc.entries.types.biblio.dto.EditionDTO;
import edu.tamu.tcat.trc.entries.types.biblio.dto.TitleDTO;
import edu.tamu.tcat.trc.entries.types.biblio.dto.WorkDTO;
import edu.tamu.tcat.trc.entries.types.biblio.dto.copies.CopyReferenceDTO;
import edu.tamu.tcat.trc.entries.types.biblio.postgres.copies.CopyReferenceMutatorImpl;
import edu.tamu.tcat.trc.entries.types.biblio.repo.EditWorkCommand;
import edu.tamu.tcat.trc.entries.types.biblio.repo.EditionMutator;
import edu.tamu.tcat.trc.entries.types.biblio.repo.copies.CopyReferenceMutator;
import edu.tamu.tcat.trc.entries.types.biblio.search.WorkIndexService;
import edu.tamu.tcat.trc.repo.CommitHook;
import edu.tamu.tcat.trc.repo.EditCommandFactory;
import edu.tamu.tcat.trc.repo.IdFactory;
import edu.tamu.tcat.trc.repo.IdFactoryProvider;

public class EditWorkCommandFactoryImpl implements EditCommandFactory<WorkDTO, EditWorkCommand>
{
   private final IdFactoryProvider idFactoryProvider;
   private final WorkIndexService indexService;

   public EditWorkCommandFactoryImpl(IdFactoryProvider idFactoryProvider, WorkIndexService indexService)
   {
      this.idFactoryProvider = idFactoryProvider;
      this.indexService = indexService;
   }

   @Override
   public EditWorkCommand create(String id, CommitHook<WorkDTO> commitHook)
   {
      // TODO: poke workIndexService when commitHook is called and finished.
      ListeningCommitHookAdapter<WorkDTO> listeningCommitHook = new ListeningCommitHookAdapter<>(commitHook);
      return new EditWorkCommandImpl(id, null, listeningCommitHook);
   }

   @Override
   public EditWorkCommand edit(String id, Supplier<WorkDTO> currentState, CommitHook<WorkDTO> commitHook)
   {
      // TODO: poke workIndexService when commitHook is called and finished.
      ListeningCommitHookAdapter<WorkDTO> listeningCommitHook = new ListeningCommitHookAdapter<>(commitHook);
      return new EditWorkCommandImpl(id, currentState, listeningCommitHook);
   }


   private class EditWorkCommandImpl implements EditWorkCommand
   {
      private final ListeningCommitHookAdapter<WorkDTO> hook;
      private final WorkChangeSet changeSet;

      private final IdFactoryProvider workIdFactoryProvider;
      private final IdFactory editionIdFactory;
      private final IdFactory copyReferenceIdFactory;

      public EditWorkCommandImpl(String id, Supplier<WorkDTO> currentState, ListeningCommitHookAdapter<WorkDTO> hook)
      {
         this.hook = hook;
         // TODO: Do we really want to create new id generators for each work? (edition, volume, etc.)
         this.workIdFactoryProvider = idFactoryProvider.extend(WorkRepositoryImpl.CONTEXT_WORK + "/" + id);
         this.editionIdFactory = workIdFactoryProvider.getIdFactory("editions");
         this.copyReferenceIdFactory = workIdFactoryProvider.getIdFactory("copies");

         this.changeSet = new WorkChangeSet(id);
         if (currentState != null)
         {
            changeSet.original = currentState.get();
            changeSet.editions = changeSet.original.editions;
         }

      }

      @Override
      public String getId()
      {
         return changeSet.id;
      }

      @Override
      public void setAuthors(List<AuthorReferenceDTO> authors)
      {
         changeSet.authors = authors;
      }

      @Override
      public void setTitles(Collection<TitleDTO> titles)
      {
         changeSet.titles = titles;
      }

      @Override
      public void setOtherAuthors(List<AuthorReferenceDTO> authors)
      {
         changeSet.otherAuthors = authors;
      }

      @Override
      public void setSeries(String series)
      {
         changeSet.series = series;
      }

      @Override
      public void setSummary(String summary)
      {
         changeSet.summary = summary;
      }

      @Override
      public EditionMutator editEdition(String id)
      {
         EditionDTO edition = changeSet.editions.stream()
               .filter(ed -> Objects.equals(ed.id, id))
               .findFirst()
               .orElseThrow(() -> new IllegalArgumentException("Cannot find edition with id {" + id + "}."));

         return new EditionMutatorImpl(edition, getEditionsIdFactoryProvider(id));
      }

      private IdFactoryProvider getEditionsIdFactoryProvider(String id)
      {
         return workIdFactoryProvider.extend("editions/" + id + "/");
      }

      @Override
      public EditionMutator createEdition()
      {
         EditionDTO edition = new EditionDTO();
         edition.id = editionIdFactory.get();
         changeSet.editions.add(edition);
         return new EditionMutatorImpl(edition, getEditionsIdFactoryProvider(edition.id));
      }

      @Override
      public void removeEdition(String editionId)
      {
         changeSet.editions.removeIf(edition -> Objects.equals(edition.id, editionId));
      }

      @Override
      public void setDefaultCopyReference(String defaultCopyReferenceId)
      {
         boolean found = false;

         // look in newly added (or modified) copy references
         found = changeSet.newCopyReferences.stream()
               .anyMatch(cr -> Objects.equals(cr.id, defaultCopyReferenceId));

         // look in existing copy references
         if (!found && changeSet.original != null && changeSet.original.copyReferences != null)
         {
            found = changeSet.original.copyReferences.stream()
               .anyMatch(copyReference -> Objects.equals(defaultCopyReferenceId, copyReference.id));
         }

         if (!found)
         {
            throw new IllegalArgumentException("Cannot find copy reference with id {" + defaultCopyReferenceId + "}.");
         }

         changeSet.defaultCopyReferenceId = defaultCopyReferenceId;
      }

      @Override
      public CopyReferenceMutator createCopyReference()
      {
         CopyReferenceDTO copyReference = new CopyReferenceDTO();
         copyReference.id = copyReferenceIdFactory.get();
         changeSet.newCopyReferences.add(copyReference);
         return new CopyReferenceMutatorImpl(copyReference);
      }

      @Override
      public CopyReferenceMutator editCopyReference(String id)
      {
         if (changeSet.original == null || changeSet.original.copyReferences == null)
         {
            throw new IllegalArgumentException("Cannot find copy reference with id {" + id + "}.");
         }

         CopyReferenceDTO original = changeSet.original.copyReferences.stream()
               .filter(ref -> Objects.equals(id, ref.id))
               .findFirst()
               .orElseThrow(() -> new IllegalArgumentException("Cannot find copy reference with id {" + id + "}."));

         changeSet.removedCopyReferences.add(original);
         CopyReferenceDTO modified = CopyReferenceDTO.copy(original);
         changeSet.newCopyReferences.add(modified);

         return new CopyReferenceMutatorImpl(modified);
      }

      @Override
      public void removeCopyReference(String id)
      {
         if (changeSet.original == null || changeSet.original.copyReferences == null)
         {
            throw new IllegalArgumentException("Cannot find copy reference with id {" + id + "}.");
         }

         CopyReferenceDTO copyReference = changeSet.original.copyReferences.stream()
               .filter(ref -> Objects.equals(id, ref.id))
               .findFirst()
               .orElseThrow(() -> new IllegalArgumentException("Cannot find copy reference with id {" + id + "}."));

         changeSet.removedCopyReferences.add(copyReference);
      }

      @Override
      public Future<String> execute()
      {
         WorkDTO data = constructUpdatedData(changeSet.original);

         ListenableFuture<String> future = hook.submit(data, changeSet);

         // HACK: This should be done asynchronously.
         Work work = ModelAdapter.adapt(data);
         indexService.index(work);

         return future;
      }

      private WorkDTO constructUpdatedData(WorkDTO original)
      {
         WorkDTO data = new WorkDTO();

         if (original != null)
         {
            data.copyReferences = original.copyReferences;
         }

         data.id = changeSet.id;
         data.authors = changeSet.authors;
         data.titles = changeSet.titles;
         data.otherAuthors = changeSet.otherAuthors;
         data.series = changeSet.series;
         data.summary = changeSet.summary;
         data.defaultCopyReferenceId = changeSet.defaultCopyReferenceId;

         // HACK: it would be nice to have added/removed granularity, but order should be preserved.
         data.editions = changeSet.editions;

         data.copyReferences.removeAll(changeSet.removedCopyReferences);
         data.copyReferences.addAll(changeSet.newCopyReferences);

         return data;
      }
   }

   static class ListeningCommitHookAdapter<T> implements CommitHook<T>
   {
      private final CommitHook<T> delegate;

      public ListeningCommitHookAdapter(CommitHook<T> delegate)
      {
         this.delegate = delegate;
      }

      @Override
      public ListenableFuture<String> submit(T data, Object changeSet)
      {
         Future<String> future = delegate.submit(data, changeSet);
         return JdkFutureAdapters.listenInPoolThread(future);
      }

   }
}