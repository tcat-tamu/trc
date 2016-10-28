package edu.tamu.tcat.trc.resolver;

import static java.text.MessageFormat.format;

import java.net.URI;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import edu.tamu.tcat.account.Account;

/**
 * A basic implementation of the {@link EntryResolverRegistry}. Intended to be registered as
 * a declarative service or otherwise accessed through dependency injection in order to ensure
 * that only one instance is present in the application.
 */
public class BasicResolverRegistry implements EntryResolverRegistry, EntryResolverRegistrar
{

   private final Map<UUID, EntryResolver<?>> resolvers = new ConcurrentHashMap<>();

   @Override
   public <T> EntryResolverRegistrar.Registration register(EntryResolver<T> resolver)
   {
      UUID registrationId = UUID.randomUUID();
      resolvers.put(registrationId, resolver);
      return () -> resolvers.remove(registrationId);
   }

   public <T> EntryReferenceProxy<T> getReference(String id, String type)
   {
      return new EntryRefImpl<>(id, type);
   }

   public <T> EntryReferenceProxy<T> getReference(String token)
   {
      EntryId eId = decodeToken(token);
      return new EntryRefImpl<>(eId.getId(), eId.getType());
   }

   public <T> EntryReferenceProxy<T> getReference(URI uri)
   {
      EntryId eId = resolvers.values().parallelStream()
            .filter(candidate -> candidate.accepts(uri))
            .findFirst()
            .map(resolver -> resolver.makeReference(uri))
            .orElseThrow(() -> new InvalidReferenceException(uri,
                  "No registered resolver accpets this uri"));

      return new EntryRefImpl<>(eId.getId(), eId.getType());
   }

   @Override
   @SuppressWarnings({ "unchecked", "rawtypes" })  // HACK: NOT TYPE SAFE
   public <T> EntryResolver<T> getResolver(EntryId ref) throws InvalidReferenceException
   {
      return (EntryResolver)resolvers.values().parallelStream()
         .filter(resolver -> resolver.accepts(ref.getId(), ref.getType()))
         .findAny()
         .orElseThrow(() -> new InvalidReferenceException(ref, "No registered resolver accpets this reference"));
   }

   @SuppressWarnings({ "unchecked", "rawtypes" })  // HACK: NOT TYPE SAFE
   public <T> EntryResolver<T> getResolver(String id, String type) throws InvalidReferenceException
   {
      return (EntryResolver)resolvers.values().parallelStream()
            .filter(resolver -> resolver.accepts(id, type))
            .findAny()
            .orElseThrow(() -> new InvalidReferenceException(id, type,
                  "No registered resolver accpets this reference"));
   }

   @Override
   @SuppressWarnings("unchecked")  // Type safety enforced by resolver's #accepts method
   public <T> EntryResolver<T> getResolver(T entry)
   {
      return (EntryResolver<T>)resolvers.values().parallelStream()
            .filter(resolver -> resolver.accepts(entry))
            .findAny()
            .orElseThrow(() -> new InvalidReferenceException((Object)null, "No registered resolver accpets this entry"));
   }

   @Override
   public String tokenize(EntryId eId)
   {
      return tokenize(eId.getId(), eId.getType());
   }

   public String tokenize(String id, String type)
   {
      // ensure that a resolver exists for this reference.
      this.getResolver(id, type);

      // HACK this is an arbitrary restriction on ids and may not be robust
      //      to future changes. Need a better tokenization strategy.
      if (id.contains("::"))
         throw new IllegalStateException("Cannot tokenize reference with id " + id);
      String key = id + "::" + type;
      return Base64.getEncoder().encodeToString(key.getBytes());
   }

   @Override
   public EntryId decodeToken(String token)
   {
      EntryId ref;
      byte[] bytes = Base64.getDecoder().decode(token);
      try
      {
         String key = new String(bytes, "UTF-8");
         int ix = key.indexOf("::");
         if (ix < 0 || ix >= key.length() - 3)
            throw new IllegalArgumentException(format("Invalid entry reference token {0}", token));

         ref = new EntryId(key.substring(0, ix), key.substring(ix + 2));
      }
      catch (Exception e)
      {
         throw new IllegalArgumentException(format("Invalid entry reference token {0}", token));
      }

      // ensure that a resolver exists for this reference.
      this.getResolver(ref);
      return ref;
   }

   private class EntryRefImpl<T> implements EntryReferenceProxy<T>
   {
      private final String id;
      private final String type;
      private final EntryResolver<T> resolver;

      public EntryRefImpl(String id, String type)
      {
         this.id = id;
         this.type = type;
         this.resolver = getResolver(id, type);
      }

      @Override
      public String getId()
      {
         return id;
      }

      public EntryId asEntryId()
      {
         return new EntryId(id, type);
      }

      @Override
      public String getType()
      {
         return type;
      }

      @Override
      public String getToken()
      {
         return tokenize(id, type);
      }

      @Override
      public URI getUri()
      {
         return resolver.toUri(asEntryId());
      }

      @Override
      public T getEntry(Account account)
      {
         return resolver.resolve(account, asEntryId());
      }

   }

}
