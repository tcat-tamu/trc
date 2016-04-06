package edu.tamu.tcat.trc.entries.types.biblio.rest;

import java.util.function.Consumer;

import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.ServerErrorException;

/**
 * Wraps an identifier for a domain object and provides methods for obtaining an instance of
 * that object and mutators/edit commands. These methods translate persistence layer exceptions
 * into JAX-RS exceptions that map to the defined behavior for the REST API.
 *
 * <p>This helps in particular to encapsulate the logic for working with domain objects at
 * different levels of the bibliographic record hierarchy. For example, a volume requires
 * the work, edition and volume id in order to obtain an instance. Obtaining a mutator for
 * that object.
 *
 * @param <Model> REST layer DTO for the referenced domain model object.
 * @param <Editor> The command/mutator type used to edit instances of this object.

 */
public interface EntityPersistenceAdapter<Model, Editor>
{
   // TODO should this migrate to TRC API?

   /**
    * @return The domain model instance
    * @throws NotFoundException if the model cannot be found
    * @throws InternalServerErrorException if any other error prevented successful loading
    */
   Model get();

   /**
    * Update the referenced domain model object. This will construct the necessary editor
    * instance, supply that editor to the modifier in order to effect changes on the domain
    * object and then execute those changes within the persistence layer.
    *
    * <p>Note that this may or may not execute synchronously. Implementations should document
    * their behavior. In general, a synchronous behavior is preferred.
    *
    * @param modifier Applies updates to an editor for the supplied object.
    * @throws NotFoundException if the model cannot be found
    * @throws InternalServerErrorException if any other error prevented successful loading or saving
    * @throws ServerErrorException Propagated from the supplied modifier.
    */
   void edit(Consumer<Editor> modifier);     // concurrent modification, permissions, etc.

   /**
    * Delete the referenced model instance.
    *
    * @throws NotFoundException if the model cannot be found
    * @throws InternalServerErrorException if any other error prevented successful deletion
    */
   void delete();
}
