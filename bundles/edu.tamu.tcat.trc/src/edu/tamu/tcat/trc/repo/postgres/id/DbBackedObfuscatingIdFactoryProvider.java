package edu.tamu.tcat.trc.repo.postgres.id;

import static java.text.MessageFormat.format;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.tamu.tcat.db.exec.sql.SqlExecutor;
import edu.tamu.tcat.trc.repo.IdFactory;
import edu.tamu.tcat.trc.repo.IdFactoryProvider;

public class DbBackedObfuscatingIdFactoryProvider implements IdFactoryProvider
{
   private final static Logger logger = Logger.getLogger(DbBackedObfuscatingIdFactoryProvider.class.getName());

   public final static String PROP_GRANT_SIZE = "grant_size";

   public final static String PROP_ENABLE_OBFUSCATION = "obfuscate";
   public final static String PROP_ALPHABET = "alphabet";
   public final static String PROP_BLOCK_SIZE = "block_size";
   public final static String PROP_MIN_LENGTH = "min_length";

   private final ConcurrentHashMap<String, IdGenerator> generators = new ConcurrentHashMap<>();

   private SqlExecutor exec;
   private IdObfuscator obfuscator;
   private int grantSize;

   public DbBackedObfuscatingIdFactoryProvider()
   {
      // TODO the default will eventually become true
   }

   public void setDatabaseExecutor(SqlExecutor exec)
   {
      this.exec = exec;
   }

   @SuppressWarnings("unchecked") // hoping the caller knows what they're doing.
   private <X> X getProperty(Map<String, Object> props, String prop, X defaultValue)
   {
      if (!props.containsKey(prop))
         return defaultValue;

      return (X)props.get(prop);
   }

   public void activate(Map<String, Object> props)
   {
      try
      {
         doActivation(props);
      }
      catch (Exception ex)
      {
         logger.log(Level.SEVERE, "Failed to activate id factory provider.", ex);
         throw ex;
      }
   }

   private void doActivation(Map<String, Object> props)
   {
      String INFO_OBFUSCATOR_CFG = "Initializing id obfuscator:"
            + "\n\tAlphabet: {0}"
            + "\n\tBlock Size: {1}"
            + "\n\tMin Length: {2}";

      logger.info("Activating obfucating id factory");
      Objects.requireNonNull(exec, "SqlExecutor not available.");

      grantSize = getProperty(props, PROP_GRANT_SIZE, Integer.valueOf(20)).intValue();

      boolean obfuscate = getProperty(props, PROP_ENABLE_OBFUSCATION, Boolean.FALSE).booleanValue();
      if (obfuscate)
      {
         String alphabet = getProperty(props, PROP_ALPHABET, IdObfuscator.ALPHABET);
         int blockSize = getProperty(props, PROP_BLOCK_SIZE, Integer.valueOf(IdObfuscator.BLOCK_SIZE)).intValue();
         int minLength = getProperty(props, PROP_MIN_LENGTH, Integer.valueOf(IdObfuscator.MIN_LENGTH)).intValue();

         logger.info(() -> format(INFO_OBFUSCATOR_CFG, alphabet, Integer.valueOf(blockSize), Integer.valueOf(minLength)));
         obfuscator = new IdObfuscator(alphabet, blockSize, minLength);
      }
   }

   public void dispose()
   {
      logger.info("Shuting down obfucating id factory");
      this.exec = null;
   }

   @Override
   public IdFactory getIdFactory(String context)
   {
      logger.fine(() -> format("Retriving IdFactory for context '{0}'", context));
      return () ->
      {
         if (!generators.contains(context))
         {
            generators.putIfAbsent(context, new IdGenerator(context));
         }

         long id = generators.get(context).next();
         String result = obfuscate(id);

         logger.fine(() -> format("Genereated id '{0}' for context '{1}'", Long.valueOf(id), context));
         return result;
      };
   }

   private String obfuscate(long id)
   {
      return obfuscator == null ? Long.toString(id) : obfuscator.encode(id);
   }

   private final class GrantProvider
   {
      private final String context;

      public GrantProvider(String ctx)
      {
         context = ctx;
      }

      public IdGrant requestIdGrant() // throws IdGrantCreationException
      {
         try
         {
            GetIdGrantTask task = new GetIdGrantTask(context, grantSize);
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
         grantProvider = new GrantProvider(context);
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
      @SuppressWarnings("unused")
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
