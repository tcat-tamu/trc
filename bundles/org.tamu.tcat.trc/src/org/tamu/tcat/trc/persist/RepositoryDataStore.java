package org.tamu.tcat.trc.persist;

import java.util.Set;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 *  Responsible for managing access to the underlying data storage systems (for example,
 *  database tables) for a defined RepositorySchema.
 *
 *  <p>
 *  This is the primary service for obtaining instances of a {@link DocumentRepository}.
 *
 *  @see DocumentRepository
 */
public interface RepositoryDataStore<StorageType>
{
   /**
    * Checks to see if the supplied schema exists on the underlying data storage system.
    *
    * @param schema The schema to check for
    * @return {@code true} if the supplied schema exists, {@code false} if it does not.
    * @throws RepositoryException If errors are encountered while checking for the existance of
    *       this schema.
    */
   boolean exists(RepositorySchema schema) throws RepositoryException;

   /**
    * Attempts to create the schema on the underlying data storage system, if it does not
    * exist. Note that creating a schema does not create a repository that use that schema.
    *
    * <p>
    * Schemas are intended to be general purpose definitions of a repository that can be freely
    * exchanged between different types of data storage implementations. The specific structures
    * used to instantiate the supplied schema depend on the structure of the underlying data
    * storage mechanism. For example, a data store backed by the filesystem might ignore the
    * supplied creation and modification descriptors and use the built-in filesystem mechanisms
    * to record this information.
    *
    * <p>
    * Consequently the schema should be understood as a tool that offers limited support to
    * hint at the intended structure of the underlying data storage mechanisms and, where
    * supported, to allow access with existing data structures that may have been created (and
    * may be used) externally to the data store API.
    *
    * <p>
    * Implementations should describe how they translate schema configurations into the
    * structures of their underlying data storage system in an implementation note.
    *
    * @param schema The schema to be created.
    * @return {@code true} if the data store was modified (that is, if the schema was created),
    *    {@code false} if it was not modified. Note that, following a successful return from
    *    this method, the schema will exist on the underlying data store. Any failure to create
    *    the schema will result in a repository exception.
    * @throws RepositoryException If the attempt to create the schema fails.
    */
   boolean create(RepositorySchema schema) throws RepositoryException;

   /**
    * Indicates whether a repository is registered for the supplied schema id.
    *
    * @param repoId The unique identifier for the repository to check
    * @return {@code true} if a repository has been registered for this schema.
    */
   boolean isRegistered(String repo);

   /**
    * @return The id's of all registered repositories.
    */
   Set<String> listRepositories();

   /**
    * Attempts to instantiate and register a repository for the supplied schema.
    *
    * <p>
    * @apiNote We separate the process of registering a repository definition from retrieving
    *    it since registration requires that the caller be able to obtain a schema and data
    *    adapter. Once this configuration has been supplied, clients can obtain a repository
    *    by using the string valued repo id used to register the repo.
    *
    * @param config Configuration definition for the repository to register..
    * @return The identifier for the registered repository.
    * @throws RepositoryException If the repository could not be registered for this data
    *       store. Notably this will throw if the supplied schema does not exist within this
    *       data store or if a repository has already been registered for the supplied repo id.
    */
   // TODO can more than one repository use the same schema?
   <RecordType> String registerRepository(RepositoryConfiguration<StorageType, RecordType> config) throws RepositoryException;

   /**
    * Returns an instance of the requested repository
    * @param repoId The id of the repository to return
    * @param type A type token for the type domain objects to be managed by the repository.
    * @return
    *
    * @throws RepositoryException If the repository cannot be returned. This will typically
    *       occur if no repository has been registered for the supplied id or if the supplied
    *       data type does not match the registered repository.
    */
   <RecordType> DocumentRepository<StorageType, RecordType> get(String repoId, Class<RecordType> type) throws RepositoryException;

   /**
    * Defines the configuration information required to register a {@link DocumentRepository}
    * with a {@link RepositoryDataStore}.
    *
    * @param <StorageType> The data type used by the data store to represent the data objects
    *       internally.
    * @param <RecordType> The type of the Java object that is stored and produced by a repository.
    */
   interface RepositoryConfiguration<StorageType, RecordType>
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
       * @return A {@link Function} that will convert instances of the internal data storage
       *    type into instances of the public data model to be produced by the repository.
       */
      Function<StorageType, RecordType> getDataAdapter();

      /**
       * @return The java type of records managed by the repository.
       */
      Class<RecordType> getRecordType();

      /**
       * @return A factory for use in generating edit commands.
       */
      EditCommandFactory<StorageType, RecordType> getEditCommandFactory();
   }

   /**
    * Supplied to the repository during the initial configuration in order to be used to create
    * new {@link RecordEditCommand}s. This allows the client application to provide an API for
    * updating records that can connect into the underlying data storage layer managed by
    * the repository.
    *
    * @param <S> The repository's internal data storage type
    * @param <R> The record type produced by the repository
    */
   interface EditCommandFactory<S, R>
   {
      // TODO ideally, I'd remove the API for RecordEditCommand and let this be a POJO.

      /**
       * Constructs a {@link RecordEditCommand} to be used to create a new record.
       *
       * @param commitHook A function that accepts data in the internal storage format of the
       *       repository (e.g., stringified JSON, InputStream, data transfer object, etc).
       *       When the command is executed, it must invoke one of the {@link CommitHook#submit}
       *       methods with the data to be stored in the repository's underlying data storage system.
       *
       * @return A command object to be used to edit the newly created record.
       */
      RecordEditCommand create(CommitHook<S> commitHook);

      /**
       * Constructs a {@link RecordEditCommand} to be used to create a new record.
       *
       * @param id A client supplied identifier for the record to create.
       * @param commitHook A function that accepts data in the internal storage format of the
       *       repository (e.g., stringified JSON, InputStream, data transfer object, etc).
       *       When the command is executed, it must invoke one of the {@link CommitHook#submit}
       *       methods with the data to be stored in the repository's underlying data storage system.
       *
       * @return A command object to be used to edit the newly created record.
       */
      RecordEditCommand create(String id, CommitHook<S> commitHook);

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
       * @param id
       * @param commitHook
       * @param currentState A {@link Supplier} that will always return the internal
       *       representation of the object currently being edited.
       * @return
       */
      RecordEditCommand edit(String id, CommitHook<S> commitHook, Supplier<S> currentState);

      // TODO how can we support write locking. I'd like to provide a lock(id) method on the
      //      commit hook that will cause the currentState supplier to block until the commit
      //      has completed (or a timeout is reached).
   }

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
   interface CommitHook<StorageType>
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
}
