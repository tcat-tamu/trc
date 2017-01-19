package edu.tamu.tcat.trc.entries.core.repo;

import edu.tamu.tcat.account.Account;

/**
 * Provides detail about and access to a registered repository.
 */
public interface RepositoryReference<RepoType extends EntryRepository<?>>
{

   /** 
    * @return A display name for this repository.
    */
   String getName();

   /**
    * @return The Java type of the repository. This should correspond to the 
    *       interface the repository is registered under rather than a concrete
    *       implementation.
    */
   Class<RepoType> getType();

   /**
    * @return An instance of the identified repository scoped to a particular 
    *       user account.
    */
   RepoType get(Account account);

}
