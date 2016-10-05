package edu.tamu.tcat.trc.impl.psql.services;

import edu.tamu.tcat.trc.services.ServiceContext;

public interface ServiceFactory<Service>
{
   Class<Service> getType();

   Service getService(ServiceContext<Service> ctx);

   void shutdown();
}
