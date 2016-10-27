package edu.tamu.tcat.trc.resolver;

import java.net.URI;

import edu.tamu.tcat.account.Account;

/**
 *
 */
public interface EntryReferenceProxy<T>
{
   String getId();

   String getType();

   String getToken();

   URI getUri();

   T getEntry(Account account);
}
