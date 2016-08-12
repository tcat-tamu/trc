package edu.tamu.tcat.trc.entries.core.repo;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import edu.tamu.tcat.account.Account;
import edu.tamu.tcat.trc.entries.core.resolver.BasicResolverRegistry;
import edu.tamu.tcat.trc.entries.core.resolver.EntryResolverRegistry;

public class BasicEntryRepositoryRegistry implements EntryRepositoryRegistry
{
   private final ConcurrentHashMap<Class<? extends Object>, Function<Account, ?>> repositories = new ConcurrentHashMap<>();

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
   public <Repo> Repo getRepository(Account account, Class<Repo> type)
   {
      if (!isAvailable(type))
         throw new IllegalArgumentException("No resolver has been registered for " + type);

      Function<Account, ?> factory = repositories.get(type);
      return (Repo)factory.apply(account);
   }

   public <Repo> void registerRepository(Class<Repo> type, Function<Account, Repo> factory)
   {
      if (repositories.containsKey(type))
            throw new IllegalArgumentException("A repository has already been registered for " + type);

      repositories.put(type, factory);
   }

   public <Repo> void unregister(Class<Repo> type)
   {
      repositories.remove(type);
   }
}
