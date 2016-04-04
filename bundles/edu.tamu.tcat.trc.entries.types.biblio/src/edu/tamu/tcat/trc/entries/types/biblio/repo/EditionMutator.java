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

import edu.tamu.tcat.trc.entries.repo.NoSuchCatalogRecordException;
import edu.tamu.tcat.trc.entries.types.biblio.Edition;
import edu.tamu.tcat.trc.entries.types.biblio.dto.AuthorReferenceDTO;
import edu.tamu.tcat.trc.entries.types.biblio.dto.PublicationInfoDTO;
import edu.tamu.tcat.trc.entries.types.biblio.dto.TitleDTO;
import edu.tamu.tcat.trc.entries.types.biblio.repo.copies.CopyReferenceMutator;

/**
 * Used to edit the properties of an {@link Edition}. A {@code EditionMutator} is created
 * within the transactional scope of an {@link EditWorkCommand} via either the
 * {@link EditWorkCommand#createEdition()} or the {@link EditWorkCommand#editEdition(String)}
 * method. Changes made to the {@code Edition} modified by this mutator will take effect
 * when the parent {@link EditWorkCommand#execute()} method is invoked. Changes made after this
 * command's {@code execute()} method is called will have indeterminate affects.
 *
 * <p>
 * Implementations are typically not threadsafe.
 */
public interface EditionMutator
{
   /**
    * @return The ID of the edition being edited.
    */
   String getId();

   /**
    * @param authors The list of authors to be set for this edition.
    */
   void setAuthors(List<AuthorReferenceDTO> authors);

   /**
    * @param titles The titles to be set for this edition
    */
   void setTitles(Collection<TitleDTO> titles);

   /**
    * @param otherAuthors The other authors for this edition.
    */
   void setOtherAuthors(List<AuthorReferenceDTO> otherAuthors);

   /**
    * @param editionName the name of this edition.
    */
   void setEditionName(String editionName);

   /**
    * @param pubInfo Information about who, where and when this edition was published.
    */
   void setPublicationInfo(PublicationInfoDTO pubInfo);

   /**
    * @param series The series name to which this edition belongs.
    */
   void setSeries(String series);

   /**
    * @param summary An editorial summary of this edition.
    */
   void setSummary(String summary);

   /**
    * Creates a new volume within this edition.
    *
    * @return A mutator to be used to edit the newly created volume.
    */
   VolumeMutator createVolume();

   /**
    * Edit a volume associated with this edition.
    * @param id The id of the volume to edit.
    * @return A mutator to be used to edit the newly created volume.
    * @throws NoSuchCatalogRecordException If the identified volume is not associated with this edition.
    */
   VolumeMutator editVolume(String id);

   /**
    * Removed the specified volume from the work.
    */
   void removeVolume(String volumeId);

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
}
