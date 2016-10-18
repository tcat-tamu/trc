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
import java.util.concurrent.CompletableFuture;

import edu.tamu.tcat.trc.entries.core.repo.EntryRepository;
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
public interface RelationshipRepository extends EntryRepository<Relationship>
{

   /** The type id used to identify relationships within the EntryResolver framework. */
   public final static String ENTRY_TYPE_ID = "trc.entries.relationships";

   /** The initial path (relative to some API endpoint) for building URIs that reference
    *  a relationship and its sub-elements. */
   public final static String ENTRY_URI_BASE = "entries/relationships";

   /**
    * @return The registry that defines the relationship types that have been configured
    *    for this application.
    */
   RelationshipTypeRegistry getTypeRegistry();

   /**
    * Lists all stored relationships
    *
    * @return An iterator over all relationships
    */
   @Override
   Iterator<Relationship> listAll();

   /**
    * @param id The id of the relationship to retrieve.
    * @return The relationship with the provided id.
    *
    * @throws RepositoryException If there is no relationship with the supplied id or there
    *    are errors accessing the persistence layer.
    */
   @Override
   Relationship get(String id);

   // Relationship get(URI relnUri);

   /**
    *  Construct an {@link EditRelationshipCommand} for use in creating a new relationship.
    *
    *  @return An {@link EditRelationshipCommand} for use in updating the properties
    *       of the relationship to be created.
    *  @throws RepositoryException If a new command instance could not be
    *       created.
    */
   @Override
   EditRelationshipCommand create();

   @Override
   EditRelationshipCommand create(String id);

   /**
    * Construct an {@link EditRelationshipCommand} to modify an existing relationship.
    *
    * @param id The id of the {@link Relationship} to edit.
    * @return An {@code EditRelationshipCommand} for use in modifying the
    *       identified {@code Relationship}.
    * @throws RepositoryException If the identified relationship does not exist or there are errors accessing the persistence layer.
    */
   @Override
   EditRelationshipCommand edit(String id);

   /**
    * Delete an existing {@link Relationship}.
    *
    * @param id The id of the {@link Relationship} to delete.
    * @throws RepositoryException If the identified relationship does not exist or there are errors accessing the persistence layer.
    */
   @Override
   CompletableFuture<Boolean> remove(String id);

   @Override
   EntryRepository.ObserverRegistration onUpdate(EntryRepository.UpdateObserver<Relationship> observer);
}
