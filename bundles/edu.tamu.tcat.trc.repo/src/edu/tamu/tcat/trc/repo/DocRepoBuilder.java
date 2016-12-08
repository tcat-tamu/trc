package edu.tamu.tcat.trc.repo;

import java.util.function.Function;

/**
 * A builder for use in constructing a document repository. Note that while this is expected
 * primarily to support document oriented storage of JSON-records within an NoSQL (Not only SQL)
 * database and the documentation and terminology reflect that use-case, the API is intended to
 * be useful for other storage technologies as well (e.g., file system or Amazon S3).
 *
 * @param <RecordType> The Java type of the domain model object.
 * @param <StorageType> The JSON serializable data type that will be stored. In most cases, this
 *       should be a simple struct-like object that can be easily converted to JSON or a similarly
 *       widely-used format for persistence.
 * @param <EditCmdType> The Java-type of the command that will be used to edit new and existing
 *       instances of objects stored in this repository.
 */
public interface DocRepoBuilder<RecordType, StorageType, EditCmdType>
{

   /**
    * @param persistenceId The identifier that designates where records managed by this repository
    *       should be stored. For example, a database-backed identifier may use the database
    *       table name.
    * @return A reference to this builder to support fluent configuration.
    */
   DocRepoBuilder<RecordType, StorageType, EditCmdType> setPersistenceId(String persistenceId);

   /**
    * @param type A type token for the data storage POJO. 
    * @return A reference to this builder to support fluent configuration.
    */
   DocRepoBuilder<RecordType, StorageType, EditCmdType> setStorageType(Class<StorageType> type);

   /**
    * @param adapter A {@link Function} that will convert instances of the internal data storage
    *    type into instances of the public data model to be produced by the repository.
    * @return A reference to this builder to support fluent configuration.
    */
   DocRepoBuilder<RecordType, StorageType, EditCmdType> setDataAdapter(Function<StorageType, RecordType> adapter);

   /**
    * @param cmdFactory A factory for use in generating edit commands.
    * @return A reference to this builder to support fluent configuration.
    */
   DocRepoBuilder<RecordType, StorageType, EditCmdType> setEditCommandFactory(EditCommandFactory<StorageType, EditCmdType> cmdFactory);

   /**
    * @param value Indicates whether the builder should create the backing database table
    *       if it does not exist. This is {@code false} by default.
    * @return A reference to this builder to support fluent configuration.
    */
   DocRepoBuilder<RecordType, StorageType, EditCmdType> setEnableCreation(boolean value);

   /**
    * @return The built document repository.
    * @throws RepositoryException
    */
   DocumentRepository<RecordType, EditCmdType> build() throws RepositoryException;

}
