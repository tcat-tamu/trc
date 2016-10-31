package edu.tamu.tcat.trc;

import java.net.URI;

import edu.tamu.tcat.account.Account;
import edu.tamu.tcat.osgi.config.ConfigurationProperties;
import edu.tamu.tcat.trc.entries.core.repo.EntryRepositoryRegistry;
import edu.tamu.tcat.trc.repo.id.IdFactory;
import edu.tamu.tcat.trc.resolver.EntryId;
import edu.tamu.tcat.trc.resolver.EntryResolverRegistry;
import edu.tamu.tcat.trc.services.TrcServiceManager;

// TODO rename TrcFrameworkManager
public interface TrcApplication
{
   URI getApiEndpoint();

   ConfigurationProperties getConfig();

   EntryRepositoryRegistry  getEntryRepositoryManager();

   EntryResolverRegistry getResolverRegistry();

   TrcServiceManager getServiceManager();

   IdFactory getIdFactory(String scope);

   <T> EntryFacade<T> getEntryFacade(EntryId ref, Class<T> type, Account account);

   <T> EntryFacade<T> getEntryFacade(T entry, Class<T> type, Account account);
}
