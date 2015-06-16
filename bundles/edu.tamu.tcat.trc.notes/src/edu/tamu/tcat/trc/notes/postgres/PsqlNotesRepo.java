package edu.tamu.tcat.trc.notes.postgres;

import java.io.IOException;
import java.net.URI;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.postgresql.util.PGobject;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.tamu.tcat.db.exec.sql.SqlExecutor;
import edu.tamu.tcat.trc.entries.notification.BaseUpdateEvent;
import edu.tamu.tcat.trc.entries.notification.DataUpdateObserverAdapter;
import edu.tamu.tcat.trc.entries.notification.EntryUpdateHelper;
import edu.tamu.tcat.trc.entries.notification.ObservableTaskWrapper;
import edu.tamu.tcat.trc.entries.notification.UpdateEvent;
import edu.tamu.tcat.trc.entries.notification.UpdateEvent.UpdateAction;
import edu.tamu.tcat.trc.entries.notification.UpdateListener;
import edu.tamu.tcat.trc.entries.repo.CatalogRepoException;
import edu.tamu.tcat.trc.entries.repo.NoSuchCatalogRecordException;
import edu.tamu.tcat.trc.notes.Notes;
import edu.tamu.tcat.trc.notes.dto.NotesDTO;
import edu.tamu.tcat.trc.notes.repo.EditNotesCommand;
import edu.tamu.tcat.trc.notes.repo.NoteChangeEvent;
import edu.tamu.tcat.trc.notes.repo.NotesRepository;

public class PsqlNotesRepo implements NotesRepository
{
   private static final Logger logger = Logger.getLogger(PsqlNotesRepo.class.getName());


   private static final String SQL_GET_ALL =
         "SELECT note "
        +  "FROM notes "
        + "WHERE reference->>'associatedEntry' LIKE ? AND active = true "
        + "ORDER BY reference->>'associatedEntry'";

   private static final String SQL_GET =
         "SELECT note "
               +  "FROM notes "
               + "WHERE note_id = ? AND active = true";

   private static final String SQL_GET_ALL_BY_ID =
         "SELECT note "
        +  "FROM notes "
        + "WHERE note_id = ?";

   private static final String SQL_REMOVE =
         "UPDATE notes "
         + " SET active = false, "
         +     " date_modified = now() "
         +"WHERE note_id = ?";

   //HACK: doesn't really matter for now, but once authz is in place, this will be the user's id
   private static final UUID ACCOUNT_ID_REPO = UUID.randomUUID();

   private EntryUpdateHelper<NoteChangeEvent> listeners;
   private SqlExecutor exec;
   private ObjectMapper mapper;

   public PsqlNotesRepo()
   {
   }

   public void setDatabaseExecutor(SqlExecutor exec)
   {
      this.exec = exec;
   }

   public void activate()
   {
      listeners = new EntryUpdateHelper<>();

      mapper = new ObjectMapper();
      mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
   }

   public void dispose()
   {
      this.exec = null;

      if (listeners != null)
         listeners.close();

      listeners = null;
      mapper = null;
   }

   @Override
   public Notes get(UUID noteId) throws NoSuchCatalogRecordException
   {
      return NotesDTO.instantiate(getNotesDTO(SQL_GET, noteId));
   }

   @Override
   public List<Notes> getNotes(URI entityURI) throws NoSuchCatalogRecordException
   {
      Future<List<Notes>> results = exec.submit(conn -> {
         try (PreparedStatement ps = conn.prepareStatement(SQL_GET_ALL))
         {
            ps.setString(1, entityURI.toString() + "%");
            try (ResultSet rs = ps.executeQuery())
            {
               List<Notes> notes = new ArrayList<>();
               while (rs.next())
               {
                  PGobject pgo = (PGobject)rs.getObject("note");
                  NotesDTO note = parseCopyRefJson(pgo.toString());
                  notes.add(NotesDTO.instantiate(note));
               }

               return notes;
            }
         }
         catch(SQLException e)
         {
            throw new IllegalStateException("Failed to retrive copy reference [" + entityURI + "]. ", e);
         }
      });

      try
      {
         return unwrapGetResults(results, entityURI.toString());
      }
      catch (NoSuchCatalogRecordException e)
      {
         throw new IllegalStateException("Unexpected internal error", e);
      }
   }

   @Override
   public EditNotesCommand create()
   {
      return new EditNotesCmdImpl(exec, listeners, new UpdateEventFactory());
   }

   @Override
   public EditNotesCommand edit(UUID noteId) throws NoSuchCatalogRecordException
   {
      NotesDTO dto = getNotesDTO(SQL_GET, noteId);
      return new EditNotesCmdImpl(exec, listeners, new UpdateEventFactory(), dto);
   }

   @Override
   public Future<Boolean> remove(UUID noteId)
   {
      UpdateEventFactory factory = new UpdateEventFactory();
      NoteChangeEvent evt = factory.remove(noteId);

      return exec.submit(new ObservableTaskWrapper<Boolean>(
            makeRemoveTask(noteId),
            new DataUpdateObserverAdapter<Boolean>()
            {
               @Override
               protected void onFinish(Boolean result) {
                  if (result.booleanValue())
                     listeners.after(evt);
               }
            }));
   }

   private SqlExecutor.ExecutorTask<Boolean> makeRemoveTask(UUID id)
   {
      return (conn) -> {
         try (PreparedStatement ps = conn.prepareStatement(SQL_REMOVE))
         {
            ps.setString(1, id.toString());
            int ct = ps.executeUpdate();
            if (ct == 0)
            {
               logger.log(Level.WARNING, "Failed to remove note reference [" + id + "]. Reference may not exist.", id);
               return Boolean.valueOf(false);
            }

            return Boolean.valueOf(true);
         }
         catch(SQLException e)
         {
            throw new IllegalStateException("Failed to remove copy reference [" + id + "]. ", e);
         }
      };
   }

   @Override
   public AutoCloseable register(UpdateListener<NoteChangeEvent> ears)
   {
      Objects.requireNonNull(listeners, "Registration for updates is not available.");
      return listeners.register(ears);
   }

   public class UpdateEventFactory
   {
      public NoteChangeEvent create(Notes note)
      {
         return new NoteChangeEventImpl(note.getId(),
                                       UpdateAction.CREATE,
                                       note);
      }

      public NoteChangeEvent update(Notes orig, Notes updated)
      {
         return new NoteChangeEventImpl(updated.getId(),
                                        UpdateAction.UPDATE,
                                        updated);
      }

      public NoteChangeEvent remove(UUID noteId)
      {
         Notes n = null;
         try {
            n = NotesDTO.instantiate(getNotesDTO(SQL_GET_ALL_BY_ID, noteId));
         } catch (Exception ex) {
            logger.log(Level.WARNING, "Failed accessing old value for deleted notes", ex);
         }
         return new NoteChangeEventImpl(noteId,
                                        UpdateAction.DELETE,
                                        n);
      }
   }

   private NotesDTO parseCopyRefJson(String json)
   {
      try
      {
         return mapper.readValue(json, NotesDTO.class);
      }
      catch (IOException e)
      {
         throw new IllegalStateException("Failed to parse relationship record\n" + json, e);
      }
   }

   private NotesDTO getNotesDTO(String sql, UUID id) throws NoSuchCatalogRecordException
   {
      Future<NotesDTO> result = exec.submit((conn) -> executeGetQuery(sql, conn, id));
      return unwrapGetResults(result, id.toString());
   }

   /**
   *
   * @param result The future to unwrap
   * @param id For error messaging purposes
   * @return
   * @throws NoSuchCatalogRecordException
   */
  private <T> T unwrapGetResults(Future<T> result, String id) throws NoSuchCatalogRecordException
  {
     try
     {
        return result.get();
     }
     catch (InterruptedException e)
     {
        throw new IllegalStateException("Failed to retrieve copy reference [" + id + "].", e);
     }
     catch (ExecutionException e)
     {
        // unwrap the execution exception that may be thrown from the executor
        Throwable cause = e.getCause();
        if (cause instanceof NoSuchCatalogRecordException)
           throw (NoSuchCatalogRecordException)cause;         // if not found
        else if (cause instanceof RuntimeException)
           throw (RuntimeException)cause;                     // 'expected' internal errors - json parsing, db access, etc
        else if (cause instanceof Error)
           throw (Error)cause;                                // OoM and other system errors
        else                                                  // unanticipated errors
           throw new IllegalStateException("Unknown error while attempting to retrive copy reference [" + id + "]", cause);
     }
  }

  private NotesDTO executeGetQuery(String sql, Connection conn, UUID id) throws NoSuchCatalogRecordException
  {
     try (PreparedStatement ps = conn.prepareStatement(sql))
     {
        ps.setString(1, id.toString());
        try (ResultSet rs = ps.executeQuery())
        {
           if (!rs.next())
              throw new NoSuchCatalogRecordException("No catalog record exists for work id=" + id);

           PGobject pgo = (PGobject)rs.getObject("reference");
           return parseCopyRefJson(pgo.toString());
        }
     }
     catch(SQLException e)
     {
        throw new IllegalStateException("Failed to retrive copy reference [" + id + "]. ", e);
     }
  }

   private class NoteChangeEventImpl extends BaseUpdateEvent implements NoteChangeEvent
   {
      private final Notes note;
      private final UUID noteId;

      public NoteChangeEventImpl(UUID id, UpdateEvent.UpdateAction type, Notes note)
      {
         super(id.toString(), type, ACCOUNT_ID_REPO, Instant.now());
         this.noteId = id;
         this.note = note;
      }

      @Override
      public Notes getNotes() throws CatalogRepoException
      {
         try
         {
            return get(noteId);
         }
         catch (Exception e)
         {
            throw new CatalogRepoException("Failed to retrieve relationship [" + id + "].", e);
         }
      }

      @Override
      public String toString()
      {
         return "Relationship Change " + super.toString();
      }
   }
}
