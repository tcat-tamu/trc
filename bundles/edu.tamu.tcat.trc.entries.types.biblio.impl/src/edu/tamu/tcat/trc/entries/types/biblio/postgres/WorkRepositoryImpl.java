package edu.tamu.tcat.trc.entries.types.biblio.postgres;

import java.util.concurrent.ExecutionException;

import edu.tamu.tcat.trc.entries.types.biblio.Edition;
import edu.tamu.tcat.trc.entries.types.biblio.Volume;
import edu.tamu.tcat.trc.entries.types.biblio.Work;
import edu.tamu.tcat.trc.entries.types.biblio.repo.EditWorkCommand;
import edu.tamu.tcat.trc.entries.types.biblio.repo.WorkRepository;
import edu.tamu.tcat.trc.repo.DocumentRepository;
import edu.tamu.tcat.trc.repo.IdFactory;
import edu.tamu.tcat.trc.repo.RepositoryException;

public class WorkRepositoryImpl implements WorkRepository
{

   private final DocumentRepository<Work, EditWorkCommand> repoBackend;
   private final IdFactory idFactory;

   public WorkRepositoryImpl(DocumentRepository<Work, EditWorkCommand> repo, IdFactory idFactory)
   {
      this.repoBackend = repo;
      this.idFactory = idFactory;
   }

   @Override
   public Work getWork(String workId)
   {
      try
      {
         return repoBackend.get(workId);
      }
      catch (RepositoryException e)
      {
         throw new IllegalArgumentException("Unable to find work with id {" + workId + "}.", e);
      }
   }

   @Override
   public EditWorkCommand createWork()
   {
      String id = idFactory.get();
      return repoBackend.create(id);
   }

   @Override
   public EditWorkCommand editWork(String workId)
   {
      try
      {
         return repoBackend.edit(workId);
      }
      catch (RepositoryException e)
      {
         throw new IllegalArgumentException("Unable to find work with id {" + workId + "}.", e);
      }
   }

   @Override
   public void deleteWork(String workId)
   {
      try {
         boolean result = repoBackend.delete(workId).get();
         if (!result)
         {
            throw new IllegalArgumentException("Unable to find work with id {" + workId + "}.");
         }
      }
      catch (UnsupportedOperationException | InterruptedException | ExecutionException e) {
         throw new IllegalStateException("Encountered an unexpected error while trying to delete work with id {" + workId + "}.", e);
      }
   }

   @Override
   public Edition getEdition(String workId, String editionId)
   {
      Work work = getWork(workId);
      Edition edition = work.getEdition(editionId);
      if (edition == null)
      {
         throw new IllegalArgumentException("Unable to find edition with id {" + editionId + "} on work {" + workId + "}.");
      }
      return edition;
   }

   @Override
   public Volume getVolume(String workId, String editionId, String volumeId)
   {
      Edition edition = getEdition(workId, editionId);
      Volume volume = edition.getVolume(volumeId);
      if (volume == null)
      {
         throw new IllegalArgumentException("Unable to find volume with id {" + volumeId + "} on edition {" + editionId + "} on work {" + workId + "}.");
      }
      return volume;
   }
}
