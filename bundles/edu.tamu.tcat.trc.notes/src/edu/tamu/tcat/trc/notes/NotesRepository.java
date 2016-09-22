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
package edu.tamu.tcat.trc.notes;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.Future;

import edu.tamu.tcat.trc.entries.core.resolver.EntryReference;

public interface NotesRepository
{
   /**
    * Retrieves a specific {@link Note}
    *
    * @param noteId The id of the Note to retrieve
    * @return The identified note.
    */
   Optional<Note> get(String noteId);

   /**
    * Retrieves the {@link Note}s associated with a given TRC entry.
    *
    * @param ref A reference to the given entry.
    * @return The notes associated with the supplied entry. Will not be <code>null</code>,
    *       may be an empty list.
    * @throws IllegalStateException If internal errors prevent the retrieval of the
    *       requested resource. This is likely due to database access issues.
    */
   Collection<Note> getNotes(EntryReference ref) throws IllegalStateException;

   /**
    * @return an edit command for use in creating a new {@link Note}.
    */
   EditNoteCommand create();

   /**
    * @return an edit command for use in modifying an existing {@link Note}.
    */
   EditNoteCommand edit(String noteId);

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
   Future<Boolean> remove(String noteId);
}
