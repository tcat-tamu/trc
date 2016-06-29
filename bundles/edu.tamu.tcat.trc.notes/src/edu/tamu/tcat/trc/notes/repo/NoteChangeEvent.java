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

import edu.tamu.tcat.trc.entries.notification.UpdateEvent;
import edu.tamu.tcat.trc.notes.Note;
import edu.tamu.tcat.trc.repo.RepositoryException;

/**
 * An event notification sent from a {@link NotesRepository} due to a data change.
 */
public interface NoteChangeEvent extends UpdateEvent
{
   /**
    * Retrieves the notes that changed.
    *
    * @return the notes that changed.
    * @throws RepositoryException If the notes cannot be retrieved (for example,
    *       if the record was deleted).
    * @deprecated In general, this API is not safe. Implementations should return null. Clients
    *       should obtain a reference to the {@link NotesRepository} and call the appropriate
    *       method on that class.
    */
   @Deprecated
   default Note getNotes() throws RepositoryException { return null; };
}
