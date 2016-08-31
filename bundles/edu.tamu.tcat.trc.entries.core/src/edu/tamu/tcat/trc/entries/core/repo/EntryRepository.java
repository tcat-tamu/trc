package edu.tamu.tcat.trc.entries.core.repo;

import java.util.concurrent.CompletableFuture;

import edu.tamu.tcat.account.Account;

/**
 * Defines a repository used to manage access to a specific class of entries within a
 * thematic research collection (TRC). An entry represents a specific scholarly contribution.
 *
 * TODO explain what an Entry is in the notion of TRC. Note desire to support Domain Driven Design
 *      when implementing specific classes of functionality.  Note the desire to create discrete
 *      classes of entries that can be relatively self-contained. ad the use of entry resolvers
 *      to mediate between repos.
 *
 * <p>
 * Instances of an {@link EntryRepository} are obtained from the {@link EntryRepositoryRegistry}
 * and are scoped to a specific user {@link Account}. All interactions with the repository are
 * performed
 *
 * @apiNote This is designed so that specific entry repositories may provide an API with no dependencies
 *    on the account API. Implementations are then free to implement any required account-specific
 *    functionality such as permission controls, recording editorial authority, user-owned resources,
 *    etc.  The actual repository instance returned by the {@link EntryRepositoryRegistry} is then
 *
 * @param <EntryType> The type of TRC entry managed by this repository.
 */
public interface EntryRepository<EntryType>
{
   EntryType get(String id);

   EditEntryCommand<EntryType> create();

   EditEntryCommand<EntryType> create(String id);

   EditEntryCommand<EntryType> edit(String id);

   CompletableFuture<Boolean> remove(String id);

   /**
    *
    * @param observer An observer that will be notified on changes Entries managed by
    *       this repository.
    * @return A registration handle that allows the observer to be removed.
    */
   ObserverRegistration onUpdate(UpdateObserver<EntryType> observer);

   @FunctionalInterface
   interface UpdateObserver<T>
   {
      void entryUpdated(EntryUpdateRecord<T> record);
   }


   interface ObserverRegistration
   {
      /**
       * Stop sending update notifications to the registered listener.
       */
      void close();
   }
}
