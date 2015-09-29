/*
 * Copyright 2015 Texas A&M Engineering Experiment Station
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

import org.tamu.tcat.trc.persist.IdFactory;

import edu.tamu.tcat.trc.entries.repo.NoSuchCatalogRecordException;
import edu.tamu.tcat.trc.entries.types.biblio.dto.AuthorRefDV;
import edu.tamu.tcat.trc.entries.types.biblio.dto.EditionDV;
import edu.tamu.tcat.trc.entries.types.biblio.dto.TitleDV;
import edu.tamu.tcat.trc.entries.types.biblio.dto.WorkDV;
import edu.tamu.tcat.trc.entries.types.biblio.repo.EditWorkCommand;
import edu.tamu.tcat.trc.entries.types.biblio.repo.EditionMutator;

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
   public void setType(String type)
   {
      dto.type = type;
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
