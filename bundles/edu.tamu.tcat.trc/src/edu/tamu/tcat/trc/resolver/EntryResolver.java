package edu.tamu.tcat.trc.resolver;

import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import edu.tamu.tcat.account.Account;


/**
 * Used to convert between {@link EntryId}s and the instantiated entry
 * that they identify and as a factory to construct an {@code EntryReference}
 * for a given entry instance. {@code EntryResolver}s are intended to be defined
 * and registered by the repository that is responsible for managing a particular
 * type of entry.
 *
 * <p>
 * The {@link #accepts} methods are used to determine if a particular resolver
 * is applicable for a given {@link URI}, {@link EntryId} or {@code Object}.
 * This is designed to allow the {@link EntryResolverRegistry} to implement a
 * Chain of Responsibility pattern to correctly handle arbitrary entries.
 * Implementations should ensure that these methods return quickly.
 *
 * @param <T> The type of entry produced by this resolver.
 */
public interface EntryResolver<T>
{
   /**
    * @return The Java type associated with this resolver.
    */
   Class<T> getType();

   /**
    * @param account A reference to the user (or other actor) account that is requesting
    *       access to this resource. May be {@code null}.
    * @param reference A reference to resolve.
    * @return An instance of the referenced entry.
    * @throws InvalidReferenceException If the supplied reference cannot be resolved.
    */
   T resolve(Account account, EntryId reference) throws InvalidReferenceException;

   /**
    * Attempts to remove the supplied entry from its associated data store.
    *
    * @param account A reference to the user (or other actor) account that is requesting
    *       to remove this resource. May be {@code null}.
    * @param reference A reference to an entry to remove.
    * @return A future that resolves to indicate whether the underlying data store was
    *       changed. On successful completion, the referenced entry will no longer be
    *       available in the repository (although it may be retained as a deleted entry for
    *       historical and versioning purposes). Execution errors will be propagated via an
    *       {@link ExecutionException}.
    *
    * @throws InvalidReferenceException If the supplied reference cannot be resolved.
    * @throws UnsupportedOperationException If this resolver does not support removal.
    */
   default CompletableFuture<Boolean> remove(Account account, EntryId reference)
         throws InvalidReferenceException, UnsupportedOperationException
   {
      throw new UnsupportedOperationException();
   }

   /**
    * Constructs a URI for the supplied reference.
    *
    * @param reference The reference for which to construct a URI.
    * @return A URI for the referenced entry.
    * @throws InvalidReferenceException If this resolver cannot create a URI
    *       for the supplied reference.
    */
   URI toUri(EntryId reference) throws InvalidReferenceException;

   /**
    * @param instance The instance for which a label should be supplied.
    * @return A display label for the supplied instance.
    */
   String getLabel(T instance);

   /**
    * @param instance The instance for which a label should be supplied.
    * @return An HTML formatted display label for the supplied instance. Implementations
    *       are expected to supply light, semantic markup for the label an rely on the
    *       client to supply any additional visual styling.
    */
   default String getHtmlLabel(T instance) {
      return getLabel(instance);
   }

   /**
    * Creates an {@link EntryId} from a given TRC entry.
    * @param instance The entry for which to construct a reference.
    * @return The constructed reference.
    * @throws InvalidReferenceException If this resolver cannot construct
    *       an {@link EntryId}.
    */
   EntryId makeReference(T instance) throws InvalidReferenceException;

   /**
    * Constructs a reference for a given URI.
    *
    * @param uri The URI to resolve.
    * @return An entry reference for the supplied URI.
    * @throws InvalidReferenceException It this resolver cannot construct
    *       an {@link EntryId}.
    */
   EntryId makeReference(URI uri) throws InvalidReferenceException;

   /**
    * Indicates whether this resolver can construct an {@link EntryId}
    * for the supplied object.
    *
    * @param obj The object to be tested.
    * @return {@code true} If a reference can be created.
    */
   boolean accepts(Object obj);

   default boolean accepts(String id, String type)
   {
      EntryId ref = new EntryId(id, type);
      return this.accepts(ref);
   }

   /**
    * Indicates whether this resolver can resolve the supplied {@link EntryId}.
    *
    * @param ref The reference to be tested.
    * @return {@code true} If the reference can be resolved.
    */
   boolean accepts(EntryId ref);

   /**
    * Indicates whether this resolver can construct an {@link EntryId}
    * for the supplied URI.
    *
    * @param uri The URI to be tested.
    * @return {@code true} If a reference can be created.
    */
   boolean accepts(URI uri);
}
