package edu.tamu.tcat.trc.entries.types.biblio.test.mocks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import edu.tamu.tcat.catalogentries.IdFactory;
import edu.tamu.tcat.catalogentries.NoSuchCatalogRecordException;
import edu.tamu.tcat.trc.entries.bib.EditWorkCommand;
import edu.tamu.tcat.trc.entries.bib.EditionMutator;
import edu.tamu.tcat.trc.entries.bib.dto.AuthorRefDV;
import edu.tamu.tcat.trc.entries.bib.dto.EditionDV;
import edu.tamu.tcat.trc.entries.bib.dto.TitleDV;
import edu.tamu.tcat.trc.entries.bib.dto.WorkDV;

public class MockEditWorkCommand implements EditWorkCommand
{

   private WorkDV dto;
   private IdFactory idFactory;
   private Consumer<WorkDV> saveHook;

   public MockEditWorkCommand(WorkDV dto, IdFactory idFactory, Consumer<WorkDV> saveHook)
   {
      this.dto = dto;
      this.idFactory = idFactory;
      this.saveHook = saveHook;
   }

   @Override
   public void setAll(WorkDV work)
   {
      // TODO Auto-generated method stub

   }

   @Override
   public void setSeries(String series)
   {
      dto.series = series;
   }

   @Override
   public void setSummary(String summary)
   {
      dto.summary = summary;
   }

   @Override
   public void setAuthors(List<AuthorRefDV> authors)
   {
      dto.authors = new ArrayList<>(authors);
   }

   @Override
   public void setOtherAuthors(List<AuthorRefDV> authors)
   {
      dto.otherAuthors = new ArrayList<>(authors);
   }

   @Override
   public void setTitles(Collection<TitleDV> titles)
   {
      dto.titles = new HashSet<>(titles);
   }

   @Override
   public EditionMutator createEdition()
   {
      EditionDV edition = new EditionDV();
      edition.id = idFactory.getNextId("editions");
      dto.editions.add(edition);

      // create a supplier to generate volume IDs
      return new MockEditionMutator(edition, () -> idFactory.getNextId("volumes"));
   }

   @Override
   public EditionMutator editEdition(String id) throws NoSuchCatalogRecordException
   {
      for (EditionDV edition : dto.editions) {
         if (edition.id.equals(id)) {
            // create a supplier to generate volume IDs
            return new MockEditionMutator(edition, () -> idFactory.getNextId("volumes"));
         }
      }

      throw new NoSuchCatalogRecordException("Unable to find edition with id [" + id + "].");
   }

   @Override
   public void removeEdition(String editionId) throws NoSuchCatalogRecordException
   {
      if (dto.editions.isEmpty())
         throw new NoSuchCatalogRecordException("This work does not contain any editons.");

      for (EditionDV edition : dto.editions)
      {
         if(edition.id.equals(editionId))
         {
            dto.editions.remove(edition);
            return;
         }
      }

      throw new NoSuchCatalogRecordException("Could not find the edition [" + editionId + "]. The edition could not be removed.");
   }

   @Override
   public void removeVolume(String volumeId) throws NoSuchCatalogRecordException
   {
      // TODO Auto-generated method stub
   }

   @Override
   public Future<String> execute()
   {
      saveHook.accept(dto);
      return new Future<String>()
      {
         @Override
         public boolean cancel(boolean mayInterruptIfRunning)
         {
            return false;
         }

         @Override
         public boolean isCancelled()
         {
            return false;
         }

         @Override
         public boolean isDone()
         {
            return true;
         }

         @Override
         public String get() throws InterruptedException, ExecutionException
         {
            return dto.id;
         }

         @Override
         public String get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException
         {
            return dto.id;
         }
      };
   }
}
