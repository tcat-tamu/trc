package edu.tamu.tcat.trc.auth.account;

import edu.tamu.tcat.account.Account;

/**
 *  An extension of the basic account that includes information
 *  used by the TRC system.
 */
public interface TrcAccount extends Account
{
   String getUsername();

   String getTitle();

   String getFirstName();

   String getLastName();

   String getEmailAddress();

   String getAffiliation();

   // getAuthenticationToken();
}
