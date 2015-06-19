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
package edu.tamu.tcat.trc.entries.types.biblio.copies.repo;

import java.net.URI;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Future;

import edu.tamu.tcat.trc.entries.notification.UpdateListener;
import edu.tamu.tcat.trc.entries.repo.NoSuchCatalogRecordException;
import edu.tamu.tcat.trc.entries.types.biblio.copies.CopyReference;

/**
 *  Provides support for creation, retrieval, update and deletion of references between a
 *  digital copy and a bibliographic entity (work, edition or volume).
 */
public interface CopyReferenceRepository
{
   // NOTE authorization, auditing, etc will be handled separately.

   /**
    * Build an {@link EditCopyReferenceCommand} for use in creating a new {@link CopyReference}.
    * @return
    */
   EditCopyReferenceCommand create();

   /**
    * Build an {@link EditCopyReferenceCommand} for use in creating editing an existing
    * {@link CopyReference}.
    *
    * @param id
    * @return
    * @throws NoSuchCatalogRecordException If the identified copy does not exist.
    */
   EditCopyReferenceCommand edit(UUID id) throws NoSuchCatalogRecordException;

   /**
    * @param entity The URI of the bibliographic entity for which copies should be returned.
    *       Note that this may be a work, edition or volume. This method will return copies for
    *       the identified object and all component entities as well.
    * @return The
    */
   List<CopyReference> getCopies(URI entity);

   /**
    * Retries a specific {@link CopyReference}
    * @param id The id of the copy reference to return.
    * @return
    * @throws NoSuchCatalogRecordException
    */
   CopyReference get(UUID id) throws NoSuchCatalogRecordException;

   /**
    * Removes the identified copy reference exception. Note that this will mark the indicated
    * entry as having been deleted, but the data may be retrieved at a later point to support
    * historical analysis
    *
    * @param id The id of the reference to remove.
    * @return A handle to determine if the item was successfully deleted from the persistence
    *       layer. Note that this will be {@code false} if the reference does not exist in the
    *       persistence layer and will propagate any internal errors encountered in attempting
    *       to remove the indicated reference.
    */
   Future<Boolean> remove(UUID id) throws CopyReferenceException;

   AutoCloseable register(UpdateListener<CopyChangeEvent> ears);
}