package org.tamu.tcat.trc.persist;

import java.util.concurrent.Future;

/**
 * Supplied to {@link EditCommandFactory} methods by the {@link DocumentRepository}
 * implementation for use in persisting records in the underlying data storage mechanism.
 *
 * <p>
 * The {@code CommitHook} is intended to be a single use construct. Once {@code #submit}
 * has been called, any future calls will throw an {@link IllegalStateException}.
 *
 * @param <StorageType> The internal data type used by the persistence layer.
 */
public interface CommitHook<StorageType>
{
   /**
    * Submits the record data in the repository's internal storage format for persistence
    * to the data store.
    *
    * @param data the record data to be saved.
    * @param changeSet A client defined change set that documents the specific changes made
    *       to the updated record.
    * @return A {@link Future} that will return the unique identifier for the record. Errors
    *       in attempting to save the data to the underlying storage layer will be propagated
    *       as a {@link RepositoryException} when {@link Future#get()} is called.
    */
   Future<String> submit(StorageType data, Object changeSet);

   /**
    * Submits the record data in the repository's internal storage format for persistence
    * to the data store.
    *
    * @implNote This invokes {@link #submit(Object, Object)} with the supplied storage type
    *       as both the data and the change set.
    *
    * @param data the record data to be saved.
    * @return A {@link Future} that will return the unique identifier for the record. Errors
    *       in attempting to save the data to the underlying storage layer will be propagated
    *       as a {@link RepositoryException} when {@link Future#get()} is called.
    */
   default Future<String> submit(StorageType data)
   {
      return submit(data, data);
   };
}