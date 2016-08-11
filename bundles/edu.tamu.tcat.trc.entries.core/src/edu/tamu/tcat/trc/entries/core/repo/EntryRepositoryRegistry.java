package edu.tamu.tcat.trc.entries.core.repo;

import java.util.function.Function;

import edu.tamu.tcat.account.Account;
import edu.tamu.tcat.trc.entries.core.resolver.EntryResolverRegistry;

/**
 *  Provides an interface for accessing repository instances and a few related
 *  resources that are typically required by repositories. Allows for some
 *  basic configuration so that individual services do not need to be stitched
 *  to repositories directly.
 */
public interface EntryRepositoryRegistry
{
   // TODO provide access to DocumentFactory, IdFactory, etc as needed.

   // TODO provide factory to create a document repo

   /**
    * Indicates whether a given repository has been registered
    *
    * @param type The type of repository to check.
    * @return {@code true} if there is a repository registered for the supplied type.
    */
   <Repo> boolean isAvailable(Class<Repo> type);

   /**
    * Obtain a reference to a TRC entry repository of the indicated type.
    *
    * @param type The type of repository to obtain.
    * @return The requested repository.
    * @throws IllegalArgumentException If no repository is registered for this type.
    */
   <Repo> Repo getRepository(Account account, Class<Repo> type) throws IllegalArgumentException;

   /**
    *
    * @param type The repository interface class under which the repository
    *       should be registered.
    * @param repository The repository instance to register.
    */
   <Repo> void registerRepository(Class<Repo> type, Function<Account, Repo> factory);

   /**
    * @return The configured {@link EntryResolverRegistry} to be used.
    */
   EntryResolverRegistry getResolverRegistry();

}
