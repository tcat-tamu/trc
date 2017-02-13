package edu.tamu.tcat.trc.services;

/**
 * Defines a factory for instantiating a service based on a supplied {@link ServiceContext}.
 * Service implementations should provide an implementation of this class that can be used by
 * the {@link TrcServiceManager} to instantiate scoped instances of the service.
 *
 * @param <Service> The type of service supplied by this factory.
 */
public interface ServiceFactory<Service>
{
   /**
    * @return The registered type of service managed by this factory. Note that this
    *    should be an instance of the interface class under which the service will
    *    be registered, rather than the implementation class returned.
    */
   Class<Service> getType();

   /**
    * Obtain an instance of the given service.
    *
    * @param ctx The context that will be used to scope the returned service.
    * @return A service, scoped to the supplied context.
    */
   Service getService(ServiceContext<Service> ctx);

   /**
    * Shuts down the associated factory.
    */
   void shutdown();
}
