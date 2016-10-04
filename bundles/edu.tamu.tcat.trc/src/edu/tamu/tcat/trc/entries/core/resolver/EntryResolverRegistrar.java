package edu.tamu.tcat.trc.entries.core.resolver;


public interface EntryResolverRegistrar
{
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
