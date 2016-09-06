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
package edu.tamu.tcat.trc.entries.types.reln.repo;

import java.util.Iterator;
import java.util.function.Consumer;

import edu.tamu.tcat.trc.entries.types.reln.Relationship;
import edu.tamu.tcat.trc.repo.RepositoryException;


/**
 * Manages the persistence of {@link Relationship}s and other entities within the
 * relationship framework.
 *
 * <p>
 * Note that this is intended to serve as a thin facade to the underlying storage system.
 * It implements minimal data integrity checks and provides hooks for listening to data
 * persistence events (e.g., to support externally configurable logging and audit trails).
 * The repository provides basic data integrity checks but is not intended to enforce
 * business logic. It is also not responsible for performing authentication or authorization.
 * These functions are the responsibility of the client application that configures and uses
 * the repository.
 *
 */
public interface RelationshipRepository
{
   /**
    * Lists all stored relationships
    *
    * @return An iterator over all relationships
    */
   Iterator<Relationship> getAllRelationships();

   /**
    * @param id The id of the relationship to retrieve.
    * @return The relationship with the provided id.
    *
    * @throws RepositoryException If there is no relationship with the supplied id or there
    *    are errors accessing the persistence layer.
    */
   Relationship get(String id) throws RepositoryException;

   // Relationship get(URI relnUri);

   /**
    *  Construct an {@link EditRelationshipCommand} for use in creating a new relationship.
    *
    *  @return An {@link EditRelationshipCommand} for use in updating the properties
    *       of the relationship to be created.
    *  @throws RepositoryException If a new command instance could not be
    *       created.
    */
   EditRelationshipCommand create() throws RepositoryException;

   /**
    * Construct an {@link EditRelationshipCommand} to modify an existing relationship.
    *
    * @param id The id of the {@link Relationship} to edit.
    * @return An {@code EditRelationshipCommand} for use in modifying the
    *       identified {@code Relationship}.
    * @throws RepositoryException If the identified relationship does not exist or there are errors accessing the persistence layer.
    */
   EditRelationshipCommand edit(String id) throws RepositoryException;

   /**
    * Delete an existing {@link Relationship}.
    *
    * @param id The id of the {@link Relationship} to delete.
    * @throws RepositoryException If the identified relationship does not exist or there are errors accessing the persistence layer.
    */
   void delete(String id) throws RepositoryException;

   /**
    * Add listener to be notified whenever a relationship is modified (created, updated or deleted).
    * Note that this will be fired after the change has taken place and the attached listener will not
    * be able affect or modify the update action.
    *
    * @param ears The listener to be added.
    * @return A registration handle that allows the listener to be removed.
    */
   AutoCloseable addUpdateListener(Consumer<RelationshipChangeEvent> ears);

   // TODO may need to add hook for notification before the change happens to allow
   //      modification of the event (e.g., permission checks, etc).

   // TODO support the creation/mgnt of defined sets of relationships
   // TODO support tracking the history of revisions to relationships
   // NOTE these might be separate services
}
