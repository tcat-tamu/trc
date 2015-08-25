package org.tamu.tcat.trc.persist;

import java.util.function.Supplier;

/**
 * Supplied to the repository during the initial configuration in order to be used to create
 * new {@link RecordEditCommand}s. This allows the client application to provide an API for
 * updating records that can connect into the underlying data storage layer managed by
 * the repository.
 *
 * @param <S> The repository's internal data storage type
 * @param <R> The record type produced by the repository
 */
public interface EditCommandFactory<S, R, EditorType>
{
   // TODO ideally, I'd remove the API for RecordEditCommand and let this be a POJO.

   /**
    * Constructs a {@link RecordEditCommand} to be used to create a new record.
    *
    * @param id The identifier for the record being created. This must not be modified by
    *       the returned command.
    * @param commitHook A function that accepts data in the internal storage format of the
    *       repository (e.g., stringified JSON, InputStream, data transfer object, etc).
    *       When the command is executed, it must invoke one of the {@link CommitHook#submit}
    *       methods with the data to be stored in the repository's underlying data storage system.
    *
    * @return A command object to be used to edit the newly created record.
    */
   EditorType create(String id, CommitHook<S> commitHook);

   /**
    * Constructs a {@link RecordEditCommand} to be used to edit an existing record. In
    * addition to a commit hook used to submit the updated record to the repository for
    * persistence, this method will be provided with a {@link Supplier} that can be used
    * to obtain the current state of the object being edited from the data storage layer.
    *
    * <p>
    * It is recommended that implementations store updates to the record that is being
    * edited and obtain a
    *
    * @param id The id of the record to edit
    * @param currentState A {@link Supplier} that will always return the internal
    *       representation of the object currently being edited.
    * @param commitHook A function that accepts data in the internal storage format of the
    *       repository (e.g., stringified JSON, InputStream, data transfer object, etc).
    *       When the command is executed, it must invoke one of the {@link CommitHook#submit}
    *       methods with the data to be stored in the repository's underlying data storage system.
    * @return A command object to be used to edit the identified record.
    */
   EditorType edit(String id, Supplier<S> currentState, CommitHook<S> commitHook);

   // TODO how can we support write locking. I'd like to provide a lock(id) method on the
   //      commit hook that will cause the currentState supplier to block until the commit
   //      has completed (or a timeout is reached).
}