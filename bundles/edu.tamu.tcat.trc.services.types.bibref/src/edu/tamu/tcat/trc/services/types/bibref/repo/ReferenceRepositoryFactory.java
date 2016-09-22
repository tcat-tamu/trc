package edu.tamu.tcat.trc.services.types.bibref.repo;

import edu.tamu.tcat.account.Account;

/**
 *
 * @deprecated to be replaced with a service registry at the TRC Framework level
 */
@Deprecated
public interface ReferenceRepositoryFactory
{

   /**
    * Retrieve a ReferenceRepository scoped to a particular account.
    *
    * @param account May be null
    * @return
    */
   ReferenceRepository getRepo(Account account);

}
