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
package edu.tamu.tcat.trc.entries.types.biblio.impl.repo;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

import edu.tamu.tcat.trc.entries.types.biblio.dto.AuthorReferenceDTO;
import edu.tamu.tcat.trc.entries.types.biblio.dto.PublicationInfoDTO;
import edu.tamu.tcat.trc.entries.types.biblio.dto.TitleDTO;
import edu.tamu.tcat.trc.entries.types.biblio.repo.BibliographicEntryRepository;
import edu.tamu.tcat.trc.entries.types.biblio.repo.CopyReferenceMutator;
import edu.tamu.tcat.trc.entries.types.biblio.repo.VolumeMutator;
import edu.tamu.tcat.trc.repo.ChangeSet;
import edu.tamu.tcat.trc.repo.id.IdFactory;
import edu.tamu.tcat.trc.repo.id.IdFactoryProvider;

public class VolumeMutatorImpl implements VolumeMutator
{
   private final IdFactory copyRefIds;

   private String id;
   private final ChangeSet<DataModelV1.VolumeDTO> changes;

   public VolumeMutatorImpl(String id, ChangeSet<DataModelV1.VolumeDTO> volChanges, IdFactoryProvider idFactoryProvider)
   {
      this.id = id;
      this.changes = volChanges;
      this.copyRefIds = idFactoryProvider.getIdFactory(BibliographicEntryRepository.ID_CONTEXT_COPIES);
   }

   private Function<DataModelV1.VolumeDTO, DataModelV1.CopyReferenceDTO> makeCopySelector(String id)
   {
      return (dto) -> dto.copyReferences.stream()
            .filter(ref -> Objects.equals(id, ref.id))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Cannot find copy reference with id {" + id + "}."));
   }

   @Override
   public String getId()
   {
      return id;
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
   public void setVolumeNumber(String volumeNumber)
   {
      changes.add("name", dto -> dto.volumeNumber = volumeNumber);
   }

   @Override
   public void setPublicationInfo(PublicationInfoDTO pubInfo)
   {
      changes.add("pubInfo", dto -> dto.publicationInfo = ModelAdapter.adapt(pubInfo));
   }

   @Override
   public void setSummary(String summary)
   {
      changes.add("summary", dto -> dto.summary = summary);
   }

   @Override
   public void setSeries(String series)
   {
      changes.add("series", dto -> dto.series = series);
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
   public void setDefaultCopyReference(String refId)
   {
      changes.add("defaultCopy", dto -> {
         dto.defaultCopyReferenceId = refId;
      });
   }
}
