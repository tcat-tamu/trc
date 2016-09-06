package edu.tamu.tcat.trc.entries.types.bio.postgres;

import java.util.Iterator;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

import edu.tamu.tcat.db.exec.sql.SqlExecutor;
import edu.tamu.tcat.trc.entries.notification.UpdateListener;
import edu.tamu.tcat.trc.entries.types.bio.Person;
import edu.tamu.tcat.trc.entries.types.bio.postgres.model.PersonImpl;
import edu.tamu.tcat.trc.entries.types.bio.repo.EditPersonCommand;
import edu.tamu.tcat.trc.entries.types.bio.repo.PeopleRepository;
import edu.tamu.tcat.trc.entries.types.bio.repo.PersonChangeEvent;
import edu.tamu.tcat.trc.repo.BasicSchemaBuilder;
import edu.tamu.tcat.trc.repo.DocumentRepository;
import edu.tamu.tcat.trc.repo.IdFactory;
import edu.tamu.tcat.trc.repo.IdFactoryProvider;
import edu.tamu.tcat.trc.repo.NoSuchEntryException;
import edu.tamu.tcat.trc.repo.RepositoryException;
import edu.tamu.tcat.trc.repo.RepositorySchema;
import edu.tamu.tcat.trc.repo.SchemaBuilder;
import edu.tamu.tcat.trc.repo.postgres.PsqlJacksonRepoBuilder;

public class PeopleRepositoryImpl implements PeopleRepository
{

   private static final Logger logger = Logger.getLogger(PeopleRepositoryImpl.class.getName());

   public static final String ID_CONTEXT = "people";

   private static final String TABLE_NAME = "people";
   private static final String SCHEMA_ID = "trcPerson";
   private static final String SCHEMA_DATA_FIELD = "historical_figure";

   private DocumentRepository<Person, DataModelV1.Person, EditPersonCommand> repo;
   private SqlExecutor exec;
   private IdFactory idFactory;

   public void setDatabaseExecutor(SqlExecutor exec)
   {
      this.exec = exec;
   }

   public void setIdFactory(IdFactoryProvider idFactoryProvider)
   {
      this.idFactory = idFactoryProvider.getIdFactory(ID_CONTEXT);
   }

   public void activate()
   {
      Objects.requireNonNull(exec, "No SQL executor provided");
      repo = buildDocumentRepository();
   }

   private DocumentRepository<Person, DataModelV1.Person, EditPersonCommand> buildDocumentRepository()
   {
      PsqlJacksonRepoBuilder<Person, DataModelV1.Person, EditPersonCommand> builder = new PsqlJacksonRepoBuilder<>();

      builder.setDbExecutor(exec);
      builder.setTableName(TABLE_NAME);
      builder.setEditCommandFactory(new EditPersonCommandFactory());
      builder.setDataAdapter(dto -> adapt(dto));
      builder.setSchema(buildSchema());
      builder.setStorageType(DataModelV1.Person.class);
      builder.setEnableCreation(true);

      return builder.build();
   }

   private RepositorySchema buildSchema()
   {
      SchemaBuilder schemaBuilder = new BasicSchemaBuilder();
      schemaBuilder.setId(SCHEMA_ID);
      schemaBuilder.setDataField(SCHEMA_DATA_FIELD);
      return schemaBuilder.build();
   }

   @Override
   public Person get(String personId) throws NoSuchEntryException
   {
      return repo.get(personId);
   }

   @Override
   public Iterator<Person> listAll() throws RepositoryException
   {
      return repo.listAll();
   }

   @Override
   public EditPersonCommand create()
   {
      String id = idFactory.get();
      return repo.create(id);
   }

   @Override
   public EditPersonCommand create(String id)
   {
      return repo.create(id);
   }

   @Override
   public EditPersonCommand update(String personId) throws NoSuchEntryException
   {
      return repo.edit(personId);
   }

   @Override
   public CompletableFuture<Boolean> delete(String personId) throws NoSuchEntryException
   {
      return repo.delete(personId);
   }

   @Override
   public AutoCloseable addUpdateListener(UpdateListener<PersonChangeEvent> ears)
   {
      return null;
   }

   public static Person adapt(DataModelV1.Person dto)
   {
      return new PersonImpl(dto);
   }
}
