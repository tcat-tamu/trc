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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import edu.tamu.tcat.trc.entries.types.biblio.Edition;
import edu.tamu.tcat.trc.entries.types.biblio.Volume;
import edu.tamu.tcat.trc.entries.types.biblio.Work;
import edu.tamu.tcat.trc.entries.types.biblio.dto.WorkDTO;
import edu.tamu.tcat.trc.entries.types.biblio.postgres.ModelAdapter;
import edu.tamu.tcat.trc.entries.types.biblio.repo.EditWorkCommand;
import edu.tamu.tcat.trc.entries.types.biblio.repo.WorkRepository;
import edu.tamu.tcat.trc.repo.IdFactory;
import edu.tamu.tcat.trc.repo.IdFactoryProvider;
import edu.tamu.tcat.trc.test.MockIdFactoryProvider;

/**
 * In memory implementation of the {@link WorkRepository} for use in testing.
 */
public class MockWorkRepository implements WorkRepository
{
   private final IdFactoryProvider idFactoryProvider = new MockIdFactoryProvider();
   private final IdFactory workIdFactory = idFactoryProvider.getIdFactory("works");
   private final Map<String, Work> cache = new HashMap<>();

   @Override
   public Iterator<Work> getAllWorks()
   {
      return cache.values().iterator();
   }

   @Override
   public Work getWork(String workId)
   {
      if (!cache.containsKey(workId))
         throw new IllegalArgumentException("No record found for work [" + workId + "]");

      return cache.get(workId);
   }

   @Override
   public Edition getEdition(String workId, String editionId)
   {
      Work w = getWork(workId);
      Edition edition = w.getEditions().stream()
                              .filter(e -> e.getId().equals(editionId))
                              .findAny()
                              .orElse(null);

      if (edition == null)
         throw new IllegalArgumentException("No record found for edition [" + editionId + "]");

      return edition;
   }

   @Override
   public Volume getVolume(String workId, String editionId, String volumeId)
   {
      Edition edition = getEdition(workId, editionId);
      Volume volume = edition.getVolumes().stream()
                           .filter(v -> v.getId().equals(volumeId))
                           .findAny()
                           .orElse(null);

      if (volume == null)
         throw new IllegalArgumentException("No record found for volume [" + volumeId + "]");

      return volume;
   }

   @Override
   public EditWorkCommand createWork()
   {
      String id = workIdFactory.get();
      return createWork(id);
   }

   @Override
   public EditWorkCommand createWork(String id)
   {
      // TODO Auto-generated method stub
      WorkDTO dto = new WorkDTO();
      dto.id = id;
      return new MockEditWorkCommand(dto, idFactoryProvider.extend(id), (update) ->
      {
         cache.put(update.id, ModelAdapter.adapt(update));
      });
   }

   @Override
   public EditWorkCommand editWork(String id)
   {
      WorkDTO dto = WorkDTO.create(getWork(id));
      return new MockEditWorkCommand(dto, idFactoryProvider.extend(id), (update) ->
      {
         // TODO fire notifications
         cache.put(update.id, ModelAdapter.adapt(update));
      });
   }

   @Override
   public void deleteWork(String id)
   {
      cache.remove(id);
   }
}
