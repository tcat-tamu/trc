package edu.tamu.tcat.trc;

import java.net.URI;

import edu.tamu.tcat.account.Account;
import edu.tamu.tcat.osgi.config.ConfigurationProperties;
import edu.tamu.tcat.trc.entries.core.repo.EntryRepositoryRegistry;
import edu.tamu.tcat.trc.repo.id.IdFactory;
import edu.tamu.tcat.trc.resolver.EntryId;
import edu.tamu.tcat.trc.resolver.EntryResolverRegistry;
import edu.tamu.tcat.trc.search.solr.IndexService;
import edu.tamu.tcat.trc.search.solr.IndexServiceStrategy;
import edu.tamu.tcat.trc.search.solr.QueryService;
import edu.tamu.tcat.trc.search.solr.SearchServiceManager;
import edu.tamu.tcat.trc.services.ServiceContext;
import edu.tamu.tcat.trc.services.TrcServiceException;
import edu.tamu.tcat.trc.services.TrcServiceManager;

// TODO rename TrcFrameworkManager
public interface TrcApplication
{
   // NOTE A key motivation of this API is to reduce the amount of service startup and to facilitate
   //      collaboration among implementations. Wherever possible, this should be the primary access
   //      point for obtaining references to framework components, in lieu of DS injection via OSGi

   /**
    * @return The REST API endpoint that has been configured for this application.
    */
   URI getApiEndpoint();

   IdFactory getIdFactory(String scope);

   /**
    * @return The configuration properties that will be used by the application.
    */
   ConfigurationProperties getConfig();

   EntryResolverRegistry getResolverRegistry();


   <T> EntryFacade<T> getEntryFacade(EntryId ref, Class<T> type, Account account);

   <T> EntryFacade<T> getEntryFacade(T entry, Class<T> type, Account account);

   /**
    * Obtain a reference to a TRC entry repository of the indicated type.
    *
    * @param type The type of repository to obtain.
    * @return The requested repository.
    * @throws IllegalArgumentException If no repository is registered for this type.
    */
   <Repo> Repo getRepository(Account account, Class<Repo> type) throws IllegalArgumentException;

   /**
    * @param ctx
    * @return A service for the
    */
   <ServiceType> ServiceType getService(ServiceContext<ServiceType> ctx) throws TrcServiceException;

   <Entry> IndexService<Entry> getIndexService(Class<Entry> type);

   <Entry, QueryCmd> QueryService<QueryCmd> getQueryService(IndexServiceStrategy<Entry, QueryCmd> indexCfg);

   /**
    * @return The registry of all entry repositories that have been configured.
    */
   EntryRepositoryRegistry  getEntryRepositoryManager();


   TrcServiceManager getServiceManager();

   SearchServiceManager getSearchManager();
}
