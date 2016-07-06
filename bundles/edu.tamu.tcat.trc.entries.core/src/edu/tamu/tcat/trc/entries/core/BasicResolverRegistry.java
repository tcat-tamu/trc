package edu.tamu.tcat.trc.entries.core;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A basic implementation of the {@link EntryResolverRegistry}. Intended to be registered as
 * a declarative service or otherwise accessed through dependency injection in order to ensure
 * that only one instance is present in the application.
 */
public class BasicResolverRegistry implements EntryResolverRegistry
{

   private final Map<UUID, EntryResolver<?>> resolvers = new ConcurrentHashMap<>();

   @Override
   public <T> Registration register(EntryResolver<T> resolver)
   {
      UUID registrationId = UUID.randomUUID();
      resolvers.put(registrationId, resolver);
      return () -> resolvers.remove(registrationId);
   }

   @Override
   @SuppressWarnings({ "unchecked", "rawtypes" })  // HACK: NOT TYPE SAFE
   public <T> EntryResolver<T> getResolver(EntryReference ref) throws InvalidReferenceException
   {
      return (EntryResolver)resolvers.values().parallelStream()
         .filter(resolver -> resolver.accepts(ref))
         .findAny()
         .orElseThrow(() -> new InvalidReferenceException(ref, "No registered resolver accpets this reference"));
   }

   @Override
   @SuppressWarnings("unchecked")  // Type safety enforced by resolver's #accepts method
   public <T> EntryResolver<T> getResolver(T entry)
   {
      return (EntryResolver<T>)resolvers.values().parallelStream()
            .filter(resolver -> resolver.accepts(entry))
            .findAny()
            .orElseThrow(() -> new InvalidReferenceException(null, "No registered resolver accpets this entry"));
   }

}
