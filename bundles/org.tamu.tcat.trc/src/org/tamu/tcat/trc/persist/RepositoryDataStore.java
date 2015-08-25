package org.tamu.tcat.trc.persist;

import java.util.Set;

/**
 *  Responsible for managing access to the underlying data storage systems (for example,
 *  database tables) for a defined RepositorySchema.
 *
 *  <p>
 *  This is the primary service for obtaining instances of a {@link DocumentRepository}.
 *
 *  @see DocumentRepository
 *  @deprecated Should use storage layer-specific builders instead
 */
@Deprecated
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
    * Indicates whether a repository is registered for the supplied id.
    *
    * @param repoId The unique identifier for the repository to check
    * @return {@code true} if a repository has been registered for this schema.
    */
   boolean isRegistered(String repoId);

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
   <RecordType, EditorType> String registerRepository(RepositoryConfiguration<StorageType, RecordType, EditorType> config) throws RepositoryException;

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
}
