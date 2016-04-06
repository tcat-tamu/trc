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
package edu.tamu.tcat.trc.entries.types.biblio.repo;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import edu.tamu.tcat.trc.entries.types.biblio.Volume;
import edu.tamu.tcat.trc.entries.types.biblio.dto.AuthorReferenceDTO;
import edu.tamu.tcat.trc.entries.types.biblio.dto.PublicationInfoDTO;
import edu.tamu.tcat.trc.entries.types.biblio.dto.TitleDTO;
import edu.tamu.tcat.trc.entries.types.biblio.repo.copies.CopyReferenceMutator;

/**
 * Used to edit the properties of a {@link Volume}. A {@code VolumeMutator} is created
 * within the transactional scope of an {@link EditWorkCommand} via either the
 * {@link EditionMutator#createVolume()} or the {@link EditionMutator#editVolume(String)}
 * method. Changes made to the {@code Volume} modified by this mutator will take effect
 * when the parent {@link EditWorkCommand#execute()} method is invoked. Changes made after
 * this command's {@code execute()} method is called will have indeterminate affects.
 *
 * <p>
 * Implementations are typically not threadsafe.
 */
public interface VolumeMutator
{
   /**
    * @return The ID of the volume being edited
    */
   String getId();

   /**
    * @param volumeNumber The number or other identifier for this volume.
    */
   void setVolumeNumber(String volumeNumber);

   /**
    * @param authors The list of author responsible for creating this volume
    */
   void setAuthors(List<AuthorReferenceDTO> authors);

   /**
    * @param titles The title(s) associated with this volume.
    */
   void setTitles(Collection<TitleDTO> titles);

   /**
    * @param otherAuthors The list of other authors who contributed to the creation of this
    *       volume.
    */
   void setOtherAuthors(List<AuthorReferenceDTO> otherAuthors);

   /**
    * @param info The publication info for this volume.
    */
   void setPublicationInfo(PublicationInfoDTO info);

   /**
    *
    * @param series The series this volume belongs to.
    */
   void setSeries(String series);

   /**
    *
    * @param summary An editorial summary of this volume.
    */
   void setSummary(String summary);

   /**
    * Sets the default copy reference by ID
    *
    * @throws IllegalArgumentException if a copy reference with the given ID does not exist.
    */
   void setDefaultCopyReference(String defaultCopyReferenceId);

   /**
    * Creates a copy reference mutator to update fields on an existing digital copy reference.
    *
    * @param id The ID of a contained copy reference.
    * @return A mutator for the given copy reference ID.
    */
   CopyReferenceMutator editCopyReference(String id);

   /**
    * Creates a copy reference mutator for a new digital copy.
    *
    * @return
    */
   CopyReferenceMutator createCopyReference();

   /**
    * Removes a copy reference by id
    *
    * @param id
    */
   void removeCopyReference(String id);

   /**
    * Removes copy references whose ID does not appear in the provided set of copy reference IDs
    *
    * @param copyReferenceIds Copy reference IDs to keep
    */
   void retainAllCopyReferences(Set<String> copyReferenceIds);
}
