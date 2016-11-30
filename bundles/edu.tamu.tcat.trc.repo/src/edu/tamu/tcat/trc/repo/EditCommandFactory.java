package edu.tamu.tcat.trc.repo;

import java.util.Optional;

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
    * Constructs an edit command.
    *
    * @param strategy A context object that provides information about this update and a
    *    hook to execute the applied changes.
    *
    * @return A command object to be used to edit the referenced record.
    */
   EditCmdType create(ExecutableUpdateContext<StorageType> ctx);

   /**
    * Initializes a new data record based on an optionally supplied original record.
    *
    * @param id The id of the entry to be returned (in case the original value is not supplied).
    * @param original The current value of this record within the repository.
    *
    * @return An initialized copy of the original or newly instantiated DTO. The returned
    *       value must be isolated from the original such that changes to one instance
    *       do not affect the other.
    */
   StorageType initialize(String id, Optional<StorageType> original);

}