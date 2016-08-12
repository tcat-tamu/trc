package edu.tamu.tcat.trc;

import edu.tamu.tcat.account.Account;

public interface TrcFrameworkManager
{
   <Service> Service getService(Account account, Class<Service> type);

   <Repo> Repo getEntryRepository(Account account, Class<Repo> type);
}
