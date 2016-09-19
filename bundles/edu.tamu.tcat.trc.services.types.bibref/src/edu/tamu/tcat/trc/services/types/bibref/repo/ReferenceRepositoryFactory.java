package edu.tamu.tcat.trc.services.types.bibref.repo;

import edu.tamu.tcat.account.Account;

public interface ReferenceRepositoryFactory
{
   /**
    * @param account
    * @return A {@link RepositoryFactory} scoped to the given account
    */
   ReferenceRepository getReferenceRepository(Account account);
}
