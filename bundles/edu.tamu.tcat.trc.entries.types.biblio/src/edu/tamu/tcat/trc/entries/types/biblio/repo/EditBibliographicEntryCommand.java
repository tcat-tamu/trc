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

import edu.tamu.tcat.trc.entries.core.repo.EditEntryCommand;
import edu.tamu.tcat.trc.entries.types.biblio.Edition;
import edu.tamu.tcat.trc.entries.types.biblio.Volume;
import edu.tamu.tcat.trc.entries.types.biblio.BibliographicEntry;
import edu.tamu.tcat.trc.entries.types.biblio.dto.AuthorReferenceDTO;
import edu.tamu.tcat.trc.entries.types.biblio.dto.TitleDTO;

/**
 * Used to edit a {@link BibliographicEntry}. This class allows clients to make updates to a {@link BibliographicEntry}
 * instance and its component elements (e.g., {@link Edition}s and {@link Volume}s) in a multi-step
 * transaction and to commit those changes to the persistence layer via its
 * {@link #execute()} method.
 *
 * <p>Note that implementations typically are not thread safe.
 *
 * @see BibliographicEntryRepository#create()
 * @see BibliographicEntryRepository#edit(String)
 */
public interface EditBibliographicEntryCommand extends EditEntryCommand<BibliographicEntry>
{
   // TODO: Any field that is a collection of models should eventually use mutators.

   /**
    * @return The ID of the work being edited.
    */
   String getId();

   /**
    *
    * @param type
    */
   @Deprecated // see note on Work#getType()
   void setType(String type);

   /**
    * Updates the list of authors.
    * @param authors
    */
   void setAuthors(List<AuthorReferenceDTO> authors);

   /**
    *
    * @param titles
    */
   void setTitles(Collection<TitleDTO> titles);

   /**
    *
    * @param authors
    */
   @Deprecated
   void setOtherAuthors(List<AuthorReferenceDTO> authors);

   /**
    *
    * @param series
    */
   void setSeries(String series);

   /**
    *
    * @param summary
    */
   void setSummary(String summary);

   /**
    * Creates an edition mutator to update fields on an existing edition of this work.
    *
    * @param id The ID of a contained edition.
    * @return A mutator for the given edition ID.
    */
   EditionMutator editEdition(String id);

   /**
    * Creates an edition mutator for a new edition of this work.
    *
    * @return
    */
   EditionMutator createEdition();

   /**
    * Removes the specified edition from the work.
    */
   void removeEdition(String editionId);

   /**
    * Removes editions whose ID does not appear in the provided set of edition IDs
    *
    * @param editionIds Edition IDs to keep
    * @return IDs that were not found
    */
   Set<String> retainAllEditions(Set<String> editionIds);

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
    * @return IDs that were not found
    */
   Set<String> retainAllCopyReferences(Set<String> copyReferenceIds);

}
