package edu.tamu.tcat.trc.impl.psql.services.bibref;

import java.util.logging.Logger;

import edu.tamu.tcat.trc.impl.psql.entries.DbEntryRepositoryRegistry;
import edu.tamu.tcat.trc.impl.psql.services.ServiceFactory;
import edu.tamu.tcat.trc.impl.psql.services.bibref.model.ReferenceCollectionImpl;
import edu.tamu.tcat.trc.impl.psql.services.bibref.repo.DataModelV1;
import edu.tamu.tcat.trc.impl.psql.services.bibref.repo.EditBibliographyCommandFactory;
import edu.tamu.tcat.trc.impl.psql.services.bibref.repo.ReferenceRepositoryImpl;
import edu.tamu.tcat.trc.repo.DocRepoBuilder;
import edu.tamu.tcat.trc.repo.DocumentRepository;
import edu.tamu.tcat.trc.resolver.EntryResolverRegistry;
import edu.tamu.tcat.trc.services.ServiceContext;
import edu.tamu.tcat.trc.services.bibref.ReferenceCollection;
import edu.tamu.tcat.trc.services.bibref.repo.EditBibliographyCommand;
import edu.tamu.tcat.trc.services.bibref.repo.ReferenceRepository;

public class RefServiceFactory implements ServiceFactory<ReferenceRepository>
{
   private static final Logger logger = Logger.getLogger(RefServiceFactory.class.getName());

   private static final String TABLE_NAME = "bibrefs";
   private static final String SCHEMA_DATA_FIELD = "data";

   private final DbEntryRepositoryRegistry repoRegistry;
   private final DocumentRepository<ReferenceCollection, DataModelV1.ReferenceCollection, EditBibliographyCommand> docRepo;

   public RefServiceFactory(DbEntryRepositoryRegistry repoRegistry)
   {
      this.repoRegistry = repoRegistry;
      this.docRepo = initDocRepo();
   }

   /**
    * @param context
    * @return A document repository for persisting and retrieving {@link ReferenceCollection} instances.
    */
   private DocumentRepository<ReferenceCollection, DataModelV1.ReferenceCollection, EditBibliographyCommand> initDocRepo()
   {
      EntryResolverRegistry resolverRegistry = repoRegistry.getResolverRegistry();
      EditBibliographyCommandFactory editCommandFactory = new EditBibliographyCommandFactory(resolverRegistry);

      DocRepoBuilder<ReferenceCollection, DataModelV1.ReferenceCollection, EditBibliographyCommand> builder =
            repoRegistry.getDocRepoBuilder();
      builder.setTableName(TABLE_NAME);
      builder.setDataColumn(SCHEMA_DATA_FIELD);
      builder.setEditCommandFactory(editCommandFactory);
      builder.setDataAdapter(ReferenceCollectionImpl::new);
      builder.setStorageType(DataModelV1.ReferenceCollection.class);
      builder.setEnableCreation(true);

      return builder.build();
   }

   @Override
   public Class<ReferenceRepository> getType()
   {
      return ReferenceRepository.class;
   }

   /**
    * Stops the bibliographic reference repository service, releasing allocated resources.
    * Once stopped, the service must be re-initialized and {@link RefServiceFactory#start} must be called in order to restart.
    */
   @Override
   public void shutdown()
   {
      docRepo.dispose();
   }

   @Override
   public ReferenceRepository getService(ServiceContext<ReferenceRepository> context)
   {
      return new ReferenceRepositoryImpl(docRepo, repoRegistry.getResolverRegistry(), context);
   }
}
