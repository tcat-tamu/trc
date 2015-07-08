package edu.tamu.tcat.trc.notes.postgres;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import org.postgresql.util.PGobject;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.tamu.tcat.db.exec.sql.SqlExecutor;
import edu.tamu.tcat.trc.entries.notification.DataUpdateObserverAdapter;
import edu.tamu.tcat.trc.entries.notification.EntryUpdateHelper;
import edu.tamu.tcat.trc.entries.notification.ObservableTaskWrapper;
import edu.tamu.tcat.trc.notes.Note;
import edu.tamu.tcat.trc.notes.dto.NotesDTO;
import edu.tamu.tcat.trc.notes.postgres.PsqlNotesRepo.UpdateEventFactory;
import edu.tamu.tcat.trc.notes.repo.EditNotesCommand;
import edu.tamu.tcat.trc.notes.repo.NoteChangeEvent;

public class EditNotesCmdImpl extends BasicEditNotesCommand implements EditNotesCommand
{
   private static String CREATE_SQL =
         "INSERT INTO notes (note, note_id) VALUES(?, ?)";
   private static String UPDATE_SQL =
         "UPDATE notes "
         + " SET note = ?, "
         +     " modified = now() "
         +"WHERE note_id = ?";


   private final SqlExecutor sqlExecutor;
   private final EntryUpdateHelper<NoteChangeEvent> notifier;
   private final UpdateEventFactory factory;

   private final AtomicBoolean executed = new AtomicBoolean(false);

   public EditNotesCmdImpl(SqlExecutor sqlExecutor,
                             EntryUpdateHelper<NoteChangeEvent> notifier,
                             UpdateEventFactory factory,
                             NotesDTO dto)
   {
      super(dto);

      this.sqlExecutor = sqlExecutor;
      this.notifier = notifier;
      this.factory = factory;
   }

   public EditNotesCmdImpl(SqlExecutor sqlExecutor,
                           EntryUpdateHelper<NoteChangeEvent> notifier,
                           UpdateEventFactory factory)
   {
      super();

      this.sqlExecutor = sqlExecutor;
      this.notifier = notifier;
      this.factory = factory;

   }

   @Override
   public synchronized Future<Note> execute()
   {
      if (!executed.compareAndSet(false, true))
         throw new IllegalStateException("This edit copy command has already been invoked.");

      NoteChangeEvent evt = constructEvent();

      String sql = isNew() ? CREATE_SQL : UPDATE_SQL;
      if (dto.id == null)
         dto.id = UUID.randomUUID();

      return sqlExecutor.submit(new ObservableTaskWrapper<Note>(
            makeCreateTask(sql),
            new DataUpdateObserverAdapter<Note>()
            {
               @Override
               protected void onFinish(Note result) {
                  notifier.after(evt);
               }
            }));
   }

   private NoteChangeEvent constructEvent()
   {
      Note updated = NotesDTO.instantiate(dto);
      NoteChangeEvent evt = isNew()
            ? factory.create(updated)
            : factory.update(original, updated);
      return evt;
   }

   private SqlExecutor.ExecutorTask<Note> makeCreateTask(String sql)
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

            return NotesDTO.instantiate(dto);
         }
         catch(SQLException e)
         {
            throw new IllegalStateException("Failed to update note reference [" + dto.id + "]. "
                  + "\n\tEntry [" + dto.associatedEntity + "]"
                  + "\n\tCopy  [" + dto.id + "]", e);
         }
      };
   }


}
