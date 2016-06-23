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
package edu.tamu.tcat.trc.entries.types.biblio.postgres;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import edu.tamu.tcat.trc.entries.types.biblio.dto.AuthorReferenceDTO;
import edu.tamu.tcat.trc.entries.types.biblio.dto.CopyReferenceDTO;
import edu.tamu.tcat.trc.entries.types.biblio.dto.PublicationInfoDTO;
import edu.tamu.tcat.trc.entries.types.biblio.dto.TitleDTO;
import edu.tamu.tcat.trc.entries.types.biblio.dto.VolumeDTO;
import edu.tamu.tcat.trc.entries.types.biblio.repo.CopyReferenceMutator;
import edu.tamu.tcat.trc.entries.types.biblio.repo.VolumeMutator;
import edu.tamu.tcat.trc.repo.IdFactory;
import edu.tamu.tcat.trc.repo.IdFactoryProvider;

public class VolumeMutatorImpl implements VolumeMutator
{
   private final VolumeDTO volume;
   private IdFactory copyReferenceIdFactory;

   VolumeMutatorImpl(VolumeDTO volume, IdFactoryProvider idFactoryProvider)
   {
      this.volume = volume;
      this.copyReferenceIdFactory = idFactoryProvider.getIdFactory(WorkRepositoryImpl.ID_CONTEXT_COPIES);
   }

   @Override
   public String getId()
   {
      return volume.id;
   }

   @Override
   public void setPublicationInfo(PublicationInfoDTO info)
   {
      this.volume.publicationInfo = info;
   }

   @Override
   public void setVolumeNumber(String volumeNumber)
   {
      this.volume.volumeNumber = volumeNumber;
   }

   @Override
   public void setAuthors(List<AuthorReferenceDTO> authors)
   {
      volume.authors = new ArrayList<>(authors);
   }

   @Override
   public void setTitles(Collection<TitleDTO> titles)
   {
      volume.titles = new HashSet<>(titles);
   }

   @Override
   public void setOtherAuthors(List<AuthorReferenceDTO> otherAuthors)
   {
      volume.otherAuthors = new ArrayList<>(otherAuthors);
   }

   @Override
   public void setSummary(String summary)
   {
      volume.summary = summary;
   }

   @Override
   public void setSeries(String series)
   {
      volume.series = series;
   }

   @Override
   public void setDefaultCopyReference(String defaultCopyReferenceId)
   {
      boolean found = volume.copyReferences.stream()
            .anyMatch(cr -> Objects.equals(cr.id, defaultCopyReferenceId));

      if (!found)
      {
         throw new IllegalArgumentException("Cannot find copy reference with id {" + defaultCopyReferenceId + "}.");
      }

      volume.defaultCopyReferenceId = defaultCopyReferenceId;
   }

   @Override
   public CopyReferenceMutator createCopyReference()
   {
      CopyReferenceDTO copyReference = new CopyReferenceDTO();
      copyReference.id = copyReferenceIdFactory.get();
      volume.copyReferences.add(copyReference);
      return new CopyReferenceMutatorImpl(copyReference);
   }

   @Override
   public CopyReferenceMutator editCopyReference(String id)
   {
      CopyReferenceDTO copyReference = volume.copyReferences.stream()
            .filter(ref -> Objects.equals(id, ref.id))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Cannot find copy reference with id {" + id + "}."));

      return new CopyReferenceMutatorImpl(copyReference);
   }

   @Override
   public void removeCopyReference(String id)
   {
      volume.copyReferences.removeIf(cr -> Objects.equals(cr.id, id));
   }

   @Override
   public Set<String> retainAllCopyReferences(Set<String> copyReferenceIds)
   {
      Objects.requireNonNull(copyReferenceIds);

      Set<String> existingIds = volume.copyReferences.stream()
            .map(cr -> cr.id)
            .collect(Collectors.toSet());

      Set<String> notFound = copyReferenceIds.stream()
         .filter(id -> !existingIds.contains(id))
         .collect(Collectors.toSet());

      volume.copyReferences.removeIf(cr -> !copyReferenceIds.contains(cr.id));

      return notFound;
   }
}
