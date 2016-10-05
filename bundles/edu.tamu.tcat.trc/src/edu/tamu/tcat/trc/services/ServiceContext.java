package edu.tamu.tcat.trc.services;

import java.util.Optional;
import java.util.Set;

import edu.tamu.tcat.account.Account;

/**
 *  The context in which a TRC Service Reference is supplied to the client. TRC Services are
 *  instantiated within the context of a user account and possibly other scoping and
 *  configuration parameters. The service context is used to obtain a reference to a service
 *  from the {@link TrcServiceManager}.
 *
 *  <p>Instances of a service context are typically obtained using a factory method or builder
 *  provided by the corresponding service API. This allows the service to provide a
 *  well-defined API to configure the context and to validate that all required information
 *  has been supplied as needed by the service.
 */
public interface ServiceContext<ServiceType>
{

   Class<ServiceType> getType();

   Optional<Account> getAccount();

   Set<String> getScopeProperties();

   Object getProperty(String key);
}
