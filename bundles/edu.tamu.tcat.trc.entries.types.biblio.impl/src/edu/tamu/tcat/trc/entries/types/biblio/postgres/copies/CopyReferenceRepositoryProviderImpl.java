package edu.tamu.tcat.trc.entries.types.biblio.postgres.copies;

import java.util.Objects;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.tamu.tcat.db.exec.sql.SqlExecutor;
import edu.tamu.tcat.trc.entries.types.biblio.copies.CopyReference;
import edu.tamu.tcat.trc.entries.types.biblio.dto.copies.CopyReferenceDTO;
import edu.tamu.tcat.trc.entries.types.biblio.repo.copies.CopyReferenceRepositoryProvider;
import edu.tamu.tcat.trc.entries.types.biblio.repo.copies.EditCopyReferenceCommand;
import edu.tamu.tcat.trc.repo.BasicSchemaBuilder;
import edu.tamu.tcat.trc.repo.CommitHook;
import edu.tamu.tcat.trc.repo.DocumentRepository;
import edu.tamu.tcat.trc.repo.EditCommandFactory;
import edu.tamu.tcat.trc.repo.RepositoryException;
import edu.tamu.tcat.trc.repo.RepositorySchema;
import edu.tamu.tcat.trc.repo.SchemaBuilder;
import edu.tamu.tcat.trc.repo.postgres.PsqlJacksonRepoBuilder;

public class CopyReferenceRepositoryProviderImpl implements CopyReferenceRepositoryProvider
{
   private static final Logger logger = Logger.getLogger(CopyReferenceRepositoryProviderImpl.class.getName());

   private static final String TABLE_NAME = "copies";
   private static final String SCHEMA_ID = "trcBiblioDigitalCopy";
   private static final String SCHEMA_DATA_FIELD = "copy_ref";

   private SqlExecutor sqlExecutor;

   private DocumentRepository<CopyReference, EditCopyReferenceCommand> repo;

   /**
    * Bind method for SQL executor service dependency (usually called by dependency injection layer)
    *
    * @param sqlExecutor
    */
   public void setSqlExecutor(SqlExecutor sqlExecutor)
   {
      this.sqlExecutor = sqlExecutor;
   }

   /**
    * Lifecycle management method (usually called by framework service layer)
    * Called when all dependencies have been provided and the service is ready to run.
    */
   public void activate()
   {
      Objects.requireNonNull(sqlExecutor, "No SQL Executor provided.");

      repo = buildRepository();
   }

   /**
    * Lifecycle management method (usually called by framework service layer)
    * Called when this service is no longer required.
    */
   public void dispose()
   {
      sqlExecutor = null;
      repo = null;
   }

   @Override
   public DocumentRepository<CopyReference, EditCopyReferenceCommand> getRepository()
   {
      return repo;
   }

   /**
    * @return A new document repository instance for persisting and retrieving digital copies
    */
   private DocumentRepository<CopyReference, EditCopyReferenceCommand> buildRepository()
   {
      PsqlJacksonRepoBuilder<CopyReference, EditCopyReferenceCommand, CopyReferenceDTO> repoBuilder = new PsqlJacksonRepoBuilder<>();

      repoBuilder.setDbExecutor(sqlExecutor);
      repoBuilder.setTableName(TABLE_NAME);
      repoBuilder.setEditCommandFactory(new EditCopyReferenceCommandFactoryImpl());
      repoBuilder.setDataAdapter(this::adapt);
      repoBuilder.setSchema(buildSchema());
      repoBuilder.setStorageType(CopyReferenceDTO.class);
      repoBuilder.setEnableCreation(true);

      try
      {
         return repoBuilder.build();
      }
      catch (RepositoryException e)
      {
         logger.log(Level.SEVERE, "Failed to construct digital copy repository instance.", e);
      }
      return null;
   }

   /**
    * @return The repository schema
    */
   private RepositorySchema buildSchema()
   {
      SchemaBuilder schemaBuilder = new BasicSchemaBuilder();
      schemaBuilder.setId(SCHEMA_ID);
      schemaBuilder.setDataField(SCHEMA_DATA_FIELD);
      return schemaBuilder.build();
   }

   /**
    * Constructs a {@link CopyReference} instance from a storage data transfer object
    * @param dto
    * @return
    */
   private CopyReference adapt(CopyReferenceDTO dto)
   {
      return new BasicCopyReference(dto.id,
            dto.type,
            dto.properties,
            dto.title,
            dto.summary,
            dto.rights);
   }

   public static class EditCopyReferenceCommandFactoryImpl implements EditCommandFactory<CopyReferenceDTO, EditCopyReferenceCommand>
   {
      @Override
      public EditCopyReferenceCommand create(String id, CommitHook<CopyReferenceDTO> commitHook)
      {
         return new EditCopyReferenceCommandImpl(id, null, commitHook);
      }

      @Override
      public EditCopyReferenceCommand edit(String id, Supplier<CopyReferenceDTO> currentState, CommitHook<CopyReferenceDTO> commitHook)
      {
         return new EditCopyReferenceCommandImpl(id, currentState, commitHook);
      }
   }

}
