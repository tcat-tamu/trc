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

import edu.tamu.tcat.account.store.AccountStore;
import edu.tamu.tcat.db.exec.sql.SqlExecutor;
import edu.tamu.tcat.osgi.config.ConfigurationProperties;
import edu.tamu.tcat.trc.impl.psql.entries.DbEntryRepositoryRegistry;
import edu.tamu.tcat.trc.impl.psql.services.ServiceFactory;
import edu.tamu.tcat.trc.repo.postgres.PsqlJacksonRepo;
import edu.tamu.tcat.trc.repo.postgres.PsqlJacksonRepoBuilder;
import edu.tamu.tcat.trc.resolver.EntryId;
import edu.tamu.tcat.trc.resolver.EntryResolverRegistry;
import edu.tamu.tcat.trc.search.solr.SearchServiceManager;
import edu.tamu.tcat.trc.services.ServiceContext;
import edu.tamu.tcat.trc.services.TrcServiceException;
import edu.tamu.tcat.trc.services.notes.EditNoteCommand;
import edu.tamu.tcat.trc.services.notes.Note;
import edu.tamu.tcat.trc.services.notes.NotesService;

public class NotesServiceFactory implements ServiceFactory<NotesService>
{
   private final static Logger logger = Logger.getLogger(NotesServiceFactory.class.getName());

   public static final String PARAM_TABLE_NAME = "trc.services.notes.tablename";
   public static final String PARAM_ID_CTX = "trc.services.notes.id_context";

   private static final String TABLE_NAME = "notes";
   public static final String SCHEMA_DATA_FIELD = "data";

   private final DbEntryRepositoryRegistry repoRegistry;
   private final AccountStore acctStore;
   private final PsqlJacksonRepo<Note, DataModelV1.Note, EditNoteCommand> docRepo;

   private SearchServiceManager indexSvcMgr;

   private String tablename;

   public NotesServiceFactory(DbEntryRepositoryRegistry repoRegistry, AccountStore acctStore)
   {
      this.repoRegistry = repoRegistry;
      this.acctStore = acctStore;

      this.docRepo = initRepo();
   }

   private PsqlJacksonRepo<Note, DataModelV1.Note, EditNoteCommand> initRepo()
   {
      EditNoteCommandFactory cmdFactory = new EditNoteCommandFactory(repoRegistry.getResolverRegistry(), acctStore);

      ConfigurationProperties config = repoRegistry.getConfig();
      tablename = config.getPropertyValue(PARAM_TABLE_NAME, String.class, TABLE_NAME);

      PsqlJacksonRepoBuilder<Note, DataModelV1.Note, EditNoteCommand> builder = repoRegistry.getDocRepoBuilder();
      builder.setTableName(tablename);
      builder.setEditCommandFactory(cmdFactory);
      builder.setDataAdapter(dto -> new NoteImpl(dto, acctStore, repoRegistry.getResolverRegistry()));
      builder.setStorageType(DataModelV1.Note.class);

      return builder.build();
   }

   @Override
   public Class<NotesService> getType()
   {
      return NotesService.class;
   }

   /**
    * Lifecycle management method (usually called by framework service layer)
    * Called when this service is no longer required.
    */
   @Override
   public void shutdown()
   {
      docRepo.dispose();
   }

   @Override
   public NotesRepoImpl getService(ServiceContext<NotesService> svcCtx)
   {
      return new NotesRepoImpl(svcCtx, docRepo);
   }

   public class NotesRepoImpl implements NotesService
   {
      private final ServiceContext<NotesService> svcCtx;
      private final PsqlJacksonRepo<Note, DataModelV1.Note, EditNoteCommand> docRepo;

      public NotesRepoImpl(ServiceContext<NotesService> svcCtx,
                           PsqlJacksonRepo<Note, DataModelV1.Note, EditNoteCommand> docRepo)
      {
         this.svcCtx = svcCtx;
         this.docRepo = docRepo;
      }

      @Override
      public Optional<Note> get(String noteId)
      {
         try
         {
            return docRepo.get(noteId);
         }
         catch (Exception ex)
         {
            throw new TrcServiceException(format("Failed to retrive note {0}", noteId), ex);
         }
      }

      private NoteImpl adapt(DataModelV1.Note dto)
      {
         return new NoteImpl(dto, acctStore, repoRegistry.getResolverRegistry());
      }

      @Override
      public Collection<Note> getNotes(EntryId ref)
      {
         SqlExecutor sqlExecutor = repoRegistry.getSqlExecutor();
         EntryResolverRegistry resolvers = repoRegistry.getResolverRegistry();
         String token = resolvers.tokenize(ref);
         Future<Collection<DataModelV1.Note>> future =
               sqlExecutor.submit(conn -> doGetByToken(token, conn));

         try
         {
            Collection<DataModelV1.Note> dtos = future.get(10, TimeUnit.SECONDS);
            return dtos.stream()
                       .map(this::adapt)
                       .collect(Collectors.toList());
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
         return docRepo.create(svcCtx.getAccount().orElse(null));
      }

      @Override
      public EditNoteCommand edit(String noteId)
      {
         return docRepo.edit(svcCtx.getAccount().orElse(null), noteId);
      }

      @Override
      public CompletableFuture<Boolean> remove(String noteId)
      {
         return docRepo.delete(svcCtx.getAccount().orElse(null), noteId);
      }
   }

}
