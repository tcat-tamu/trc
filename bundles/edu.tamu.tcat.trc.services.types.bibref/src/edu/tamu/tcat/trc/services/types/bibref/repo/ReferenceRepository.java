package edu.tamu.tcat.trc.services.types.bibref.repo;

import java.util.concurrent.CompletableFuture;

import edu.tamu.tcat.trc.entries.core.resolver.EntryReference;
import edu.tamu.tcat.trc.services.types.bibref.ReferenceCollection;

public interface ReferenceRepository
{
   /**
    * Retrieves the bibliography associated with the given reference or creates a new bibliography if one does not already exist.
    * @param ref
    * @return
    */
   ReferenceCollection get(EntryReference ref);

   /**
    * Edits an existing bibliography associated with the given reference or creates a new bibliography if one does not already exist.
    * @param ref
    * @return
    */
   EditBibliographyCommand edit(EntryReference ref);

   /**
    * Removes the associated bibliography from the given reference if it exists.
    * @param ref
    * @return A completable future that resolves to {@code true} if the database was modified, and {@code false} otherwise.
    */
   CompletableFuture<Boolean> delete(EntryReference ref);
}
