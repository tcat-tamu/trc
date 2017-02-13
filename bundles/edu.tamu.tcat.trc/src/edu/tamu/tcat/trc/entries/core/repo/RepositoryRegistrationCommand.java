package edu.tamu.tcat.trc.entries.core.repo;

import java.util.function.Function;

import edu.tamu.tcat.account.Account;
import edu.tamu.tcat.trc.resolver.EntryResolver;

public interface RepositoryRegistrationCommand<EntryType, Repo extends EntryRepository<EntryType>>
{
   // NOTE Registration/configuration of a repository should supply the following information
   //      - key
   //      - name
   //      - type
   //      - creation factory
   //      - entry resolver
   //      - search hooks
   //      - REST sub-resource

   void setKey(String key);

   void setName(String name);

   void setType(Class<Repo> type);

   void setFactory(Function<Account, Repo> factory);

   void addResolver(EntryResolver<EntryType> resolver);

   // TODO add hooks for REST sub-resources and for search hooks

   EntryRepositoryRegistrar.Registration execute();
}
