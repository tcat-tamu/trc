package edu.tamu.tcat.trc.entries.core.resolver;

import java.net.URI;

import edu.tamu.tcat.account.Account;
import edu.tamu.tcat.trc.entries.core.InvalidReferenceException;


/**
 * Used to convert between {@link EntryReference}s and the instantiated entry
 * that they identify and as a factory to construct an {@code EntryReference}
 * for a given entry instance. {@code EntryResolver}s are intended to be defined
 * and registered by the repository that is responsible for managing a particular
 * type of entry.
 *
 * <p>
 * The {@link #accepts} methods are used to determine if a particular resolver
 * is applicable for a given {@link URI}, {@link EntryReference} or {@code Object}.
 * This is designed to allow the {@link EntryResolverRegistry} to implement a
 * Chain of Responsibility pattern to correctly handle arbitrary entries.
 * Implementations should ensure that these methods return quickly.
 *
 * @param <T> The type of entry produced by this resolver.
 */
public interface EntryResolver<T>
{

   /**
    * Configuration parameter that specifies the REST API endpoint for use in
    * creating and interpreting TRC entry URIs.
    */
   static final String API_ENDPOINT_PARAM = "trc.api.endpoint";


   /**
    * @param reference A reference to resolve.
    * @return An instance of the referenced entry.
    * @throws InvalidReferenceException If the supplied reference cannot be resolved.
    * @deprecated Use {@link #resolve(Account, EntryReference)}
    */
   @Deprecated
   default T resolve(EntryReference reference) throws InvalidReferenceException
   {
      return resolve(null, reference);
   }

   /**
    * @param account A reference to the user (or other actor) account that is requesting
    *       access to this resource. May be {@code null}.
    * @param reference A reference to resolve.
    * @return An instance of the referenced entry.
    * @throws InvalidReferenceException If the supplied reference cannot be resolved.
    */
   T resolve(Account account, EntryReference reference) throws InvalidReferenceException;

   /**
    * Constructs a URI for the supplied reference.
    *
    * @param reference The reference for which to construct a URI.
    * @return A URI for the referenced entry.
    * @throws InvalidReferenceException If this resolver cannot create a URI
    *       for the supplied reference.
    */
   URI toUri(EntryReference reference) throws InvalidReferenceException;

   /**
    * Creates an {@link EntryReference} from a given TRC entry.
    * @param instance The entry for which to construct a reference.
    * @return The constructed reference.
    * @throws InvalidReferenceException If this resolver cannot construct
    *       an {@link EntryReference}.
    */
   EntryReference makeReference(T instance) throws InvalidReferenceException;

   /**
    * Constructs a reference for a given URI.
    *
    * @param uri The URI to resolve.
    * @return An entry reference for the supplied URI.
    * @throws InvalidReferenceException It this resolver cannot construct
    *       an {@link EntryReference}.
    */
   EntryReference makeReference(URI uri) throws InvalidReferenceException;

   /**
    * Indicates whether this resolver can construct an {@link EntryReference}
    * for the supplied object.
    *
    * @param obj The object to be tested.
    * @return {@code true} If a reference can be created.
    */
   boolean accepts(Object obj);

   /**
    * Indicates whether this resolver can resolve the supplied {@link EntryReference}.
    *
    * @param ref The reference to be tested.
    * @return {@code true} If the reference can be resolved.
    */
   boolean accepts(EntryReference ref);

   /**
    * Indicates whether this resolver can construct an {@link EntryReference}
    * for the supplied URI.
    *
    * @param uri The URI to be tested.
    * @return {@code true} If a reference can be created.
    */
   boolean accepts(URI uri);
}
