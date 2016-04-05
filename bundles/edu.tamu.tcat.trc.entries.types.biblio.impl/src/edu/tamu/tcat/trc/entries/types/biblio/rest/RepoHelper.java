package edu.tamu.tcat.trc.entries.types.biblio.rest;

import java.util.function.Consumer;

import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;

public interface RepoHelper<Model, Editor>
{
   /**
    * Helper method to load a model from persistence, handling any exceptions that arise and passing
    * them as HTTP messages.
    *
    * @return
    * @throws NotFoundException if the model cannot be found
    * @throws InternalServerErrorException if any other error prevented successful loading
    */
   Model get();

   /**
    * Helper method to start editing a model, handling any exceptions that arise and passing them as
    * HTTP messages. Changes are automatically persisted after the modifier callback is finished.
    *
    * @param modifier
    * @throws NotFoundException if the model cannot be found
    * @throws InternalServerErrorException if any other error prevented successful loading or saving
    */
   void edit(Consumer<Editor> modifier);

   /**
    * Helper method to delete a model, handling any exceptions that arise and passing them as HTTP
    * messages.
    *
    * @throws NotFoundException if the model cannot be found
    * @throws InternalServerErrorException if any other error prevented successful deletion
    */
   void delete();
}
