package edu.tamu.tcat.trc.entries.core.repo;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 *  Base interface for edit commands. Entries within the TRC framework are immutable objects.
 *  They care modified, either during initial creation or to update existing entries via an
 *  edit command. These commands represent a transactional modification to the object and
 *  will be executed or else will fail to execute as a unit. Commands are executed within the
 *  scope of a single actor (and Account holder).
 *
 *  <p>
 *  Sub-elements of an entry (for example, structured references to a list of authors, or
 *  editions to a book) may be modified using mutators.
 *
 *
 *  @param <EntryType> The type of entry that is edited by this command.
 */
public interface EditEntryCommand<EntryType>
{


   /**
    * Executes this command. All changes made using this command will take effect
    * only upon successful execution.
    *
    * @return A future that resolves to the id of the updated entry. If the execution fails
    *       for any reason, {@link CompletableFuture#get()} will throw an
    *       {@link ExecutionException} that wraps the cause of the failure.
    */
   CompletableFuture<String> execute();
}
