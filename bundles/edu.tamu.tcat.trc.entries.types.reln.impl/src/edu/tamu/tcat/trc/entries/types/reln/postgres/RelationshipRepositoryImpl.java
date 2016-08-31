package edu.tamu.tcat.trc.entries.types.reln.postgres;

import java.util.Iterator;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.tamu.tcat.db.exec.sql.SqlExecutor;
import edu.tamu.tcat.trc.entries.types.reln.Relationship;
import edu.tamu.tcat.trc.entries.types.reln.dto.RelationshipDTO;
import edu.tamu.tcat.trc.entries.types.reln.repo.EditRelationshipCommand;
import edu.tamu.tcat.trc.entries.types.reln.repo.RelationshipChangeEvent;
import edu.tamu.tcat.trc.entries.types.reln.repo.RelationshipRepository;
import edu.tamu.tcat.trc.entries.types.reln.repo.RelationshipTypeRegistry;
import edu.tamu.tcat.trc.repo.BasicSchemaBuilder;
import edu.tamu.tcat.trc.repo.DocumentRepository;
import edu.tamu.tcat.trc.repo.IdFactory;
import edu.tamu.tcat.trc.repo.IdFactoryProvider;
import edu.tamu.tcat.trc.repo.RepositoryException;
import edu.tamu.tcat.trc.repo.RepositorySchema;
import edu.tamu.tcat.trc.repo.SchemaBuilder;
import edu.tamu.tcat.trc.repo.postgres.PsqlJacksonRepoBuilder;

public class RelationshipRepositoryImpl implements RelationshipRepository
{
   
   private static final Logger logger = Logger.getLogger(RelationshipRepositoryImpl.class.getName());
   
   public static final String ID_CONTEXT = "relationships";

   private static final String TABLE_NAME = "relationships";
   private static final String SCHEMA_ID = "trcRelationship";
   private static final String SCHEMA_DATA_FIELD = "relationship";
   
   private DocumentRepository<Relationship, RelationshipDTO, EditRelationshipCommand> repoBackend;
   private SqlExecutor exec;
   private IdFactory idFactory;
   private IdFactoryProvider idFactoryProvider;
   private RelationshipTypeRegistry typeReg;
   

   public void setDatabaseExecutor(SqlExecutor exec)
   {
      this.exec = exec;
   }

   public void setIdFactory(IdFactoryProvider idFactoryProvider)
   {
      this.idFactoryProvider = idFactoryProvider;
   }

   public void setTypeRegistry(RelationshipTypeRegistry typeReg)
   {
      this.typeReg = typeReg;
   }

   public void activate()
   {
      Objects.requireNonNull(exec, "No SQL executor provided");
      
      repoBackend = buildDocumentRepository();
      idFactory = idFactoryProvider.getIdFactory(ID_CONTEXT);
   }

   public void dispose()
   {
      exec = null;
   }
   
   private DocumentRepository<Relationship, RelationshipDTO, EditRelationshipCommand> buildDocumentRepository()
   {
      PsqlJacksonRepoBuilder<Relationship, RelationshipDTO, EditRelationshipCommand> repoBuilder = new PsqlJacksonRepoBuilder<>();
      
      repoBuilder.setDbExecutor(exec);
      repoBuilder.setTableName(TABLE_NAME);
      repoBuilder.setEditCommandFactory(new EditRelationshipCommandFactory(idFactoryProvider));
      repoBuilder.setDataAdapter(dto -> ModelAdapter.adapt(dto, typeReg));
      repoBuilder.setSchema(buildSchema());
      repoBuilder.setStorageType(RelationshipDTO.class);
      repoBuilder.setEnableCreation(true);
      
      try
      {
         return repoBuilder.build();
      }
      catch (RepositoryException e)
      {
         logger.log(Level.SEVERE, "Failed to construct relationship repository instance.", e);
      }
      
      return null;
   }

   private RepositorySchema buildSchema()
   {
      SchemaBuilder schemaBuilder = new BasicSchemaBuilder();
      schemaBuilder.setId(SCHEMA_ID);
      schemaBuilder.setDataField(SCHEMA_DATA_FIELD);
      return schemaBuilder.build();
   }

   @Override
   public Iterator<Relationship> getAllRelationships()
   {
      try
      {
         return repoBackend.listAll();
      }
      catch (RepositoryException e)
      {
         throw new IllegalStateException("Unable to list all relationships", e);
      }
   }

   @Override
   public Relationship get(String id) throws RepositoryException
   {
      try
      {
         return repoBackend.get(id);
      }
      catch (RepositoryException e)
      {
         throw new IllegalArgumentException("Unable to find relationship with id {" + id + "}.", e);
      }
   }

   @Override
   public EditRelationshipCommand create() throws RepositoryException
   {
      String id = idFactory.get();
      return repoBackend.create(id);
   }

   @Override
   public EditRelationshipCommand edit(String id) throws RepositoryException
   {
      try
      {
         return repoBackend.edit(id);
      }
      catch (RepositoryException e)
      {
         throw new IllegalArgumentException("Unable to find relationship with id {" + id + "}.", e);
      }
   }

   @Override
   public void delete(String id) throws RepositoryException
   {
      try 
      {
         repoBackend.delete(id).get();
      }
      catch (Exception e) 
      {
         throw new IllegalStateException("Encountered an unexpected error while trying to delete relationship with id {" + id + "}.", e);
      }

   }

   @Override
   public AutoCloseable addUpdateListener(Consumer<RelationshipChangeEvent> ears)
   {
      return null;
   }

}
