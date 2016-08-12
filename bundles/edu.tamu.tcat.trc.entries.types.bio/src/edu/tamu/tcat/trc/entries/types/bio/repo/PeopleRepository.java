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
package edu.tamu.tcat.trc.entries.types.bio.repo;

import java.util.Iterator;
import java.util.concurrent.Future;

import edu.tamu.tcat.trc.entries.notification.UpdateListener;
import edu.tamu.tcat.trc.entries.types.bio.Person;
import edu.tamu.tcat.trc.repo.NoSuchEntryException;
import edu.tamu.tcat.trc.repo.RepositoryException;

/**
 *
 */
public interface PeopleRepository
{

   /** The type id used to identify biographical entries within the EntryResolver framework. */
   public final static String ENTRY_TYPE_ID = "trc.entries.biographical";

   /** The initial path (relative to some API endpoint) for building URIs that reference
    *  a biographical entry and its sub-elements. */
   public final static String ENTRY_URI_BASE = "entries/biographical";

   /**
    * Retrieves a person instance by their string identifier.
    * @param personId
    * @return
    */
   Person get(String personId) throws NoSuchEntryException;

   /**
    * @since 1.1
    */
   Iterator<Person> listAll() throws RepositoryException;

   /**
    * Creates a new entry for the supplied historical figure. Note that no de-duplication will
    * be performed. If this person (or a similar person) has already been added, a new entry
    * will be created.
    *
    * <p>This method will execute asynchronously. Upon success, it will pass an instance of
    * {@link Person} representing the create person to the observer. On failure, it
    * will supply an error message and optionally, a exception associated with the failure.
    *
    * @param histFigure A data vehicle containing the information for the person to create.
    * @param observer An optional observer that will be notified upon success or failure of
    *       this operation.
    */
   EditPersonCommand create();

   /**
    * Creates a new entry with a known ID. If a duplicate record exists with that ID, then an error
    * will be thrown upon command execution.
    *
    * @param id
    * @return
    */
   EditPersonCommand create(String id);

   /**
    * Updates the entry for the supplied historical figure. Note that this assumes that the
    * supplied person has already been created, that is, {@link #getPerson(String)} returns
    * successfully for {@code histFigure.id}.
    *
    * <p>This method will execute asynchronously. Upon success, it will pass an instance of
    * {@link Person} representing the updated person to the observer. On failure, it
    * will supply an error message and optionally, a exception associated with the failure.
    *
    * @param histFigure A data vehicle containing the information for the person to update.
    * @param observer An optional observer that will be notified upon success or failure of
    *       this operation.
    * @throws NoSuchEntryException
    */
   EditPersonCommand update(String personId) throws NoSuchEntryException;

   /**
    * Marks the entry for the identified person as having been deleted. References to this
    * person will be retained for historical and data consistency purposes but will not be
    * accessible via standard interfaces and queries.
    *
    * @param personId The unique identifier of the person to delete.
    * @param observer An optional observer that will be notified upon success or failure of
    *       this operation.
    * @return
    * @throws NoSuchEntryException
    * @since 1.1
    */
   Future<Boolean> delete(String personId) throws NoSuchEntryException;

   /**
    * Add listener to be notified whenever a biography has been modified (created, updated or deleted).
    * Note that this will be fired after the change has taken place and the attached listener will not
    * be able affect or modify the update action.
    *
    * @param ears The listener to be added.
    * @return A registration handle that allows the listener to be removed.
    * @since 1.1
    */
   AutoCloseable addUpdateListener(UpdateListener<PersonChangeEvent> ears);
}
