package edu.tamu.tcat.trc.entries.core;

import java.util.concurrent.ConcurrentHashMap;

public class BasicEntryRepositoryRegistry implements EntryRepositoryRegistry
{
   private final ConcurrentHashMap<Class<? extends Object>, Object> repositories = new ConcurrentHashMap<>();

   public BasicResolverRegistry resolvers = new BasicResolverRegistry();

   @Override
   public EntryResolverRegistry getResolverRegistry()
   {
      return resolvers;
   }

   @Override
   public <Repo> boolean isAvailable(Class<Repo> type)
   {
      return repositories.containsKey(type);
   }

   @Override
   @SuppressWarnings("unchecked") // type safety maintained by registration process
   public <Repo> Repo getRepository(Class<Repo> type)
   {
      if (!isAvailable(type))
         throw new IllegalArgumentException("No resolver has been registered for " + type);

      return (Repo)repositories.get(type);
   }

   @Override
   public <Repo> void registerRepository(Class<Repo> type, Repo repository)
   {
      if (repositories.containsKey(type))
            throw new IllegalArgumentException("A repository has already been registered for " + type);

      repositories.put(type, repository);
   }

   public <Repo> void unregister(Class<Repo> type)
   {
      repositories.remove(type);
   }
}
