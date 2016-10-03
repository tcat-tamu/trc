package edu.tamu.tcat.trc.impl.psql.services.notes;

import static java.text.MessageFormat.format;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.tamu.tcat.account.Account;
import edu.tamu.tcat.account.store.AccountStore;
import edu.tamu.tcat.db.exec.sql.SqlExecutor;
import edu.tamu.tcat.osgi.config.ConfigurationProperties;
import edu.tamu.tcat.trc.entries.core.resolver.EntryReference;
import edu.tamu.tcat.trc.entries.core.resolver.EntryResolverRegistry;
import edu.tamu.tcat.trc.impl.psql.entries.DbEntryRepositoryRegistry;
import edu.tamu.tcat.trc.repo.DocumentNotFoundException;
import edu.tamu.tcat.trc.repo.postgres.PsqlJacksonRepo;
import edu.tamu.tcat.trc.repo.postgres.PsqlJacksonRepoBuilder;
import edu.tamu.tcat.trc.search.solr.SearchServiceManager;
import edu.tamu.tcat.trc.services.notes.EditNoteCommand;
import edu.tamu.tcat.trc.services.notes.Note;
import edu.tamu.tcat.trc.services.notes.NotesRepository;

public class NotesService
{
   private final static Logger logger = Logger.getLogger(NotesService.class.getName());

   public static final String PARAM_TABLE_NAME = "trc.services.notes.tablename";
   public static final String PARAM_ID_CTX = "trc.services.notes.id_context";

   private static final String TABLE_NAME = "notes";
   public static final String SCHEMA_DATA_FIELD = "data";

   private DbEntryRepositoryRegistry context;
   private SearchServiceManager indexSvcMgr;

//   private RepositoryContext.Registration repoReg;
//      private EntryResolverRegistry.Registration resolverReg;
//      private EntryRepository.ObserverRegistration searchReg;

   public PsqlJacksonRepo<Note, DataModelV1.Note, EditNoteCommand> docRepo;
   private AccountStore acctStore;

   private SqlExecutor sqlExecutor;

   private String tablename;

   /**
    * This depends directly on the DB-backed implementation because we will need to
    * poke the database directly. It may eventually be adequate to rely on search to find
    * results, but this seems unwarrented at the moment
    */
   public void setRepoContext(DbEntryRepositoryRegistry ctx)
   {
      this.context = ctx;
   }

   public void setAccountStore(AccountStore acctStore)
   {
      this.acctStore = acctStore;
   }

   public void setSearchSvcMgr(SearchServiceManager indexSvcFactory)
   {
      this.indexSvcMgr = indexSvcFactory;
   }

   /**
    * Lifecycle management method (usually called by framework service layer)
    * Called when all dependencies have been provided and the service is ready to run.
    */
   public void activate()
   {
      try
      {
         logger.info("Activating " + getClass().getSimpleName());

         initRepo();
         sqlExecutor = context.getSqlExecutor();
         // make sure these are all set up and fail fast if not.
         Objects.requireNonNull(docRepo);

//         initSearch();

         logger.fine("Activated " + getClass().getSimpleName());
      }
      catch (Exception e)
      {
         logger.log(Level.SEVERE, "Failed to construct articles repository instance.", e);
         throw e;
      }
   }

   /**
    * Lifecycle management method (usually called by framework service layer)
    * Called when this service is no longer required.
    */
   public void dispose()
   {
      try
      {
         logger.info("Stopping " + getClass().getSimpleName());

         docRepo.dispose();
         // TODO register with service framework
//         if (searchReg != null)
//            searchReg.close();

         logger.fine("Stopped " + getClass().getSimpleName());

      }
      catch (Exception ex)
      {
         logger.log(Level.SEVERE, "Failed to stop" + getClass().getSimpleName(), ex);
         throw ex;
      }
   }

   private void initRepo()
   {
      EditNoteCommandFactory cmdFactory = new EditNoteCommandFactory(context.getResolverRegistry(), acctStore);

      ConfigurationProperties config = context.getConfig();
      tablename = config.getPropertyValue(PARAM_TABLE_NAME, String.class, TABLE_NAME);

      PsqlJacksonRepoBuilder<Note, DataModelV1.Note, EditNoteCommand> builder = context.getDocRepoBuilder();
      builder.setTableName(tablename);
      builder.setDataColumn(SCHEMA_DATA_FIELD);
      builder.setEditCommandFactory(cmdFactory);
      builder.setDataAdapter(dto -> new NoteImpl(dto, acctStore, context.getResolverRegistry()));
      builder.setStorageType(DataModelV1.Note.class);

      docRepo = builder.build();
   }

   public NotesRepoImpl getRepository(Account account)
   {
      return new NotesRepoImpl(account, context.getResolverRegistry(), sqlExecutor, docRepo);
   }

   public class NotesRepoImpl implements NotesRepository
   {
      private Account account;
      private PsqlJacksonRepo<Note, DataModelV1.Note, EditNoteCommand> docRepo;
      private EntryResolverRegistry resolvers;
      private SqlExecutor exec;

      public NotesRepoImpl(Account account,
                           EntryResolverRegistry resolvers,
                           SqlExecutor exec,
                           PsqlJacksonRepo<Note, DataModelV1.Note, EditNoteCommand> docRepo)
      {
         this.resolvers = resolvers;
         this.docRepo = docRepo;
         this.account = account;
         this.exec = exec;
      }

      @Override
      public Optional<Note> get(String noteId)
      {
         try
         {
            return Optional.of(docRepo.get(noteId));
         }
         catch (DocumentNotFoundException ex)
         {
            return Optional.empty();
         }
         catch (Exception ex)
         {
            throw new IllegalStateException(format("Failed to retrive note {0}", noteId), ex);
         }
      }

      private NoteImpl adapt(DataModelV1.Note dto)
      {
         return new NoteImpl(dto, acctStore, resolvers);
      }

      @Override
      public Collection<Note> getNotes(EntryReference ref)
      {
         String token = resolvers.tokenize(ref);
         Future<Collection<DataModelV1.Note>> future =
               sqlExecutor.submit(conn -> doGetByToken(token, conn));

         try
         {
            Collection<DataModelV1.Note> dtos = future.get(10, TimeUnit.SECONDS);
            return dtos.stream().map(this::adapt).collect(Collectors.toList());
         }
         catch (ExecutionException ex)
         {
            Throwable cse = ex.getCause();
            if (Error.class.isInstance(cse))
            {
               throw (Error)cse;
            }
            else if (IllegalStateException.class.isInstance(cse))
            {
               throw (IllegalStateException)cse;
            }

            throw new IllegalStateException("Failed to load notes for " + ref.toString(), cse);
         }
         catch (InterruptedException | TimeoutException e)
         {
            throw new IllegalStateException(format("Failed to load notes for {0} in a timely fashion.", ref.toString()), e);
         }
      }

      private Collection<DataModelV1.Note> doGetByToken(String token, Connection conn)
      {
         String sqlTemplate = "SELECT {0} AS json"
               + " FROM {1} "
               + "WHERE {0}->>''entryRef'' = ? "
               + docRepo.buildNotRemovedClause();

         String sql = format(sqlTemplate, SCHEMA_DATA_FIELD, tablename);

         ObjectMapper mapper = new ObjectMapper();
         List<DataModelV1.Note> notes = new ArrayList<>();
         try (PreparedStatement stmt = conn.prepareStatement(sql))
         {
            stmt.setString(1, token);
            ResultSet rs = stmt.executeQuery();
            while (rs.next())
            {
               String json = rs.getString("json");
               try
               {
                  DataModelV1.Note note = mapper.readValue(json, DataModelV1.Note.class);
                  notes.add(note);
               }
               catch (IOException ioe)
               {
                  logger.log(Level.SEVERE, "Failed to parse value of note:\n\t" + json, ioe);
               }
            }

            return notes;
         }
         catch (SQLException e)
         {
            throw new IllegalStateException("Failed to read notes data from database.", e);
         }
      }

      @Override
      public EditNoteCommand create()
      {
         return docRepo.create(account);
      }

      @Override
      public EditNoteCommand edit(String noteId)
      {
         return docRepo.edit(account, noteId);
      }

      @Override
      public CompletableFuture<Boolean> remove(String noteId)
      {
         return docRepo.delete(account, noteId);
      }
   }

}
