package edu.tamu.tcat.trc.resources.books.resolve;


/**
 *  Responsible for resolving detailed information about a specific digital copy.
 *
 *  @param T
 */
public interface CopyResolverStrategy<T extends DigitalCopy>
{
   // NOTE this is expected to be used as a concrete sub-type. That is, clients will request and
   //      use the HathiTrustCopyResolver. The returned types provide information that is specific
   //      to their internal representation.

   // TODO what is this? Mediator, Strategy, Facade?
   // TODO provide authorization support


   /**
    * Indicates whether this strategy can resolve copies for the supplied identifier.
    *
    * @param identifier The unique identifier for the copy.
    * @return {@code true} If this copy provide recognizes and can attempt to resolve
    *    the supplied copy.
    */
   boolean canResolve(String identifier);

   /**
    * Retrieves detailed information about a specific digital copy.
    *
    * @param identifier An identifier for the digital copy to return.
    * @return The requested digital copy. Will not be {@code null}.
    *
    * @throws ResourceAccessException If the requested copy could not be found or accessed.
    * @throws IllegalArgumentException If the supplied identifier cannot be interpreted by
    *       this {@code CopyResolverStrategy}.
    */
   T resolve(String identifier) throws ResourceAccessException, IllegalArgumentException;



}
