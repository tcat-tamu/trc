package edu.tamu.tcat.trc.services.bibref.repo;

import java.util.concurrent.CompletableFuture;

import edu.tamu.tcat.account.Account;
import edu.tamu.tcat.trc.resolver.EntryReference;
import edu.tamu.tcat.trc.services.BasicServiceContext;
import edu.tamu.tcat.trc.services.ServiceContext;
import edu.tamu.tcat.trc.services.bibref.ReferenceCollection;

public interface RefCollectionService
{

   static ServiceContext<RefCollectionService> makeContext(Account account)
   {
      return new BasicServiceContext<>(RefCollectionService.class, account);
   }

   /**
    * Retrieves the reference collection associated with the given entry. Note that all
    * entries are assumed to have a reference collection. On the first attempt to retrieve
    * a reference collection for a given entry, a new (empty) reference collection will
    * be created.
    *
    * @param ref A reference to an entry whose reference collection is to be retrieved
    * @return The reference collection for the supplied entry.
    */
   @Deprecated // TODO use application managed string identifiers
   ReferenceCollection get(EntryReference ref);

   /**
    * Retrieves the reference collection associated with the given entry. Note that all
    * entries are assumed to have a reference collection. On the first attempt to retrieve
    * a reference collection for a given entry, a new (empty) reference collection will
    * be created.
    *
    * @param id A globally unique identifier for the reference collection. Clients are
    *       expected to implement a scheme to generate these identifiers. UUIDs and
    *       tokenized representations of an {@code EntryReference} are recommended options.
    * @return The reference collection for the supplied entry.
    */
   ReferenceCollection get(String id);

   /**
    * Edits the reference collection associated with the referenced entry.
    * @param ref A reference to an entry whose reference collection should be edited
    * @return A command to edit the associated reference collection
    */
   @Deprecated // TODO use application managed string identifiers
   EditBibliographyCommand edit(EntryReference ref);

   /**
    * Edits the reference collection associated with the referenced entry.
    *
    * @param id A globally unique identifier of the reference collection to edit.
    * @return A command to edit the associated reference collection
    */
   EditBibliographyCommand edit(String id);

   /**
    * Removes the associated bibliography from the given reference if it exists.
    * @param ref
    * @return A CompletableFuture future that resolves to {@code true} if the database was modified, and {@code false} otherwise.
    */
   @Deprecated // TODO use application managed string identifiers

   CompletableFuture<Boolean> delete(EntryReference ref);

   /**
    * Removes the associated bibliography from the given reference if it exists.
    * @param id A globally unique identifier of the reference collection to remove.
    * @return A CompletableFuture that resolves to {@code true} if the database was modified,
    *       and {@code false} otherwise.
    */
   CompletableFuture<Boolean> delete(String id);
}
