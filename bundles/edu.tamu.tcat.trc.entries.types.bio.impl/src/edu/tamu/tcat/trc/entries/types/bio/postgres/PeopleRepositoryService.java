package edu.tamu.tcat.trc.entries.types.bio.postgres;

import java.util.Iterator;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.tamu.tcat.account.Account;
import edu.tamu.tcat.db.exec.sql.SqlExecutor;
import edu.tamu.tcat.osgi.config.ConfigurationProperties;
import edu.tamu.tcat.trc.entries.core.InvalidReferenceException;
import edu.tamu.tcat.trc.entries.core.repo.BasicRepoDelegate;
import edu.tamu.tcat.trc.entries.core.repo.EntryRepository;
import edu.tamu.tcat.trc.entries.core.repo.RepositoryContext;
import edu.tamu.tcat.trc.entries.core.repo.UnauthorziedException;
import edu.tamu.tcat.trc.entries.core.resolver.EntryReference;
import edu.tamu.tcat.trc.entries.core.resolver.EntryResolverBase;
import edu.tamu.tcat.trc.entries.types.bio.Person;
import edu.tamu.tcat.trc.entries.types.bio.postgres.model.PersonImpl;
import edu.tamu.tcat.trc.entries.types.bio.repo.EditPersonCommand;
import edu.tamu.tcat.trc.entries.types.bio.repo.PeopleRepository;
import edu.tamu.tcat.trc.repo.DocRepoBuilder;
import edu.tamu.tcat.trc.repo.DocumentRepository;
import edu.tamu.tcat.trc.repo.IdFactory;
import edu.tamu.tcat.trc.repo.NoSuchEntryException;
import edu.tamu.tcat.trc.repo.RepositoryException;

public class PeopleRepositoryService
{
   private static final Logger logger = Logger.getLogger(PeopleRepositoryService.class.getName());

   public static final String ID_CONTEXT = "people";

   private static final String TABLE_NAME = "people";
   private static final String SCHEMA_ID = "trcPerson";
   private static final String SCHEMA_DATA_FIELD = "historical_figure";

   private DocumentRepository<Person, DataModelV1.Person, EditPersonCommand> repo;
   private SqlExecutor exec;
   private IdFactory bioIds;

   private RepositoryContext ctx;
   private ConfigurationProperties config;

   private DocumentRepository<Person, DataModelV1.Person, EditPersonCommand> docRepo;
   private BasicRepoDelegate<Person, DataModelV1.Person, EditPersonCommand> delegate;

   public void setRepoContext(RepositoryContext ctx)
   {
      this.ctx = ctx;
   }

   public void activate()
   {
      try
      {
         logger.info("Activating relationship repository service. . . ");
         this.bioIds = ctx.getIdFactory(ID_CONTEXT);
         this.config = ctx.getConfig();

         initDocumentStore();
         initDelegate();

         ctx.registerResolver(new BioEntryResolver());
         ctx.registerRepository(PeopleRepository.class, account -> new BioEntryRepoImpl(delegate, account));

         logger.fine("Activated relationship repository service.");

      }
      catch (Exception ex)
      {
         logger.log(Level.SEVERE, "Failed to activate relationship repository service.", ex);
         throw ex;
      }
   }

   private void initDocumentStore()
   {
      DocRepoBuilder<Person, DataModelV1.Person, EditPersonCommand> builder = ctx.getDocRepoBuilder();
      builder.setTableName(TABLE_NAME);
      builder.setDataColumn(SCHEMA_DATA_FIELD);
      builder.setEditCommandFactory(new EditPersonCommandFactory());
      builder.setDataAdapter(PeopleRepositoryService::adapt);
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
      delegateBuilder.setAdapter(PeopleRepositoryService::adapt);
      delegateBuilder.setDocumentRepo(docRepo);

      delegate = delegateBuilder.build();
   }

   public static Person adapt(DataModelV1.Person dto)
   {
      return new PersonImpl(dto);
   }

   public static class BioEntryRepoImpl implements PeopleRepository
   {
      private final Account account;
      private final BasicRepoDelegate<Person, DataModelV1.Person, EditPersonCommand> delegate;

      BioEntryRepoImpl(BasicRepoDelegate<Person, DataModelV1.Person, EditPersonCommand> delegate, Account account)
      {
         this.delegate = delegate;
         this.account = account;
      }

      @Override
      public Person get(String id) throws NoSuchEntryException
      {
         return delegate.get(account, id);
      }

      @Override
      public Iterator<Person> listAll() throws RepositoryException
      {
         return delegate.listAll();
      }

      @Override
      public EditPersonCommand create()
      {
         return delegate.create(account);
      }

      @Override
      public EditPersonCommand create(String id)
      {
         return delegate.create(account, id);
      }

      @Override
      public EditPersonCommand edit(String id) throws NoSuchEntryException
      {
         return delegate.edit(account, id);
      }

      @Override
      public CompletableFuture<Boolean> remove(String id) throws NoSuchEntryException
      {
         return delegate.remove(account, id);
      }

      @Override
      public EntryRepository.ObserverRegistration onUpdate(EntryRepository.UpdateObserver<Person> observer)
      {
         return delegate.onUpdate(observer, account);
      }
   }

   private class BioEntryResolver extends EntryResolverBase<Person>
   {
      public BioEntryResolver()
      {
         super(Person.class, config, PeopleRepository.ENTRY_URI_BASE, PeopleRepository.ENTRY_TYPE_ID);
      }

      @Override
      public Person resolve(Account account, EntryReference reference) throws InvalidReferenceException
      {
         if (!accepts(reference))
            throw new InvalidReferenceException(reference, "Unsupported reference type.");

         return delegate.get(account, reference.id);
      }

      @Override
      protected String getId(Person person)
      {
         return person.getId();
      }

      @Override
      public CompletableFuture<Boolean> remove(Account account, EntryReference reference) throws InvalidReferenceException, UnauthorziedException, UnsupportedOperationException
      {
         return delegate.remove(account, reference.id);
      }
   }
}
