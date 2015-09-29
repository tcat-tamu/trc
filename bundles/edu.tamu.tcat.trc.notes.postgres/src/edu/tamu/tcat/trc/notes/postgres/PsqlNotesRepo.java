/*
 * Copyright 2015 Texas A&M Engineering Experiment Station
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
import edu.tamu.tcat.trc.entries.notification.UpdateListener;
import edu.tamu.tcat.trc.entries.repo.NoSuchCatalogRecordException;
import edu.tamu.tcat.trc.notes.Note;
import edu.tamu.tcat.trc.notes.dto.NoteDTO;
import edu.tamu.tcat.trc.notes.repo.EditNoteCommand;
import edu.tamu.tcat.trc.notes.repo.NoteChangeEvent;
import edu.tamu.tcat.trc.notes.repo.NotesRepository;

public class PsqlNotesRepo implements NotesRepository
{
   private static final Logger logger = Logger.getLogger(PsqlNotesRepo.class.getName());

   private static final String SQL_GET_ALL =
         "SELECT note "
        +  "FROM notes "
        + "WHERE note->>'associatedEntity' LIKE ? AND active = true "
        + "ORDER BY note->>'associatedEntity'";

   private static final String SQL_GET =
         "SELECT note "
               +  "FROM notes "
               + "WHERE note_id = ? AND active = true";

   private static String CREATE_SQL =
         "INSERT INTO notes (note, note_id) VALUES(?, ?)";

   private static String UPDATE_SQL =
         "UPDATE notes "
         + " SET note = ?, "
         +     " modified = now() "
         +"WHERE note_id = ?";

   private static final String SQL_REMOVE =
         "UPDATE notes "
         + " SET active = FALSE, "
         +     " modified = now() "
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
      try
      {
         if (listeners != null)
            listeners.close();
      }
      catch (Exception ex)
      {
         logger.log(Level.WARNING, "Failed to shut down event notification helper.", ex);
      }

      // managed by supplier. no need to shut down
      this.exec = null;
      listeners = null;
      mapper = null;
   }

   @Override
   public Note get(UUID noteId) throws NoSuchCatalogRecordException
   {
      return adapt(getNotesDTO(SQL_GET, noteId));
   }

   @Override
   public List<Note> getNotes(URI entityURI) throws NoSuchCatalogRecordException
   {
      Future<List<Note>> results = exec.submit(conn -> {
         try (PreparedStatement ps = conn.prepareStatement(SQL_GET_ALL))
         {
            ps.setString(1, entityURI.toString() + "%");
            try (ResultSet rs = ps.executeQuery())
            {
               List<Note> notes = new ArrayList<>();
               while (rs.next())
               {
                  PGobject pgo = (PGobject)rs.getObject("note");
                  NoteDTO note = parseCopyRefJson(pgo.toString());
                  Note n = adapt(note);
                  notes.add(n);
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

   private static Note adapt(NoteDTO note)
   {
      return new PsqlNote(note.id, note.associatedEntity, note.authorId, note.mimeType, note.content);
   }

   @Override
   public EditNoteCommand create()
   {
      NoteDTO note = new NoteDTO();
      note.id = UUID.randomUUID();

      PostgresEditNoteCmd cmd = new PostgresEditNoteCmd(note);
      cmd.setCommitHook((n) -> {
         NoteChangeNotifier notifier = new NoteChangeNotifier(UpdateEvent.UpdateAction.CREATE);
         return exec.submit(new ObservableTaskWrapper<UUID>(makeSaveTask(n, CREATE_SQL), notifier));
      });

      return cmd;
   }

   @Override
   public EditNoteCommand edit(UUID noteId) throws NoSuchCatalogRecordException
   {
      NoteDTO note = getNotesDTO(SQL_GET, noteId);

      PostgresEditNoteCmd cmd = new PostgresEditNoteCmd(note);
      cmd.setCommitHook((n) -> {
         NoteChangeNotifier notifier = new NoteChangeNotifier(UpdateEvent.UpdateAction.UPDATE);
         return exec.submit(new ObservableTaskWrapper<UUID>(makeSaveTask(n, UPDATE_SQL), notifier));
      });

      return cmd;
   }

   @Override
   public Future<Boolean> remove(UUID noteId)
   {
      NoteChangeEvent evt = new NoteChangeEventImpl(noteId, UpdateEvent.UpdateAction.DELETE);
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

   @Override
   public AutoCloseable register(UpdateListener<NoteChangeEvent> ears)
   {
      Objects.requireNonNull(listeners, "Registration for updates is not available.");
      return listeners.register(ears);
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
               logger.log(Level.WARNING, "Failed to remove note  [" + id + "]. Reference may not exist.", id);
               return Boolean.valueOf(false);
            }

            return Boolean.valueOf(true);
         }
         catch(SQLException e)
         {
            throw new IllegalStateException("Failed to remove note [" + id + "]. ", e);
         }
      };
   }

   private SqlExecutor.ExecutorTask<UUID> makeSaveTask(NoteDTO dto, String sql)
   {
      ObjectMapper mapper = new ObjectMapper();
      mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

      return (conn) -> {
         try (PreparedStatement ps = conn.prepareStatement(sql))
         {
            PGobject jsonObject = new PGobject();
            jsonObject.setType("json");
            jsonObject.setValue(mapper.writeValueAsString(dto));

            ps.setObject(1, jsonObject);
            ps.setString(2, dto.id.toString());

            int cnt = ps.executeUpdate();
            if (cnt != 1)
               throw new IllegalStateException("Failed to update copy reference [" + dto.id +"]");

            return dto.id;
         }
         catch(SQLException e)
         {
            throw new IllegalStateException("Failed to update note reference [" + dto.id + "]. "
                  + "\n\tEntry [" + dto.associatedEntity + "]"
                  + "\n\tCopy  [" + dto.id + "]", e);
         }
      };
   }

   private NoteDTO parseCopyRefJson(String json)
   {
      try
      {
         return mapper.readValue(json, NoteDTO.class);
      }
      catch (IOException e)
      {
         throw new IllegalStateException("Failed to parse relationship record\n" + json, e);
      }
   }

   private NoteDTO getNotesDTO(String sql, UUID id) throws NoSuchCatalogRecordException
   {
      Future<NoteDTO> result = exec.submit((conn) -> executeGetQuery(sql, conn, id));
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

  private NoteDTO executeGetQuery(String sql, Connection conn, UUID id) throws NoSuchCatalogRecordException
  {
     try (PreparedStatement ps = conn.prepareStatement(sql))
     {
        ps.setString(1, id.toString());
        try (ResultSet rs = ps.executeQuery())
        {
           if (!rs.next())
              throw new NoSuchCatalogRecordException("No catalog record exists for work id=" + id);

           PGobject pgo = (PGobject)rs.getObject("note");
           return parseCopyRefJson(pgo.toString());
        }
     }
     catch(SQLException e)
     {
        throw new IllegalStateException("Failed to retrive copy reference [" + id + "]. ", e);
     }
  }

   private final class NoteChangeNotifier extends DataUpdateObserverAdapter<UUID>
   {
      private final UpdateEvent.UpdateAction type;

      public NoteChangeNotifier(UpdateEvent.UpdateAction type)
      {
         this.type = type;
      }

      @Override
      public void onFinish(UUID id)
      {
         listeners.after(new NoteChangeEventImpl(id, type));
      }
   }

   private class NoteChangeEventImpl extends BaseUpdateEvent implements NoteChangeEvent
   {
      public NoteChangeEventImpl(UUID id, UpdateEvent.UpdateAction type)
      {
         super(id.toString(), type, ACCOUNT_ID_REPO, Instant.now());
      }

      @Override
      public String toString()
      {
         return "Note Change " + super.toString();
      }
   }

   private static class PsqlNote implements Note
   {
      private final UUID id;
      private final URI associatedEntity;
      private final String authorId;
      private final String mimeType;
      private final String content;

      public PsqlNote(UUID id, URI associatedEntity, String authorId, String mimeType, String content)
      {
         this.id = id;
         this.associatedEntity = associatedEntity;
         this.authorId = authorId;
         this.mimeType = mimeType;
         this.content = content;
      }

      @Override
      public UUID getId()
      {
         return id;
      }

      @Override
      public URI getEntity()
      {
         return associatedEntity;
      }

      @Override
      public UUID getAuthorId()
      {
         return authorId == null ? null : UUID.fromString(authorId);
      }

      @Override
      public String getMimeType()
      {
         return mimeType;
      }

      @Override
      public String getContent()
      {
         return content;
      }
   }
}
