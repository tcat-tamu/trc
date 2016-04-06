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
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import edu.tamu.tcat.trc.entries.types.biblio.dto.AuthorReferenceDTO;
import edu.tamu.tcat.trc.entries.types.biblio.dto.EditionDTO;
import edu.tamu.tcat.trc.entries.types.biblio.dto.TitleDTO;
import edu.tamu.tcat.trc.entries.types.biblio.dto.WorkDTO;
import edu.tamu.tcat.trc.entries.types.biblio.repo.EditWorkCommand;
import edu.tamu.tcat.trc.entries.types.biblio.repo.EditionMutator;
import edu.tamu.tcat.trc.entries.types.biblio.repo.copies.CopyReferenceMutator;
import edu.tamu.tcat.trc.repo.IdFactory;
import edu.tamu.tcat.trc.repo.IdFactoryProvider;

public class MockEditWorkCommand implements EditWorkCommand
{

   private WorkDTO dto;
   private IdFactoryProvider idFactoryProvider;
   private IdFactory editionIdFactory;
   private Consumer<WorkDTO> saveHook;

   public MockEditWorkCommand(WorkDTO dto, IdFactoryProvider idFactoryProvider, Consumer<WorkDTO> saveHook)
   {
      this.dto = dto;
      this.idFactoryProvider = idFactoryProvider;
      this.editionIdFactory = idFactoryProvider.getIdFactory("editions");
      this.saveHook = saveHook;
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
      edition.id = editionIdFactory.get();
      dto.editions.add(edition);

      // create a supplier to generate volume IDs
      return new MockEditionMutator(edition, idFactoryProvider.extend(edition.id));
   }

   @Override
   public EditionMutator editEdition(String id)
   {
      for (EditionDTO edition : dto.editions) {
         if (edition.id.equals(id)) {
            // create a supplier to generate volume IDs
            return new MockEditionMutator(edition, idFactoryProvider.extend(id));
         }
      }

      throw new IllegalArgumentException("Unable to find edition with id [" + id + "].");
   }

   @Override
   public void removeEdition(String editionId)
   {
      if (dto.editions.isEmpty())
         throw new IllegalArgumentException("This work does not contain any editons.");

      for (EditionDTO edition : dto.editions)
      {
         if(edition.id.equals(editionId))
         {
            dto.editions.remove(edition);
            return;
         }
      }

      throw new IllegalArgumentException("Could not find the edition [" + editionId + "]. The edition could not be removed.");
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

   @Override
   public String getId()
   {
      return dto.id;
   }

   @Override
   public void setDefaultCopyReference(String defaultCopyReferenceId)
   {
      // TODO Auto-generated method stub
   }

   @Override
   public CopyReferenceMutator editCopyReference(String id)
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public CopyReferenceMutator createCopyReference()
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public void removeCopyReference(String id)
   {
      // TODO Auto-generated method stub
   }

   @Override
   public void retainAllEditions(Set<String> editionIds)
   {
      // TODO Auto-generated method stub

   }

   @Override
   public void retainAllCopyReferences(Set<String> copyReferenceIds)
   {
      // TODO Auto-generated method stub

   }
}
