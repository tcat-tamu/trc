package edu.tamu.tcat.trc.services;

import java.util.Optional;
import java.util.Set;

import edu.tamu.tcat.account.Account;

/**
 *  The context in which a TRC Service Reference is supplied to the client. TRC Services are
 *  instantiated within the context of a user account and possibly a variety of other scoping
 *  parameters. The service context is used to obtain a reference to a service from the
 *  TrcServiceRegistry
 */
public interface ServiceContext<ServiceType>
{

   Class<ServiceType> getType();

   Optional<Account> getAccount();

   Set<String> getScopeProperties();

   Object getProperty(String key);
}
