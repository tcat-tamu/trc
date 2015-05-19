package edu.tamu.tcat.trc.entries.bib.copies.postgres;

import java.io.IOException;
import java.net.URI;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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

import edu.tamu.tcat.catalogentries.NoSuchCatalogRecordException;
import edu.tamu.tcat.db.exec.sql.SqlExecutor;
import edu.tamu.tcat.trc.entries.bib.copies.CopyReference;
import edu.tamu.tcat.trc.entries.bib.copies.CopyReferenceException;
import edu.tamu.tcat.trc.entries.bib.copies.CopyReferenceRepository;
import edu.tamu.tcat.trc.entries.bib.copies.EditCopyReferenceCommand;
import edu.tamu.tcat.trc.entries.bib.copies.model.CopyRefDTO;
import edu.tamu.tcat.trc.entries.notification.BasicUpdateEvent;
import edu.tamu.tcat.trc.entries.notification.DataUpdateObserverAdapter;
import edu.tamu.tcat.trc.entries.notification.EntryUpdateHelper;
import edu.tamu.tcat.trc.entries.notification.ObservableTaskWrapper;
import edu.tamu.tcat.trc.entries.notification.UpdateEvent;
import edu.tamu.tcat.trc.entries.notification.UpdateListener;
import edu.tamu.tcat.trc.entries.notification.UpdateEvent.UpdateAction;

public class PsqlDigitalCopyLinkRepo implements CopyReferenceRepository
{
   private static final Logger logger = Logger.getLogger(PsqlDigitalCopyLinkRepo.class.getName());

   private static final String GET_ALL_SQL =
         "SELECT reference "
        +  "FROM copy_references "
        + "WHERE reference->>'associatedEntry' LIKE ? AND active = true "
        + "ORDER BY reference->>'associatedEntry'";

   private static final String GET_SQL =
         "SELECT reference "
               +  "FROM copy_references "
               + "WHERE ref_id = ? AND active = true";
   private static final String GET_ANY_SQL =
         "SELECT reference "
        +  "FROM copy_references "
        + "WHERE ref_id = ?";
   private static final String REMOVE_SQL =
         "UPDATE copy_references "
         + " SET active = false, "
         +     " date_modified = now() "
         +"WHERE ref_id = ?";


   private SqlExecutor exec;

   private EntryUpdateHelper<CopyReference> listeners;

   private ObjectMapper mapper;

   public PsqlDigitalCopyLinkRepo()
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
   public EditCopyReferenceCommand create()
   {
      return new EditCopyRefCmdImpl(exec, listeners, new UpdateEventFactory());
   }

   @Override
   public EditCopyReferenceCommand edit(UUID id) throws NoSuchCatalogRecordException
   {
      CopyRefDTO dto = getCopyDTO(GET_SQL, id);
      return new EditCopyRefCmdImpl(exec, listeners, new UpdateEventFactory(), dto);
   }

   @Override
   public List<CopyReference> getCopies(URI entity)
   {
      Future<List<CopyReference>> results = exec.submit(conn -> {
         try (PreparedStatement ps = conn.prepareStatement(GET_ALL_SQL))
         {
            ps.setString(1, entity.toString() + "%");
            try (ResultSet rs = ps.executeQuery())
            {
               List<CopyReference> copies = new ArrayList<>();
               while (rs.next())
               {
                  PGobject pgo = (PGobject)rs.getObject("reference");
                  CopyRefDTO copy = parseCopyRefJson(pgo.toString());
                  copies.add(CopyRefDTO.instantiate(copy));
               }

               return copies;
            }
         }
         catch(SQLException e)
         {
            throw new IllegalStateException("Failed to retrive copy reference [" + entity + "]. ", e);
         }
      });

      try
      {
         return unwrapGetResults(results, entity.toString());
      }
      catch (NoSuchCatalogRecordException e)
      {
         // should not happen
         throw new IllegalStateException("Unexpected internal error", e);
      }
   }

   @Override
   public CopyReference get(UUID id) throws NoSuchCatalogRecordException
   {
      return CopyRefDTO.instantiate(getCopyDTO(GET_SQL, id));
   }

   @Override
   public Future<Boolean> remove(UUID id) throws CopyReferenceException
   {
      UpdateEventFactory factory = new UpdateEventFactory();
      UpdateEvent<CopyReference> evt = factory.makeDeleteEvent(id);

      boolean shouldExecute = listeners.before(evt);

      return exec.submit(new ObservableTaskWrapper<Boolean>(
            makeRemoveTask(id, shouldExecute),
            new DataUpdateObserverAdapter<Boolean>()
            {
               @Override
               protected void onFinish(Boolean result) {
                  if (result.booleanValue())
                     listeners.after(evt);
               }
            }));
   }

   private SqlExecutor.ExecutorTask<Boolean> makeRemoveTask(UUID id, boolean shouldExecute)
   {
      return (conn) -> {
         if (!shouldExecute)
            return Boolean.valueOf(false);

         try (PreparedStatement ps = conn.prepareStatement(REMOVE_SQL))
         {
            ps.setString(1, id.toString());
            int ct = ps.executeUpdate();
            if (ct == 0)
            {
               logger.log(Level.WARNING, "Failed to remove copy reference [" + id + "]. Reference may not exist.", id);
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

   public class UpdateEventFactory
   {
      public UpdateEvent<CopyReference> create(CopyReference newRef)
      {
         return new BasicUpdateEvent<>(newRef.getId().toString(),
                                       UpdateAction.CREATE,
                                       () -> null,
                                       () -> newRef);
      }

      public UpdateEvent<CopyReference> edit(CopyReference orig, CopyReference updated)
      {
         return new BasicUpdateEvent<>(updated.getId().toString(),
               UpdateAction.UPDATE,
               () -> orig,
               () -> updated);
      }

      public UpdateEvent<CopyReference> makeDeleteEvent(UUID id)
      {
         return new BasicUpdateEvent<>(id.toString(),
               UpdateAction.DELETE,
               () -> {
                  try {
                     return CopyRefDTO.instantiate(getCopyDTO(GET_ANY_SQL, id));
                  } catch (Exception ex) {
                     return null;
                  }
               },
               () -> null);
      }
   }

   private CopyRefDTO parseCopyRefJson(String json)
   {
      try
      {
         return mapper.readValue(json, CopyRefDTO.class);
      }
      catch (IOException e)
      {
         throw new IllegalStateException("Failed to parse relationship record\n" + json, e);
      }
   }

   private CopyRefDTO getCopyDTO(String sql, UUID id) throws NoSuchCatalogRecordException
   {
      Future<CopyRefDTO> result = exec.submit((conn) -> executeGetQuery(sql, conn, id));
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

   private CopyRefDTO executeGetQuery(String sql, Connection conn, UUID id) throws NoSuchCatalogRecordException
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
   
   @Override
   public AutoCloseable register(UpdateListener<CopyReference> ears)
   {
      Objects.requireNonNull(listeners, "Update registration is not available at this time.");
      return listeners.register(ears);
   }
}
