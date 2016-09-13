package edu.tamu.tcat.trc.entries.core.repo;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import edu.tamu.tcat.account.Account;
import edu.tamu.tcat.trc.repo.NoSuchEntryException;

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
   /**
    *
    * @param id The id of the entry to return.
    * @return The entry to be returned
    * @throws NoSuchEntryException If the no entry with the supplied id exists.
    */
   EntryType get(String id) throws NoSuchEntryException;

   /**
    * @return An edit command to be used to create a new entry. The entry will not be
    *       created until the returned command is executed.
    */
   EditEntryCommand<EntryType> create();

   /**
    * Creates a new command with the application supplied id. While is is generally
    * preferable to allow the TRC framework to manage id generation, in some cases, the
    * application may require that an entry be associated with a specific id. In these cases
    * the application is responsible for maintaining the uniqueness of entry identifiers.
    * Duplicate ids will be handled in an implementation specific manner and may result in
    * inconsistent behavior including data loss.
    *
    * @param id The id of the entry to create.
    * @return An edit command to be used a new entry with the application supplied id.
    *       The entry will not be created until the command is executed.
    */
   EditEntryCommand<EntryType> create(String id);

   /**
    * @param id The id of the entry to edit.
    * @return A command for use to edit the identified entry.
    * @throws NoSuchEntryException If the no entry with the supplied id exists.
    */
   EditEntryCommand<EntryType> edit(String id) throws NoSuchEntryException;

   /**
    * Removes the identified entry.
    *
    * @param id The entry to remove
    * @return A {@link CompletableFuture} that, when resolved will return a value indicating
    *       whether the identified entry was found and removed. Entries that cannot be found
    *       will be considered removed (i.e., no exception will be thrown) but the return
    *       value can be used to check whether the operation changed the state of the underlying
    *       persistent data store.
    *
    *       <p>Any exception that may have prevented the proper execution of this request, will be
    *       propagated via the {@link ExecutionException} thrown by the future.
    */
   CompletableFuture<Boolean> remove(String id);

   /**
    *
    * @apiNote While {@link EntryRepository} implementations are intended to be thin
    *       proxies that wrap a user account and are easily created and destroyed, care
    *       should be taken to ensure that observers registered via this method are
    *       attached to the underlying repo implementation and are called regardless
    *       which proxy they are associated with.
    *
    * @param observer An observer that will be notified on changes Entries managed by
    *       this repository.
    * @return A registration handle that allows the observer to be removed.
    */
   ObserverRegistration onUpdate(UpdateObserver<EntryType> observer);

   /**
    * An observer to be notified when entries associated with this repository are changed.
    * Observers are intended as internal hooks to monitor for change and should be used with
    * care as they are executed outside the account scope of any particular repository.
    *
    * @param <T> The type of entry being observed.
    */
   @FunctionalInterface
   interface UpdateObserver<T>
   {
      void entryUpdated(EntryUpdateRecord<T> record);
   }

   /**
    * A registration handle that allows the registered update observer to be removed.
    */
   interface ObserverRegistration
   {
      /**
       * Stop sending update notifications to the registered observer.
       */
      void close();
   }
}
