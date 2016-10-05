package edu.tamu.tcat.trc.services.bibref.repo;

import java.util.concurrent.CompletableFuture;

import edu.tamu.tcat.account.Account;
import edu.tamu.tcat.trc.resolver.EntryReference;
import edu.tamu.tcat.trc.services.BasicServiceContext;
import edu.tamu.tcat.trc.services.ServiceContext;
import edu.tamu.tcat.trc.services.bibref.ReferenceCollection;

public interface ReferenceRepository
{

   static ServiceContext<ReferenceRepository> makeContext(Account account)
   {
      return new BasicServiceContext<>(ReferenceRepository.class, account);
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
    * Edits the reference collection associated with the referenced entry.
    * @param ref A reference to an entry whose reference collection should be edited
    * @return A command to edit the associated reference collection
    */
   @Deprecated // TODO use application managed string identifiers
   EditBibliographyCommand edit(EntryReference ref);

   /**
    * Removes the associated bibliography from the given reference if it exists.
    * @param ref
    * @return A completable future that resolves to {@code true} if the database was modified, and {@code false} otherwise.
    */
   @Deprecated // TODO use application managed string identifiers
   CompletableFuture<Boolean> delete(EntryReference ref);
}
