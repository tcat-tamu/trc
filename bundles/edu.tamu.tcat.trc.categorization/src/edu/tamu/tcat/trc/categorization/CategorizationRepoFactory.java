package edu.tamu.tcat.trc.categorization;

import edu.tamu.tcat.account.Account;

/**
 * Used to manufacture {@link CategorizationScope} instances and to obtain
 * {@link CategorizationRepo} instances.
 */
public interface CategorizationRepoFactory
{
   // TODO it should be possible to change and/or map a namespace. For instance, for
   //      a namespace that is mapped to a particular username, change the scope id
   //      when the username is changed.

   /**
    * Creates a new categorization scope for the given scope id and user account.
    * Note that the scope id defines space for repositories (all categories associated
    * with a given scope id must have unique keys) while the account is used to
    * identify the actor who is interacting with the repository for logging and
    * access control purposes.
    *
    * @param account The account that will be used to represent the actor for all
    *    actions performed using this repository.
    * @param scopeId An identifier that defines the namespace for a repository
    *    (all categories associated with a given scope id must have unique keys).
    * @return The requested categorization scope.
    */
   CategorizationScope createScope(Account account, String scopeId);

   /**
    *
    * @param scope The scope for the categorization repository
    * @return A categorization repository instance that operates within the defined scope.
    */
   CategorizationRepo getRepository(CategorizationScope scope);

}
