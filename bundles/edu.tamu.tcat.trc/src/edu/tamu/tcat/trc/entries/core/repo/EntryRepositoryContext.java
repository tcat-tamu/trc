package edu.tamu.tcat.trc.entries.core.repo;

import java.util.function.Function;

import edu.tamu.tcat.account.Account;
import edu.tamu.tcat.db.exec.sql.SqlExecutor;
import edu.tamu.tcat.osgi.config.ConfigurationProperties;
import edu.tamu.tcat.trc.entries.core.resolver.EntryResolverRegistry;
import edu.tamu.tcat.trc.repo.DocumentRepository;
import edu.tamu.tcat.trc.repo.EditCommandFactory;
import edu.tamu.tcat.trc.repo.IdFactory;

/**
 *  Provides access to various services and registration actions that are
 *  commonly required by TRC Entry Repositories. Rather than requiring each
 *  of the services be injected individually, this class provides a convenient
 *  structure for gr
 */
public interface EntryRepositoryContext
{

   /**
    * @param scope A string-based scope for the ids to be generated. For id factories
    *       created by this context, the returned factory guarantees that duplicate
    *       ids will not be generated within the same scope.
    *       scope.
    * @return An {@link IdFactory} that can be used to generate unique identifiers
    *       within the scope of a particular context. For example, to generate ids
    *       for entry instances.
    */
   IdFactory getIdFactory(String scope);

   SqlExecutor getSqlExecutor();

   ConfigurationProperties getConfig();

   EntryResolverRegistry getResolverRegistry();

   EntryRepositoryRegistry getRepositoryRegistry();

   <T, DTO, CMD> DocumentRepository<T, DTO, CMD> buildDocumentRepo(String tablename, EditCommandFactory<DTO, CMD> factory, Function<DTO, T> adapter, Class<DTO> type);

  /**
   * Registers a repository with the associated {@link EntryRepositoryRegistry}.
   *
   * @param type The repository interface class under which the repository
   *       should be registered.
   * @param repository The repository instance to register.
   */
  <Repo> void register(Class<Repo> type, Function<Account, Repo> factory);
}
