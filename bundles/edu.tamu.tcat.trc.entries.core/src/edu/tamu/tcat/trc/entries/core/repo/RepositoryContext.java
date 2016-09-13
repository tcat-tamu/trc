package edu.tamu.tcat.trc.entries.core.repo;

import java.net.URI;
import java.util.function.Function;

import edu.tamu.tcat.account.Account;
import edu.tamu.tcat.osgi.config.ConfigurationProperties;
import edu.tamu.tcat.trc.entries.core.resolver.EntryResolver;
import edu.tamu.tcat.trc.entries.core.resolver.EntryResolverRegistry;
import edu.tamu.tcat.trc.repo.DocRepoBuilder;
import edu.tamu.tcat.trc.repo.IdFactory;

public interface RepositoryContext
{
   /**
    * @return The configured public API endpoint for this deployment.
    */
   URI getApiEndpoint();

   /**
    * @return Configuration properties for this deployment.
    */
   ConfigurationProperties getConfig();

   /**
    * Construct an {@link IdFactory} that will provide identifies that are unique within
    * the supplied context. Note that multiple calls to this method with the same context
    * may return different {@code IdFactory} instances that generate different id sequences,
    * however, the returned factories guarantee that ids obtained from will be unique
    * regardless of which instance generates the id.
    *
    * @param context The context relative to which, ids must be unique.
    * @return An id factory.
    */
   IdFactory getIdFactory(String context);

   EntryResolverRegistry getResolverRegistry();

   /**
    * Builds a basic document repository using the supplied configuration utilities.
    *
    * @param tablename The name of the database table (or other storage specific mechanism)
    *       for storing recording these entries. Note that if the underlying table does not
    *       exist, it will be created.
    * @param factory A factory for generating edit commands.
    * @param adapter An adapter to convert stored data representations (type DTO) to
    *       instances of the associated entries Java type (an Interface).
    * @param type The Java interface that defines this entry.
    *
    * @return A document repository with the supplied configuration.
    */
   <T, DTO, CMD> DocRepoBuilder<T, DTO, CMD> getDocRepoBuilder();
//   buildDocumentRepo(String tablename,
//                                                                   EditCommandFactory<DTO, CMD> factory,
//                                                                   Function<DTO, T> adapter,
//                                                                   Class<DTO> type);

   /**
    * Registers an entry repository. Duplicate registrations for the same type will result
    * in an {@link IllegalArgumentException}.
    *
    * @param type The type of repository to register. Should be the interface type rather than
    *       an implementation class.
    * @param factory A factory that, given an account (possibly null) will return an entry
    *       repository of the associated type. The returned repository should be scoped to
    *       the supplied user account such that all actions on the returned repository are
    *       considered to be performed by that account. Note that returned instances may be
    *       cached for performance purposes.
    */
   <Repo> void registerRepository(Class<Repo> type, Function<Account, Repo> factory);

   /**
    * Remove a previously registered repository.
    *
    * @param type The Java interface associated with the repository to remove.
    */
   <Repo> void unregister(Class<Repo> type); // TODO seems like a bad idea - should supply registration handle?

   /**
    * Register a new {@link EntryResolver} with the associated {@link EntryResolverRegistry}.
    *
    * <p>Note that, since the {@code EntryResolverRegistry} is intended to be managed by the
    * {@link EntryRepositoryRegistry} rather than configured as an external service, it
    * cannot be directly injected into repositories. Consequently the {@code RepositoryContext}
    * needs to mediate resolver registration.
    *
    * @param resolver The resolver to be registered.
    * @return A registration handle.
    */
   <EntryType> EntryResolverRegistry.Registration registerResolver(EntryResolver<EntryType> resolver);


}
