package org.tamu.tcat.trc.persist.postgres.id;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import org.tamu.tcat.trc.persist.IdFactory;

import edu.tamu.tcat.db.exec.sql.SqlExecutor;

public class DbBackedObfuscatingIdFactory implements IdFactory
{
   private final static Logger logger = Logger.getLogger(DbBackedObfuscatingIdFactory.class.getName());

   private SqlExecutor exec;
   private final ConcurrentHashMap<String, IdGenerator> generators = new ConcurrentHashMap<>();

   public DbBackedObfuscatingIdFactory()
   {
      // TODO Auto-generated constructor stub
   }

   public void setDatabaseExecutor(SqlExecutor exec)
   {
      this.exec = exec;
   }

   public void activate()
   {
      logger.info("Activating Id factory");
      Objects.requireNonNull(exec, "SqlExecutor not available.");
   }

   public void dispose()
   {
      logger.info("Shuting down Id factory");
      this.exec = null;
   }

   @Override
   public String getNextId(String context)
   {
      if (!generators.contains(context))
      {
         generators.putIfAbsent(context, new IdGenerator(context));
      }

      long id = generators.get(context).next();
      return obfuscate(id);
   }

   private String obfuscate(long id)
   {
      // TODO actually perform obfuscation
      return Long.toString(id);
   }

   private final class GrantProvider
   {
      private final String context;
      private final int grantSize;

      public GrantProvider(String ctx, int size)
      {
         context = ctx;
         this.grantSize = size;
      }

      public IdGrant requestIdGrant() // throws IdGrantCreationException
      {
         try
         {
            GetIdGrantTask task = new GetIdGrantTask(context, grantSize);   // HACK: magic number
            IdGrant idGrant = exec.submit(task).get();
            return idGrant;
         }
         catch (Exception e)
         {
            throw new IllegalStateException("Failed to generate id grant for context [" + context + "]", e);
         }
      }
   }

   private class IdGenerator
   {
      private final GrantProvider grantProvider;
      private IdGrant grant;

      public long nextId = 0;

      public IdGenerator(String context)
      {
         grantProvider = new GrantProvider(context, 20);
      }

      public synchronized long next()
      {
         if (grant == null || this.nextId > grant.limit)
            renewGrant();

         return this.nextId++;
      }

      private void renewGrant()
      {
         grant = grantProvider.requestIdGrant();
         this.nextId = grant.initial;
      }
   }

   /**
    * Retrieves a new ID grant from the database and updates the DB to ensure that the
    * next range of IDs doesn't overlap.
    */
   private static class GetIdGrantTask implements SqlExecutor.ExecutorTask<IdGrant>
   {
      private static final int MAX_ATTEMPTS = 5;     // HACK: need to retrieve from a

      private static final String QUERY = "SELECT next_id FROM id_table WHERE context = ?";
      private static final String UPDATE = "UPDATE id_table SET next_id = ? WHERE context = ? AND next_id = ?";
      private static final String CREATE = "INSERT INTO  id_table (next_id, context) VALUES (?, ?)";

      private final String context;
      private final long increment;

      private long initial;
      private long limit;
      private boolean isNewContext;

      public GetIdGrantTask(String context, long increment)
      {
         this.context = context;
         this.increment = increment;
      }

      @Override
      public IdGrant execute(Connection conn)
      {
         int attempts = 0;
         do {
            if (attempts > MAX_ATTEMPTS)
               throw new IllegalStateException("Failed to retrieve id from database after " + attempts + " tries.");

            attempts++;
            getNextIdSequence(conn);
         } while (!updateIdTable(conn));

         return new IdGrant(context, initial, limit);
      }

      private void getNextIdSequence(Connection conn)
      {
         try (PreparedStatement stmt = conn.prepareStatement(QUERY))
         {
            stmt.setString(1, context);
            try (ResultSet rs = stmt.executeQuery())
            {
               if (!rs.next())
               {
                  initial = 1;
                  limit = initial + increment;
                  isNewContext = true;
               }
               else
               {
                  initial = rs.getLong("next_id");
                  limit = initial + increment;
                  isNewContext = false;
               }
            }
         }
         catch (SQLException e)
         {
            throw new IllegalStateException("Failed to query id table", e);
         }
      }

      private boolean updateIdTable(Connection conn)
      {
         return (isNewContext) ?  insertLimit(conn) : updateLimit(conn);
      }

      private boolean updateLimit(Connection conn)
      {
         try (PreparedStatement stmt = conn.prepareStatement(UPDATE))
         {
            stmt.setLong(1, limit + 1);
            stmt.setString(2, context);
            stmt.setLong(3, initial);

            int ct = stmt.executeUpdate();
            return ct == 1;
         }
         catch (SQLException e)
         {
            return false;
         }
      }

      private boolean insertLimit(Connection conn)
      {
         try (PreparedStatement stmt = conn.prepareStatement(CREATE))
         {
            stmt.setLong(1, limit + 1);
            stmt.setString(2, context);

            int ct = stmt.executeUpdate();
            return ct == 1;
         }
         catch (SQLException e)
         {
            return false;
         }
      }
   }

   /**
    * Represents a range of identifiers that can be returned. An IdGrant is provided
    * by some underlying gatekeeper (such as a database) that ensure that all grants
    * are issued for distinct id ranges. A grant holder can issue identifiers in the
    * supplied range without returning to the gatekeeper to update the persistent
    * mechanism that guards against duplicate id generation.
    */
   private static class IdGrant
   {
      /** The context for this grant. */
      public final String context;

      /** The first ID authorized to be returned. */
      public final long initial;

      /** The last ID authorized to be returned. */
      public final long limit;

      public IdGrant(String context, long initial, long limit)
      {
         this.context = context;
         this.initial = initial;
         this.limit = limit;
      }
   }
}
