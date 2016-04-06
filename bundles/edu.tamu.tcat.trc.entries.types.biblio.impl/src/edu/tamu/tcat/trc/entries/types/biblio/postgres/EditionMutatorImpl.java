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

import edu.tamu.tcat.trc.entries.types.biblio.dto.AuthorReferenceDTO;
import edu.tamu.tcat.trc.entries.types.biblio.dto.EditionDTO;
import edu.tamu.tcat.trc.entries.types.biblio.dto.PublicationInfoDTO;
import edu.tamu.tcat.trc.entries.types.biblio.dto.TitleDTO;
import edu.tamu.tcat.trc.entries.types.biblio.dto.VolumeDTO;
import edu.tamu.tcat.trc.entries.types.biblio.dto.copies.CopyReferenceDTO;
import edu.tamu.tcat.trc.entries.types.biblio.postgres.copies.CopyReferenceMutatorImpl;
import edu.tamu.tcat.trc.entries.types.biblio.repo.EditionMutator;
import edu.tamu.tcat.trc.entries.types.biblio.repo.VolumeMutator;
import edu.tamu.tcat.trc.entries.types.biblio.repo.copies.CopyReferenceMutator;
import edu.tamu.tcat.trc.repo.IdFactory;
import edu.tamu.tcat.trc.repo.IdFactoryProvider;

public class EditionMutatorImpl implements EditionMutator
{
   private final EditionDTO edition;
   private final IdFactoryProvider idFactoryProvider;
   private final IdFactory volumeIdFactory;
   private final IdFactory copyReferenceIdFactory;


   /**
    * @param edition
    * @param volumeIdSupplier Supplier to generate IDs for volumes.
    */
   EditionMutatorImpl(EditionDTO edition, IdFactoryProvider idFactoryProvider)
   {
      this.edition = edition;
      this.idFactoryProvider = idFactoryProvider;
      this.volumeIdFactory = idFactoryProvider.getIdFactory("volumes");
      this.copyReferenceIdFactory = idFactoryProvider.getIdFactory("copies");
   }


   @Override
   public String getId()
   {
      return edition.id;
   }

   @Override
   public void setAuthors(List<AuthorReferenceDTO> authors)
   {
      edition.authors = new ArrayList<>(authors);
   }

   @Override
   public void setTitles(Collection<TitleDTO> titles)
   {
      edition.titles = new HashSet<>(titles);
   }

   @Override
   public void setOtherAuthors(List<AuthorReferenceDTO> otherAuthors)
   {
      edition.otherAuthors = new ArrayList<>(otherAuthors);
   }

   @Override
   public void setEditionName(String editionName)
   {
      this.edition.editionName = editionName;
   }

   @Override
   public void setPublicationInfo(PublicationInfoDTO pubInfo)
   {
      edition.publicationInfo = pubInfo;
   }

   @Override
   public void setSummary(String summary)
   {
      edition.summary = summary;
   }

   @Override
   public void setSeries(String series)
   {
      edition.series = series;
   }

   @Override
   public VolumeMutator createVolume()
   {
      VolumeDTO volume = new VolumeDTO();
      volume.id = volumeIdFactory.get();
      edition.volumes.add(volume);
      return new VolumeMutatorImpl(volume, getVolumeIdFactoryProvider(volume.id));
   }

   @Override
   public VolumeMutator editVolume(String id)
   {
      VolumeDTO volume = edition.volumes.stream()
            .filter(vol -> Objects.equals(vol.id, id))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unable to find volume with id [" + id + "]."));

      return new VolumeMutatorImpl(volume, getVolumeIdFactoryProvider(id));
   }

   private IdFactoryProvider getVolumeIdFactoryProvider(String id)
   {
      return idFactoryProvider.extend("volumes/" + id + "/");
   }


   @Override
   public void removeVolume(String volumeId)
   {
      edition.volumes.removeIf(volume -> Objects.equals(volume.id, volumeId));
   }

   @Override
   public void syncVolumes(Set<String> volumeIds)
   {
      Objects.requireNonNull(volumeIds);
      // TODO: should this method check that all copyReferenceIds are valid?
      edition.volumes.removeIf(volume -> !volumeIds.contains(volume.id));
   }

   @Override
   public void setDefaultCopyReference(String defaultCopyReferenceId)
   {
      boolean found = edition.copyReferences.stream()
            .anyMatch(cr -> Objects.equals(cr.id, defaultCopyReferenceId));

      if (!found)
      {
         throw new IllegalArgumentException("Cannot find copy reference with id {" + defaultCopyReferenceId + "}.");
      }

      edition.defaultCopyReferenceId = defaultCopyReferenceId;
   }

   @Override
   public CopyReferenceMutator createCopyReference()
   {
      CopyReferenceDTO copyReference = new CopyReferenceDTO();
      copyReference.id = copyReferenceIdFactory.get();
      edition.copyReferences.add(copyReference);
      return new CopyReferenceMutatorImpl(copyReference);
   }

   @Override
   public CopyReferenceMutator editCopyReference(String id)
   {
      CopyReferenceDTO copyReference = edition.copyReferences.stream()
            .filter(ref -> Objects.equals(id, ref.id))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Cannot find copy reference with id {" + id + "}."));

      return new CopyReferenceMutatorImpl(copyReference);
   }

   @Override
   public void removeCopyReference(String id)
   {
      edition.copyReferences.removeIf(cr -> Objects.equals(cr.id, id));
   }

   @Override
   public void syncCopyReferences(Set<String> copyReferenceIds)
   {
      Objects.requireNonNull(copyReferenceIds);
      // TODO: should this method check that all copyReferenceIds are valid?
      edition.copyReferences.removeIf(cr -> !copyReferenceIds.contains(cr.id));
   }
}
