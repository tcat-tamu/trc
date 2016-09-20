package edu.tamu.tcat.trc.services.types.bibref.impl;

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.tamu.tcat.account.Account;
import edu.tamu.tcat.trc.entries.core.repo.RepositoryContext;
import edu.tamu.tcat.trc.entries.core.resolver.EntryResolverRegistry;
import edu.tamu.tcat.trc.repo.DocRepoBuilder;
import edu.tamu.tcat.trc.repo.DocumentRepository;
import edu.tamu.tcat.trc.services.types.bibref.ReferenceCollection;
import edu.tamu.tcat.trc.services.types.bibref.impl.model.ReferenceCollectionImpl;
import edu.tamu.tcat.trc.services.types.bibref.impl.repo.DataModelV1;
import edu.tamu.tcat.trc.services.types.bibref.impl.repo.EditBibliographyCommandFactory;
import edu.tamu.tcat.trc.services.types.bibref.impl.repo.ReferenceRepositoryImpl;
import edu.tamu.tcat.trc.services.types.bibref.repo.EditBibliographyCommand;
import edu.tamu.tcat.trc.services.types.bibref.repo.ReferenceRepository;
import edu.tamu.tcat.trc.services.types.bibref.repo.ReferenceRepositoryFactory;

public class BiographicReferenceService implements ReferenceRepositoryFactory
{
   private static final Logger logger = Logger.getLogger(BiographicReferenceService.class.getName());

   private static final String TABLE_NAME = "bibrefs";
   private static final String SCHEMA_DATA_FIELD = "data";

   private RepositoryContext context;
   private DocumentRepository<ReferenceCollection, DataModelV1.ReferenceCollection, EditBibliographyCommand> docRepo;

   /**
    * Provides a {@link RepositoryContext} in which the {@link ReferenceRepository} will operate. This should be provided prior to starting the service.
    * @param context
    */
   public void setRepositoryContext(RepositoryContext context)
   {
      this.context = context;
   }

   /**
    * Starts the bibliographic reference repository service, performing initialization tasks as necessary.
    * This method should be called prior to using the service.
    * If this is successful, then methods on this service should operate properly.
    */
   public void start()
   {
      String serviceName = getClass().getSimpleName();
      try
      {
         logger.info(() -> "Activating " + serviceName);

         Objects.requireNonNull(context, "no repository context provided");

         docRepo = initDocRepo(context);

         logger.fine(() -> "Activated " + serviceName);
      }
      catch (Exception e)
      {
         logger.log(Level.SEVERE, "Failed to activate " + serviceName, e);
         throw e;
      }
   }

   /**
    * Stops the bibliographic reference repository service, releasing allocated resources.
    * Once stopped, the service must be re-initialized and {@link BiographicReferenceService#start} must be called in order to restart.
    */
   public void stop()
   {
      String serviceName = getClass().getSimpleName();
      try
      {
         logger.info(() -> "Stopping " + serviceName);

         docRepo.dispose();
         docRepo = null;
         context = null;

         logger.fine(() -> "Stopped " + serviceName);
      }
      catch (Exception e)
      {
         logger.log(Level.SEVERE, "Failed to stop " + serviceName, e);
         throw e;
      }
   }

   /**
    * @param context
    * @return A document repository for persisting and retrieving {@link ReferenceCollection} instances.
    */
   private DocumentRepository<ReferenceCollection, DataModelV1.ReferenceCollection, EditBibliographyCommand> initDocRepo(RepositoryContext context)
   {
      EntryResolverRegistry resolverRegistry = context.getResolverRegistry();
      EditBibliographyCommandFactory editCommandFactory = new EditBibliographyCommandFactory(resolverRegistry);

      DocRepoBuilder<ReferenceCollection, DataModelV1.ReferenceCollection, EditBibliographyCommand> builder = context.getDocRepoBuilder();
      builder.setTableName(TABLE_NAME);
      builder.setDataColumn(SCHEMA_DATA_FIELD);
      builder.setEditCommandFactory(editCommandFactory);
      builder.setDataAdapter(ReferenceCollectionImpl::new);
      builder.setStorageType(DataModelV1.ReferenceCollection.class);
      builder.setEnableCreation(true);

      return builder.build();
   }

   @Override
   public ReferenceRepository getReferenceRepository(Account account)
   {
      EntryResolverRegistry resolverRegistry = context.getResolverRegistry();
      return new ReferenceRepositoryImpl(docRepo, resolverRegistry, account);
   }
}
