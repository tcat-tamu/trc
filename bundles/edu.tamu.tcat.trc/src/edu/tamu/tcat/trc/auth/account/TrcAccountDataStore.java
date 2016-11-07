package edu.tamu.tcat.trc.auth.account;

import java.util.UUID;

import edu.tamu.tcat.account.Account;
import edu.tamu.tcat.account.AccountException;
import edu.tamu.tcat.account.login.LoginData;
import edu.tamu.tcat.account.login.LoginProvider;
import edu.tamu.tcat.account.store.AccountStore;

/**
 *  Manages all accounts for a TRC based application. Note that individual components of
 *  the TRC system or applications that use the TRC system may supply their own
 *  data stores that manage data linked to a specific account, for instance, personal
 *  bookmarks or bibliography collections.
 */
public interface TrcAccountDataStore extends AccountStore
{
   static TrcAccount GUEST = new TrcSystemAccount("Guest User", "guest.user", 0);

   static TrcAccount SYSTEM = new TrcSystemAccount("System Internal User", "internal.user.system", 1);
   static TrcAccount SEARCH = new TrcSystemAccount("Search Internal User", "internal.user.search", 2);

   @Override
   TrcAccount getAccount(UUID id);

   /**
    * Retrieves an account based on the associated TRC username.
    *
    * <p>Note that this may or may not be the same username used to authenticate
    * the user. TRC maintains usernames for accounts internally. For external logins,
    * for instance with Facebook, these user names may not correspond to the locally
    * stored TRC account username.
    *
    * @param username The name of the account to retrieve.
    * @return The associated account. Will not have authentication information.
    * @throws AccountException if there is no account
    */
   TrcAccount getAccount(String username) throws AccountException;

   @Override
   TrcAccount lookup(LoginData data);

   /**
    * Initializes the account creation process based on the supplied login data.
    * A new TRC Account will be created when (and only when) the returned edit
    * command is executed.
    *
    * <p>This is invoked after authentication has already been performed against a
    * {@link LoginProvider} which has provided a {@link LoginData}, containing the
    * parameters that will be used to set up the core account authentication credentials.
    *
    * @param data The authenticated login data that will be associated with this account.
    * @return A command for use in editing the associated account data.
    */
   EditTrcAccountCommand create(LoginData data);

   /**
    * Edits an existing account. Changes will be made under the authority of the
    * supplied actor, which may be the account that is being modified.
    *
    * @param account The account to be modified.
    * @param actor The account representing the user or other actor who
    *       is making the requested modifications.
    * @return A command for use in editing the associated account data.
    */
   EditTrcAccountCommand modify(TrcAccount account, TrcAccount actor);

   /**
    * Links an account to a set of authentication credentials. This could be used,
    * for instance, to associate login credentials from Facebook, LinkedIn or Zotero
    * with a TRC Account.
    *
    * TODO this is insufficient to restore and connect to the linked data source.
    *
    * @param account The account to link.
    * @param data The data to be associated with this account.
    */
   void link(Account account, LoginData data);

}
