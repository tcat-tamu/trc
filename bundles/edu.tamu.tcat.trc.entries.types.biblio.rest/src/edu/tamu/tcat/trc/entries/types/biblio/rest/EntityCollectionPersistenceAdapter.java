package edu.tamu.tcat.trc.entries.types.biblio.rest;

import java.util.Collection;
import java.util.function.Consumer;

import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.ServerErrorException;

/**
 * Interfaces with the works repository layer to provide convenience mechanisms for accessing
 * the bibliographic hierarchy (editions, volumes, copies) and translating those to the REST
 * API layer. This is the counterpart to {@link EntityPersistenceAdapter} designed to support
 * actions on collections of entities. Method calls translate persistence layer exceptions
 * into JAX-RS exceptions that map to the defined behavior for the REST API.
 *
 * @param <Model> REST layer DTO for the referenced domain model object.
 * @param <Editor> The command/mutator type used to edit instances of this object.
 */
public interface EntityCollectionPersistenceAdapter<Model, Editor>
{

   // TODO add support for insert, remove, add, move

   /**
    * @return REST layer DTOs all instances in the collection. If there are no items in the
    *       collection, this will return an empty collection, rather than throw an error.
    *
    * @throws InternalServerErrorException If data access or other errors prevented the
    *       retrieval of this object.
    */
   Collection<Model> get() throws InternalServerErrorException;

   /**
    * @param id The id of the entity to obtain.
    *
    * @return A persistence adapter for accessing and editing the identified entry.
    *
    * @throws NotFoundException If the identified entity is not a member of this collection.
    * @throws InternalServerErrorException If errors prevented successful retrieval
    */
   EntityPersistenceAdapter<Model, Editor> get(String id) throws NotFoundException, InternalServerErrorException;

   /**
    * Creates a new instance within this collection.
    *
    * @param modifier A function that will set the initial state of the entity to create.
    *
    * @return The id of the created entity.
    *
    * @throws InternalServerErrorException If errors prevented successful creation
    * @throws ServerErrorException Propagated from the supplied modifier.
    */
   String create(Consumer<Editor> modifier);
}
