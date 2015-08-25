package org.tamu.tcat.trc.persist;

import java.util.function.Function;

/**
 * Defines the configuration information required to register a {@link DocumentRepository}
 * with a {@link RepositoryDataStore}.
 *
 * @param <StorageType> The data type used by the data store to represent the data objects
 *       internally.
 * @param <RecordType> The type of the Java object that is stored and produced by a repository.
 */
public interface RepositoryConfiguration<StorageType, RecordType, EditorType>
{
   /**
    * @return A unique identifier for a repository.
    */
   String getId();

   /**
    * @return The schema to use for the underlying data storage definition.
    */
   RepositorySchema getSchema();

   /**
    * @return The java type of records managed by the repository.
    */
   Class<RecordType> getRecordType();

   /**
    * @return A {@link Function} that will convert instances of the internal data storage
    *    type into instances of the public data model to be produced by the repository.
    */
   Function<StorageType, RecordType> getDataAdapter();

   /**
    * @return A factory for use in generating edit commands.
    */
   EditCommandFactory<StorageType, RecordType, EditorType> getEditCommandFactory();
}