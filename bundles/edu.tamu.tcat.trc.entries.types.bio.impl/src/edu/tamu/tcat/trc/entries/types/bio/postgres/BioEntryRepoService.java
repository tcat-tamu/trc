package edu.tamu.tcat.trc.entries.types.bio.postgres;

import java.util.logging.Level;
import java.util.logging.Logger;

import edu.tamu.tcat.osgi.config.ConfigurationProperties;
import edu.tamu.tcat.trc.entries.core.repo.BasicRepoDelegate;
import edu.tamu.tcat.trc.entries.core.repo.RepositoryContext;
import edu.tamu.tcat.trc.entries.types.bio.Person;
import edu.tamu.tcat.trc.entries.types.bio.postgres.model.PersonImpl;
import edu.tamu.tcat.trc.entries.types.bio.repo.EditPersonCommand;
import edu.tamu.tcat.trc.entries.types.bio.repo.PeopleRepository;
import edu.tamu.tcat.trc.repo.DocRepoBuilder;
import edu.tamu.tcat.trc.repo.DocumentRepository;
import edu.tamu.tcat.trc.repo.IdFactory;

public class BioEntryRepoService
{
   private static final Logger logger = Logger.getLogger(BioEntryRepoService.class.getName());

   public static final String ID_CONTEXT = "people";
   private static final String TABLE_NAME = "people";
   private static final String SCHEMA_DATA_FIELD = "historical_figure";

   private IdFactory bioIds;

   private RepositoryContext ctx;
   private ConfigurationProperties config;

   private DocumentRepository<Person, DataModelV1.Person, EditPersonCommand> docRepo;
   private BasicRepoDelegate<Person, DataModelV1.Person, EditPersonCommand> delegate;

   public BioEntryRepoService(RepositoryContext ctx)
   {
      this.ctx = ctx;
   }

   public void setRepoContext(RepositoryContext ctx)
   {
      this.ctx = ctx;
   }

   public void start()
   {
      try
      {
         logger.info("Activating relationship repository service. . . ");
         this.bioIds = ctx.getIdFactory(ID_CONTEXT);
         this.config = ctx.getConfig();

         initDocumentStore();
         initDelegate();

         ctx.registerResolver(new BioEntryResolver(config, delegate));
         ctx.registerRepository(PeopleRepository.class, account -> new BioEntryRepoImpl(delegate, account));

         logger.fine("Activated relationship repository service.");

      }
      catch (Exception ex)
      {
         logger.log(Level.SEVERE, "Failed to activate relationship repository service.", ex);
         throw ex;
      }
   }

   public void stop()
   {
      docRepo.dispose();
      delegate.dispose();
   }

   private void initDocumentStore()
   {
      DocRepoBuilder<Person, DataModelV1.Person, EditPersonCommand> builder = ctx.getDocRepoBuilder();
      builder.setTableName(TABLE_NAME);
      builder.setDataColumn(SCHEMA_DATA_FIELD);
      builder.setEditCommandFactory(new EditPersonCommandFactory());
      builder.setDataAdapter(BioEntryRepoService::adapt);
      builder.setStorageType(DataModelV1.Person.class);
      builder.setEnableCreation(true);

      docRepo = builder.build();
   }

   private void initDelegate()
   {
      BasicRepoDelegate.Builder<Person, DataModelV1.Person, EditPersonCommand> delegateBuilder =
            new BasicRepoDelegate.Builder<>();

      delegateBuilder.setEntryName("relationship");
      delegateBuilder.setIdFactory(bioIds);
      delegateBuilder.setEntryResolvers(ctx.getResolverRegistry());
      delegateBuilder.setAdapter(BioEntryRepoService::adapt);
      delegateBuilder.setDocumentRepo(docRepo);

      delegate = delegateBuilder.build();
   }

   public static Person adapt(DataModelV1.Person dto)
   {
      return new PersonImpl(dto);
   }
}
