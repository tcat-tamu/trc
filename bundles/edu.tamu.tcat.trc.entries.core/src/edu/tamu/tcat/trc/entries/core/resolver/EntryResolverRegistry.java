package edu.tamu.tcat.trc.entries.core;

/**
 * Maintains a registry of {@link EntryResolver}s for the various entry types
 * defined by the system.
 */
public interface EntryResolverRegistry
{
   // TODO how to handle multiple matching registrations?

   /**
    * Registers an {@link EntryResolver} to allow other system components
    * to restore entity references
    *
    * <p>
    * Note that, in general, multiple resolvers may be registered for the
    * same type {@code T} of entity. This might be needed, for example,
    * if two bibliographic entity repositories are defined, one for
    * local storage and one that connects to a remote system via REST.
    *
    * @param resolver The resolver to register.
    * @return A handle to use to unregister this resolver.
    */
   <T> Registration register(EntryResolver<T> resolver);

   /**
    * @param ref An {@link EntryReference} for which to obtain a resolver
    * @return An {@link EntryResolver} that accepts the supplied reference.
    * @throws InvalidReferenceException If no registered resolver accepts
    *       the supplied reference.
    */
   <T> EntryResolver<T> getResolver(EntryReference ref) throws InvalidReferenceException;


   /**
    * @param entry An TRC entry instance for which to obtain a resolver
    * @return An {@link EntryResolver} that accepts the supplied entry.
    * @throws InvalidReferenceException If no registered resolver accepts
    *       the supplied entry.
    */
   <T> EntryResolver<T> getResolver(T entry);

   /**
    * A handle that references a particular resolver registration.
    */
   public interface Registration
   {
      /**
       * Removes the associated {@link EntryResolver} from this registry.
       */
      void unregister();
   }
}
