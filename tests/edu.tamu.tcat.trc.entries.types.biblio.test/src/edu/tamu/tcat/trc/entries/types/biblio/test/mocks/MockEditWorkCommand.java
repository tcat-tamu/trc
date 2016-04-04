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

import edu.tamu.tcat.trc.entries.repo.NoSuchCatalogRecordException;
import edu.tamu.tcat.trc.entries.types.biblio.dto.AuthorReferenceDTO;
import edu.tamu.tcat.trc.entries.types.biblio.dto.EditionDTO;
import edu.tamu.tcat.trc.entries.types.biblio.dto.TitleDTO;
import edu.tamu.tcat.trc.entries.types.biblio.dto.WorkDTO;
import edu.tamu.tcat.trc.entries.types.biblio.repo.EditWorkCommand;
import edu.tamu.tcat.trc.entries.types.biblio.repo.EditionMutator;
import edu.tamu.tcat.trc.repo.IdFactory;

public class MockEditWorkCommand implements EditWorkCommand
{

   private WorkDTO dto;
   private IdFactory idFactory;
   private Consumer<WorkDTO> saveHook;

   public MockEditWorkCommand(WorkDTO dto, IdFactory idFactory, Consumer<WorkDTO> saveHook)
   {
      this.dto = dto;
      this.idFactory = idFactory;
      this.saveHook = saveHook;
   }

   @Override
   public void setAll(WorkDTO work)
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
   public void setAuthors(List<AuthorReferenceDTO> authors)
   {
      dto.authors = new ArrayList<>(authors);
   }

   @Override
   public void setOtherAuthors(List<AuthorReferenceDTO> authors)
   {
      dto.otherAuthors = new ArrayList<>(authors);
   }

   @Override
   public void setTitles(Collection<TitleDTO> titles)
   {
      dto.titles = new HashSet<>(titles);
   }

   @Override
   public EditionMutator createEdition()
   {
      EditionDTO edition = new EditionDTO();
      edition.id = idFactory.getNextId("editions");
      dto.editions.add(edition);

      // create a supplier to generate volume IDs
      return new MockEditionMutator(edition, () -> idFactory.getNextId("volumes"));
   }

   @Override
   public EditionMutator editEdition(String id) throws NoSuchCatalogRecordException
   {
      for (EditionDTO edition : dto.editions) {
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

      for (EditionDTO edition : dto.editions)
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
