package edu.tamu.tcat.trc.repo;

import java.util.function.Function;

/**
 * A builder for use in constructing a {@link DocumentRepository}. Applications that leverage
 * the {@code DocumentRepository} micro-framework for object persistence need to configure
 * the data store by providing two main components: an {@link EditCommandFactory} that can
 * be used to create EditCommand instances used to modify domain objects and an adapter
 * function that converts between the data structure used for internal storage and instances
 * of the immutable domain model that will be exposed to clients of the framework.
 *
 * <p>Applications that use the {@code DocumentRepository} should obtain a builder specific
 * for the implementation they intend to use. In many cases, it may be advisable to create
 * a simple provider class that will return properly initialized {@code DocRepoBuilder}
 * instances as this will make it easier to ensure consistency across multiple components
 * and to change the underlying implementation at a later date if needed.
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
