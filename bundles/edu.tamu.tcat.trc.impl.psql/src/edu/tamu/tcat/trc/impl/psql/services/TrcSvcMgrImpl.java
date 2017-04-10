package edu.tamu.tcat.trc.impl.psql.services;

import static java.text.MessageFormat.format;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.tamu.tcat.account.store.AccountStore;
import edu.tamu.tcat.db.exec.sql.SqlExecutor;
import edu.tamu.tcat.trc.impl.psql.entries.DbEntryRepositoryRegistry;
import edu.tamu.tcat.trc.impl.psql.services.bibref.RefServiceFactory;
import edu.tamu.tcat.trc.impl.psql.services.categorization.CategorizationServiceFactory;
import edu.tamu.tcat.trc.impl.psql.services.notes.NotesServiceFactory;
import edu.tamu.tcat.trc.impl.psql.services.seealso.SeeAlsoServiceFactory;
import edu.tamu.tcat.trc.services.ServiceContext;
import edu.tamu.tcat.trc.services.ServiceFactory;
import edu.tamu.tcat.trc.services.TrcServiceException;
import edu.tamu.tcat.trc.services.TrcServiceManager;

public class TrcSvcMgrImpl implements TrcServiceManager
{
   private final static Logger logger = Logger.getLogger(TrcSvcMgrImpl.class.getName());

   private DbEntryRepositoryRegistry repoRegistry;
   private AccountStore accountStore;
   private SqlExecutor sqlExecutor;

   private List<ServiceRegistration> servicesRegistrations = new ArrayList<>();


   public TrcSvcMgrImpl()
   {
      // TODO Auto-generated constructor stub
   }

   public void bind(DbEntryRepositoryRegistry entryRepo)
   {
      this.repoRegistry = entryRepo;
   }

   public void bind(AccountStore accountStore)
   {
      this.accountStore = accountStore;
   }

   public void bind(SqlExecutor sqlExecutor)
   {
      this.sqlExecutor = sqlExecutor;
   }

   public void activate()
   {
      RefServiceFactory bibRefSvc = new RefServiceFactory(repoRegistry);
      CategorizationServiceFactory categorizationSvc = new CategorizationServiceFactory(repoRegistry);
      NotesServiceFactory notesSvc = new NotesServiceFactory(repoRegistry, accountStore);
      SeeAlsoServiceFactory seeAlsoSvc = new SeeAlsoServiceFactory(sqlExecutor);

      servicesRegistrations.add(new ServiceRegistration(bibRefSvc));
      servicesRegistrations.add(new ServiceRegistration(categorizationSvc));
      servicesRegistrations.add(new ServiceRegistration(notesSvc));
      servicesRegistrations.add(new ServiceRegistration(seeAlsoSvc));
   }

   public void dispose()
   {
      servicesRegistrations.stream().forEach(ServiceRegistration::shutdown);
   }

   public DbEntryRepositoryRegistry getRepoRegistry()
   {
      return repoRegistry;
   }

   public <ServiceType> boolean isAvailable(ServiceContext<ServiceType> ctx)
   {
      return servicesRegistrations.stream().anyMatch(reg -> reg.canHandle(ctx));
   }

   @Override
   public <ServiceType> ServiceType getService(ServiceContext<ServiceType> ctx) throws TrcServiceException
   {
      String noRegisteredSvc = "No TRC service has been registered for the requested type {0}";
      ServiceRegistration svcReg = servicesRegistrations.stream()
                  .filter(reg -> reg.canHandle(ctx))
                  .findFirst()
                  .orElseThrow(() -> new TrcServiceException(format(noRegisteredSvc, ctx.getType())));

      return svcReg.get(ctx);
   }

   private static class ServiceRegistration
   {
      private final ServiceFactory<?> factory;

      public ServiceRegistration(ServiceFactory<?> factory)
      {
         this.factory = factory;
      }

      public void shutdown()
      {
         try
         {
            logger.log(Level.INFO, "Shutting down service factory " + factory.getClass().getSimpleName());
            factory.shutdown();
         }
         catch (Exception ex)
         {
            logger.log(Level.SEVERE, "Failed to cleanly shut down service factory " + factory.getClass().getSimpleName(), ex);
         }
      }

      public boolean canHandle(ServiceContext<?> ctx)
      {
         return (ctx.getType().isAssignableFrom(factory.getType()));
      }

      public <X> X get(ServiceContext<X> ctx)
      {
         String badContext = "Unsupported service context. Cannot convert registerd type {0} to requested type {1}";

         Class<X> returnType = ctx.getType();
         if (!canHandle(ctx))
            throw new TrcServiceException(format(badContext, factory.getType(), returnType));

         @SuppressWarnings({ "rawtypes", "unchecked" })     // type safety explicitly enforced
         Object svc = factory.getService((ServiceContext)ctx);
         return returnType.cast(svc);
      }
   }
}
