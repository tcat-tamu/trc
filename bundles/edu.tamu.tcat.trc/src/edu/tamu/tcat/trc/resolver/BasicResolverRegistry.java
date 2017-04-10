package edu.tamu.tcat.trc.resolver;

import static java.text.MessageFormat.format;

import java.net.URI;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import edu.tamu.tcat.account.Account;
import edu.tamu.tcat.trc.ResourceNotFoundException;

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

   @Override
   public <T> EntryReference<T> getReference(EntryId eId)
   {
      return new EntryRefImpl<>(eId.getId(), eId.getType());
   }

   @Override
   public <T> EntryReference<T> getReference(String token)
   {
      EntryId eId = decodeToken(token);
      return new EntryRefImpl<>(eId.getId(), eId.getType());
   }

   @Override
   public <T> EntryReference<T> getReference(URI uri)
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
   public <T> EntryReference<T> getReference(T entry)
   {
      return new EntryRefImpl<>(entry);
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

   private class EntryRefImpl<T> implements EntryReference<T>
   {
      private final EntryResolver<T> resolver;
      private final ConcurrentHashMap<UUID, Optional<T>> cache = new ConcurrentHashMap<>();
      private EntryId entryId;

      public EntryRefImpl(String id, String type)
      {
         this.entryId = new EntryId(id, type);
         this.resolver = BasicResolverRegistry.this.getResolver(id, type);
      }

      public EntryRefImpl(T instance)
      {
         this.resolver = BasicResolverRegistry.this.getResolver(instance);
         this.entryId = this.resolver.makeReference(instance);
         this.cache.put(new UUID(0, 0), Optional.of(instance));
      }

      @Override
      public String getLabel()
      {
         return resolver.getLabel(getEntry(null));
      }

      @Override
      public String getHtmlLabel()
      {
         return resolver.getHtmlLabel(getEntry(null));
      }

      @Override
      public EntryId getEntryId()
      {
         return this.entryId;
      }

      @Override
      public Class<T> getEntryType()
      {
         return resolver.getType();
      }

      @Override
      public EntryResolver<T> getResolver()
      {
         return resolver;
      }

      @Override
      public String getId()
      {
         return entryId.getId();
      }

      @Override
      public String getType()
      {
         return entryId.getType();
      }

      @Override
      public String getToken()
      {
         return tokenize(entryId.getId(), entryId.getType());
      }

      @Override
      public URI getUri()
      {
         return resolver.toUri(getEntryId());
      }

      @Override
      public synchronized T getEntry(Account account)
      {
         UUID id = account != null ? account.getId() : new UUID(0, 0);
         Optional<T> result = get(account);

         String msg = "No entry of type {0} found for id={1} using account {3} [{4}].";
         return result.orElseThrow(() -> {
            String eId = entryId.getId();
            String type = entryId.getType();
            String name = account != null ? account.getDisplayName() : "Anonymous";

            return new ResourceNotFoundException(format(msg, type, eId, name, id));
         });
      }

      public synchronized Optional<T> get(Account account)
      {
         UUID id = account != null ? account.getId() : new UUID(0, 0);
         return cache.computeIfAbsent(id, key -> resolver.resolve(account, getEntryId()));
      }

   }

}
