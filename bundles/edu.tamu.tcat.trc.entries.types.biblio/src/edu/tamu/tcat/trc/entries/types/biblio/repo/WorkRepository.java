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

import java.util.function.Consumer;

import edu.tamu.tcat.trc.entries.repo.NoSuchCatalogRecordException;
import edu.tamu.tcat.trc.entries.types.biblio.AuthorReference;
import edu.tamu.tcat.trc.entries.types.biblio.Edition;
import edu.tamu.tcat.trc.entries.types.biblio.Volume;
import edu.tamu.tcat.trc.entries.types.biblio.Work;
import edu.tamu.tcat.trc.entries.types.bio.Person;

/**
 * Provides the main point of access for creating, editing and querying works.
 *
 */
public interface WorkRepository
{
   /**
    *
    * @return An {@link Iterable} over all works in the collection.
    * @deprecated This method will be replaced or updated in order to prevent unbounded
    *    retrieval of all items. In general, we should provide a robust query mechanism
    *    rather than a 'get all'. We also need to support a repeatable, pageable,
    *    identifiable result set.
    */
   @Deprecated
   Iterable<Work> listWorks();      // TODO must create a repeatable, pageable, identifiable result set.

   /**
    *
    * @param title The title to search for.
    * @return All works whose title partially matches the supplied title.
    * @deprecated To be replaced with a more robust query/search mechanism.
    *
    */
   @Deprecated
   Iterable<Work> listWorks(String title);

   /**
    * @param workId The id of the work to retrieve.
    * @return The record for a specific work.
    * @throws NoSuchCatalogRecordException If the requested work does not exist.
    */
   Work getWork(String workId) throws NoSuchCatalogRecordException;

   /**
    * @param workId The ID of a work.
    * @param editionId The ID of an edition of the given work.
    * @return The edition for a specific work.
    * @throws NoSuchCatalogRecordException If the requested work or edition does not exist.
    */
   Edition getEdition(String workId, String editionId) throws NoSuchCatalogRecordException;

   /**
    * @param workId The ID of a work.
    * @param editionId The ID of an edition of the given work.
    * @param volumeId The ID of a volume of the specified edition.
    * @return The volume for a specific edition of a work.
    * @throws NoSuchCatalogRecordException If the requested work, edition, or volume does not exist.
    */
   Volume getVolume(String workId, String editionId, String volumeId) throws NoSuchCatalogRecordException;

   /**
    * Given an author reference, return the biographical record for the referenced person.
    *
    * @param ref A reference to an author.
    * @return The person associated with that author.
    */
   Person getAuthor(AuthorReference ref);

   /**
    * Construct an {@link EditWorkCommand} to be used to create a new work.
    *
    * @return
    */
   EditWorkCommand create();

   /**
    * Constructs an {@link EditWorkCommand} to be used to create a new work with a known ID.
    *
    * If a duplicate record exists with that ID, then an error will be thrown at command execution.
    *
    * @param id
    * @return
    */
   EditWorkCommand create(String id);

   /**
    * Construct an {@link EditWorkCommand} to be used to modify an existing work.
    *
    * @param id
    * @return
    * @throws NoSuchCatalogRecordException
    */
   EditWorkCommand edit(String id) throws NoSuchCatalogRecordException;

   void delete(String id);

   AutoCloseable addUpdateListener(Consumer<WorkChangeEvent> ears);

//   @Deprecated // use the EditWorkCommand methods
//   void create(WorkDV work, DataUpdateObserver<String> observer);
//
//   // TODO might return a handle that allows for cancellation, and blocking
//   @Deprecated // use the EditWorkCommand methods
//   void update(WorkDV work, DataUpdateObserver<String> observer);
}
