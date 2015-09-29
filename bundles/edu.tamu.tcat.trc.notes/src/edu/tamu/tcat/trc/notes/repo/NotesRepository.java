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
package edu.tamu.tcat.trc.notes.repo;

import java.net.URI;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Future;

import edu.tamu.tcat.trc.entries.notification.UpdateListener;
import edu.tamu.tcat.trc.entries.repo.NoSuchCatalogRecordException;
import edu.tamu.tcat.trc.notes.Note;

public interface NotesRepository
{
   /**
    * Retrieves a specific {@link Note}
    *
    * @param noteId The id of the Note to retrieve
    * @return The identified note.
    * @throws NoSuchCatalogRecordException If the requested note does not exist.
    */
   Note get(UUID noteId) throws NoSuchCatalogRecordException;

   /**
    * Retrieves a list of {@link Note} associated with a particular URI.
    *
    * @param entityURI URI that may contain {@link Note}.
    * @return Collection of Notes
    */
   List<Note> getNotes(URI entityURI) throws NoSuchCatalogRecordException;

   /**
    * @return an edit command for use in creating a new {@link Note}.
    */
   EditNoteCommand create();

   /**
    * @return an edit command for use in modifying an existing {@link Note}.
    */
   EditNoteCommand edit(UUID noteId) throws NoSuchCatalogRecordException;

   /**
    * Removes a {@link Note} entry from the database.
    *
    * @return A future for use to determine if the deletion action succeeded. Will return
    *    {@code true} if the note was removed from the persistence layer or {@code false}
    *    if the note did not exist or has already been removed. In either case, the note
    *    will no longer exist within the storage layer. Will throw an exception if the note
    *    could not be removed or if another internal error occurred while attempting to
    *    remove the note. In this case, the removal operation may or may not have succeeded.
    */
   Future<Boolean> remove(UUID noteId);

   /**
    * Register a listener that will be notified when a note changes.
    *
    * @param ears The listener to be notified.
    * @return A registration that allows the client to stop listening for changes. The returned
    *       registration <em>must</em> be closed by the caller.
    */
   AutoCloseable register(UpdateListener<NoteChangeEvent> ears);
}
