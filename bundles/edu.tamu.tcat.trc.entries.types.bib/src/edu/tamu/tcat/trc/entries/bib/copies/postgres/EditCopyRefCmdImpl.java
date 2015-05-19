package edu.tamu.tcat.trc.entries.bib.copies.postgres;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import org.postgresql.util.PGobject;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.tamu.tcat.db.exec.sql.SqlExecutor;
import edu.tamu.tcat.trc.entries.bib.UpdateCanceledException;
import edu.tamu.tcat.trc.entries.bib.copies.CopyReference;
import edu.tamu.tcat.trc.entries.bib.copies.EditCopyReferenceCommand;
import edu.tamu.tcat.trc.entries.bib.copies.model.BaseEditCopyRefCmd;
import edu.tamu.tcat.trc.entries.bib.copies.model.CopyRefDTO;
import edu.tamu.tcat.trc.entries.bib.copies.postgres.PsqlDigitalCopyLinkRepo.UpdateEventFactory;
import edu.tamu.tcat.trc.entries.notification.DataUpdateObserverAdapter;
import edu.tamu.tcat.trc.entries.notification.EntryUpdateHelper;
import edu.tamu.tcat.trc.entries.notification.ObservableTaskWrapper;
import edu.tamu.tcat.trc.entries.notification.UpdateEvent;

public class EditCopyRefCmdImpl extends BaseEditCopyRefCmd implements EditCopyReferenceCommand
{
   //  NOTE: table needs date_created, active
   private static String CREATE_SQL =
         "INSERT INTO copy_references (reference, ref_id) VALUES(?, ?)";
   private static String UPDATE_SQL =
         "UPDATE copy_references "
         + " SET reference = ?, "
         +     " modified = now() "
         +"WHERE ref_id = ?";


   private final SqlExecutor sqlExecutor;
   private final EntryUpdateHelper<CopyReference> notifier;
   private final UpdateEventFactory factory;

   private final AtomicBoolean executed = new AtomicBoolean(false);

   public EditCopyRefCmdImpl(SqlExecutor sqlExecutor,
                             EntryUpdateHelper<CopyReference> notifier,
                             UpdateEventFactory factory,
                             CopyRefDTO dto)
   {
      super(dto);

      this.sqlExecutor = sqlExecutor;
      this.notifier = notifier;
      this.factory = factory;
   }

   public EditCopyRefCmdImpl(SqlExecutor sqlExecutor,
                             EntryUpdateHelper<CopyReference> notifier,
                             UpdateEventFactory factory)
   {
      super();

      this.sqlExecutor = sqlExecutor;
      this.notifier = notifier;
      this.factory = factory;

   }

   @Override
   public synchronized Future<CopyReference> execute() throws UpdateCanceledException
   {
      if (!executed.compareAndSet(false, true))
         throw new IllegalStateException("This edit copy command has already been invoked.");

      UpdateEvent<CopyReference> evt = constructEvent();
      if (!notifier.before(evt))
         throw new UpdateCanceledException();

      String sql = isNew() ? CREATE_SQL : UPDATE_SQL;
      if (dto.id == null)
         dto.id = UUID.randomUUID();

      return sqlExecutor.submit(new ObservableTaskWrapper<CopyReference>(
            makeCreateTask(sql),
            new DataUpdateObserverAdapter<CopyReference>()
            {
               @Override
               protected void onFinish(CopyReference result) {
                  notifier.after(evt);
               }
            }));
   }

   private UpdateEvent<CopyReference> constructEvent()
   {
      CopyReference updated = CopyRefDTO.instantiate(dto);
      UpdateEvent<CopyReference> evt = isNew()
            ? factory.create(updated)
            : factory.edit(original, updated);
      return evt;
   }

   private SqlExecutor.ExecutorTask<CopyReference> makeCreateTask(String sql)
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

            return CopyRefDTO.instantiate(dto);
         }
         catch(SQLException e)
         {
            throw new IllegalStateException("Failed to update copy reference [" + dto.id + "]. "
                  + "\n\tEntry [" + dto.associatedEntry + "]"
                  + "\n\tCopy  [" + dto.copyId + "]", e);
         }
      };
   }
}
