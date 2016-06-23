package edu.tamu.tcat.trc.repo;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * Supplied to the repository during the initial configuration in order to be used to create
 * new {@link RecordEditCommand}s. This allows the client application to provide an API for
 * updating records that can connect into the underlying data storage layer managed by
 * the repository.
 *
 * @param <StorageType> The repository's internal data storage type
 * @param <EditCmdType> The type of the edit command object produced by this factory
 */
public interface EditCommandFactory<StorageType, EditCmdType>
{

   /**
    * Constructs an edit command to be used to create a new record.
    *
    * @param id The identifier for the record being created.
    * @param strategy The a strategy to apply the updates to the persistence layer.
    *
    * @return A command object to be used to edit the newly created record.
    */
   EditCmdType create(String id, UpdateStrategy<StorageType> strategy);

   /**
    * Constructs an edit command to be used to edit an existing record.
    *
    * @param id The identifier for the record to be edited.
    * @param strategy The a strategy to apply the updates to the persistence layer.
    *
    * @return A command object to be used to edit the identified record.
    */
   EditCmdType edit(String id, UpdateStrategy<StorageType> strategy);

   /**
    * A strategy supplied to an edit update command that will asynchronously apply updates
    * to the persistence layer.
    *
    * @param <StorageType> The type of record to be stored in the persistence layer. This
    *       should be a JSON serializable struct.
    */
   public interface UpdateStrategy<StorageType>
   {
      /**
       * Called by the edit command to enact the edits within the persistence layer.
       * Takes a {@link Function} that will generate a new DTO for the updated entry
       * based on an {@link UpdateContext} supplied by the {@link DocumentRepository}.
       *
       * <p>
       * While there are many options for implementing edit commands and generator
       * functions, this is designed to be easy to use in combination with the {@link ChangeSet}
       * APi. To do this, the generator function should create a new DTO instance based
       * the original (pre-commit) DTO value supplied by the {@link UpdateContext}. Note
       * that this instance MUST be copied such that changes to the new DTO do not
       * accidentally modify the original DTO. Once the new DTO instance has been created
       * the changes can be applied using {@link ChangeSet#apply(Object)} and the resulting
       * object returned from the generator.
       *
       * @param generator A function supplied by the edit command to generate the updated
       *    DTO to be persisted by the {@code DocumentRepository}
       *
       * @return A future that will be completed once object has been stored in the
       *    persistence layer. Note that it is currently an implementation detail as whether
       *    post-commit hooks will have completed execution prior to returning this result.
       *
       * TODO perhaps return the UpdateContext and allow callers to wait for persistence
       *   using {@link UpdateContext#getModified()} or call/monitor other commit actions as
       *   they occur.
       */
      CompletableFuture<StorageType> update(Function<UpdateContext<StorageType>, StorageType> generator);
   }


   // TODO how can we support write locking. I'd like to provide a lock(id) method on the
   //      commit hook that will cause the currentState supplier to block until the commit
   //      has completed (or a timeout is reached).
}