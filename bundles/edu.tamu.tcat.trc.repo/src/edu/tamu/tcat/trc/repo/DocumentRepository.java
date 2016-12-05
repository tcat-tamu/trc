package edu.tamu.tcat.trc.repo;

import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

import edu.tamu.tcat.account.Account;

/**
 *  A simple document-oriented data store that performs basic CRUD operations over NoSQL-like
 *  database structures. A primary goal of the {@code DocumentRepository} is to provide a
 *  simplified API for data-persistence that allows applications to define domain model objects
 *  that closely match their needs without implementing the details of data-persistence. The core
 *  {@code DocumentRepository} implementation provides support for CRUD operations, notification
 *  of update events, caching, etc.
 *
 *  <p>Domain objects managed by the {@code DocumentRepository} are expected to be immutable.
 *  To allow flexibility between the domain model API and stored representation of the document, an
 *  intermediate DTO is used to represent the stored form of the record. These records are updated
 *  using an using an EditCommand that provides a domain specific API for modifying a record (via
 *  its stored representation) and encapsulates discrete updates to the record within the execution
 *  of an instance of the command object.
 *
 *  <p>This is designed to support applications with relatively straight-forward document
 *  storage needs that can be backed by a wide range of underlying technologies such as
 *  PostgreSQL, MongoDB, Amazon S3, etc. A repository is defined by a simple schema that
 *  specifies the name of the underlying data storage unit (a table name in a database), and
 *  optionally the names of the fields used to store metadata values (id, date created, date
 *  modified and status) as well as allowable status values.
 *
 *  <p>Note that one core use-case is to store data objects that are serialized as a text-based
 *  document such as JSON or XML. This detail, however, is implementation dependent.
 *  Implementations could use byte-oriented storage or other tools as needed, for example, to
 *  store image data, optimized JSON serialization or other formats.
 *
 *  <p>Applications are expected to select an implementation or implementation family that is
 *  appropriate to their needs and to configure that using the {@link DocRepoBuilder} provided by
 *  that implementation.
 *
 * @param <RecordType> The domain model associated with this repository.
 * @param <EditCommandType> The type of the edit command used to modify document records.
 *       Instances will be instantiated by the {@link EditCommandFactory} configured for
 *       this repository. These commands represent a transactional modification to the
 *       document record and will be executed or else will fail to execute as a unit.
 *       When instantiated, an edit command will be supplied an {@link ExecutableUpdateContext}
 *       that represents the update to be performed with the command. Command execution is
 *       achieved by invoking the {@link ExecutableUpdateContext#update(java.util.function.Function)}
 *       method of the supplied update context.
 */
public interface DocumentRepository<RecordType, EditCommandType>
{
   // TODO provide access to a more richly structured PagedResult API.
   // TODO use MD5 or SHA256 hashes to ensure data integrity, add crytography, etc. Use Jackson's SMILE format.
   // TODO Define simple integration with search/query system
   // TODO Provide simple property-based queries
   
   /**
    * Releases all resources associated with this repository. This method must be called once
    * the repository is no longer needed in order to allow it to clean up any resources that
    * it may have allocated such as executors or cached data.
    */
   void dispose();

   /**
    * @return An {@link Iterator} over all items in this repository.
    * @throws RepositoryException For errors accessing the underlying data store.
    */
   Iterator<RecordType> listAll() throws RepositoryException;

   Optional<RecordReference<RecordType>> getRecord(String id);

   /**
    * Retrieve the identified record.
    *
    * @param id The id of the record to return.
    * @return The identified record (will be empty if no record with the supplied id exists.
    * @throws RepositoryException If internal errors errors were encountered retrieving the document.
    */
   Optional<RecordType> get(String id);

   /**
    * Attempts to retrieve multiple records for a collection of record ids.
    *
    * @param ids The ids of the records to retrieve.
    * @return A collection of results corresponding to the supplied
    * @throws RepositoryException
    */
   Collection<RecordType> get(String... ids) throws RepositoryException;

   /**
    * Registers a task to be invoked after a document record is modified.
    *
    * @param observer Invoked when a document record is updated (created, modified or deleted).
    * @return A registration handle for use in removing this observer.
    */
   Runnable afterUpdate(RecordUpdateObserver<RecordType> observer);

   /**
    * Constructs a {@link RecordEditCommand} for use to create a new entry in this
    * repository.
    *
    * @param account The user account responsible for making this change.
    * @return A {@code DocumentEditorCommand} for use in editing the attributes of the object
    *       to be stored.
    */
   EditCommandType create(Account account);

   /**
    * Optional method to constructs a {@link RecordEditCommand} for use to create a new
    * entry with a client-supplied unique identifier. Note that the client is responsible for
    * ensuring that the supplied identifier is unique within the scope of this repository. If
    * it is not unique, the {@link RecordEditCommand#execute()} method will throw when
    * invoked.
    *
    * @apiNote This method is intended to be used for cases in which externally defined id's
    * are required in order to work with existing data sources. If this is required, the
    * developer should consult the documentation of the implementing repository to ensure that
    * this method is supported and to understand how duplicate identifiers are reported.
    *
    * @param account The user account responsible for making this change.
    * @param id The id of the record to create
    * @return A {@code DocumentEditorCommand} for use in editing the attributes of the object
    *       to be stored.
    * @throws UnsupportedOperationException If client-supplied identifiers are not supported.
    */
   EditCommandType create(Account account, String id) throws UnsupportedOperationException;

   /**
    * Constructs a {@link RecordEditCommand} for use in editing the identified record.
    *
    * @param account The user account responsible for making this change.
    * @param id The id of the record to edit.
    * @return A command for use in editing the record.
    * @throws RepositoryException If the identified record does not exist or if an edit command
    *       could not be constructed.
    */
   EditCommandType edit(Account account, String id) throws RepositoryException;

   /**
    * Removes the identified record from the repository. Often, repository implementations will
    * elect to mark a record as deleted rather than actually removing that record from the
    * underlying data storage system. This allows deleted records to be retained for historical
    * and data consistency purposes if needed.
    *
    * <p>
    * Deleted records must not be returned from the methods defined by this API. Repository
    * implementations that are capable of returning deleted record should provide additional
    * methods to support retrieval of those records.
    *
    * <p>
    * Implementations should include an implementation note that describes how they handle
    * record deletion and how they interpret the delete field of the {@link RepositorySchema}
    * in order to aid clients in selecting the correct repository implementation to meet their
    * needs.
    *
    * @param account The user account responsible for making this change.
    * @param id The unique identifier of the record to delete.
    * @return A {@link Future} that returns the result of this deletion. If the record is
    *       no longer contained in the repository the {@link Future#get()} will return a
    *       boolean to indicate if the data store was changed. If {@code true} the record was
    *       deleted by this operation. If {@code false}, the record either did not exist or had
    *       already been deleted. If errors are encountered in removing the requested item,
    *       the {@link Future#get()} method will propagate an {@link ExecutionException}
    *       that wraps the {@link RepositoryException} or {@link RuntimeException} depending
    *       on the nature of the error.
    *
    * @throws UnsupportedOperationException If the delete operation is not supported by this
    *       repository. This may reflect an inherent limitation of the repository or the way
    *       in which the {@link RepositorySchema} was configured.
    */
   CompletableFuture<Boolean> delete(Account account, String id) throws UnsupportedOperationException;

   /**
    * Convenience method to unwrap a {@link Future} and return the result or
    * package the underlying exception in a {@link RepositoryException}.
    *
    * @param result The future to be unwrapped.
    * @param message A message to supply if an exception was thrown by the future.
    *
    * @return The object returned by the future.
    *
    * @throws IllegalStateException If an {@link InterruptedException} or
    *       {@link TimeoutException} occurs while waiting on the supplied future.
    *       To avoid blocked threads, this will wait 10 minutes for the future to
    *       complete. For tasks that require longer execution, this method is not
    *       appropriate.
    * @throws RuntimeException If the future throws an {@link ExecutionException}
    *    whose cause is an unchecked exception, this method will propagate that
    *    exception directly.
    * @throws RepositoryException If the future throws an {@link ExecutionException}
    *    whose cause is a checked exception, this will wrap the checked exception in
    *    an unchecked {@link RepositoryException}.
    */
   public static <X> X unwrap(Future<X> result, Supplier<String> message)
      throws IllegalStateException, RepositoryException, RuntimeException
   {
      // HACK: ridiculously long timeout is better than nothing.
      try
      {
         return result.get(10, TimeUnit.MINUTES);
      }
      catch (InterruptedException | TimeoutException e)
      {
         String msg = message.get();
         throw new IllegalStateException(msg + " Failed to obtain result in a timely manner.", e);
      }
      catch (ExecutionException e)
      {
         Throwable cause = e.getCause();
         if (cause instanceof RuntimeException)
            throw (RuntimeException)cause;
         if (cause instanceof Error)
            throw (Error)cause;

         throw new RepositoryException(message.get(), e);
      }
   }
}
