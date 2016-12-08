package edu.tamu.tcat.trc.impl.psql.dbutils;

import static java.text.MessageFormat.format;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.tamu.tcat.db.exec.sql.SqlExecutor;
import edu.tamu.tcat.trc.repo.RepositoryException;

/**
 * Utility class to perform basic database schema management and to test for the existence of
 * database tables.
 *
 * <p>At the moment, this is a simple implementation with minimal architecture and structure. It
 * is being developed for use in conjunction with PostgreSQL. Uses beyond this will require
 * significant code revisions and rethinking the API.
 */
public class DbTableManager
{
   // TODO rename to PsqlTableManager, build API
   private final static Logger logger = Logger.getLogger(DbTableManager.class.getName());

   private final SqlExecutor exec;

   public DbTableManager(SqlExecutor exec)
   {
      this.exec = exec;
   }

   /**
    * Represents column metadata retrieved from the DB.
    */
   static class ColumnMeta
   {
      public String name;
      public String type;
   }

   public void create(TableDefinition table)
   {
      String tablename = table.getName();
      unwrap(exec.submit(conn -> createTable(conn, tablename, getCreateSql(table))),
            () -> format("Failed to create table [{0}].", tablename));
   }

   /**
    * Checks to see if a table with the same name as the supplied table exists.
    * @param table The table definition to check.
    * @return <code>true</code> if a table with the same name as the supplied table exists.
    */
   public boolean exists(TableDefinition table)
   {
      String tablename = table.getName();
      return unwrap(exec.submit(conn -> tableExists(conn, tablename)),
                    () -> format("Failed to determin whether the table table {0} exists.", tablename));
   }

   public boolean isConsistent(TableDefinition table)
   {
      String tablename = table.getName();
      return unwrap(exec.submit(conn -> tableExists(conn, tablename) && checkColumnsMatch(conn, table)),
            () -> format("Failed to determine whether the table table {0} is consistent.", tablename));
   }

   public void truncate(TableDefinition table)
   {
      String tablename = table.getName();
      unwrap(exec.submit(conn -> dropOrTruncate(conn, "TRUNCATE TABLE ?", tablename)),
            () -> format("Failed to truncate table table {0}.", tablename));
   }

   public void drop(TableDefinition table)
   {
      String tablename = table.getName();
      unwrap(exec.submit(conn -> dropOrTruncate(conn, "DROP TABLE ?", tablename)),
            () -> format("Failed to drop table table {0}.", tablename));

   }

   private boolean dropOrTruncate(Connection conn, String sql, String tablename)
   {
      try (PreparedStatement stmt = conn.prepareStatement(sql))
      {
         stmt.setString(1, tablename);
         stmt.executeUpdate();
         return true;
      }
      catch (SQLException ex)
      {
         String msg = "Internal error attempting to truncate {0}: {1}";
         throw new IllegalStateException(format(msg, tablename, ex.getMessage()), ex);
      }
   }

   private Boolean createTable(Connection conn, String tablename, String sql) throws RepositoryException, SQLException
   {
      if (tableExists(conn, tablename))
         throw new RepositoryException(MessageFormat.format("A table with this name {0} already exists.", tablename));

      logger.log(Level.INFO, "Creating database tables for repository.\n" + sql);
      try (Statement stmt = conn.createStatement())
      {
         stmt.executeUpdate(sql);
      }

      return Boolean.TRUE;
   }
   private String getCreateSql(TableDefinition table)
   {
      String columsSql = buildColumnSql(table.getColumns());
      String constrainsSql = buildConstraints();
      return format("CREATE TABLE IF NOT EXISTS {0} ({1} {2}\n) ", table.getName(), columsSql, constrainsSql);
   }

   private String getCreateSql(ColumnDefinition column)
   {
      String cname = column.getName();
      ColumnDefinition.ColumnType type = column.getType();
      Integer size = column.getSize();
      String defaultValue = column.getDefault();


      StringBuilder sb = new StringBuilder();
      sb.append(format("\"{0}\" {1}", cname, type.type.toUpperCase()));

      if (type.sized && size != null)
         sb.append("(").append(size.intValue()).append(")");

      if (!column.allowNull())
         sb.append(" ").append("NOT NULL");

      if (defaultValue != null)
         sb.append(format(" DEFAULT {0}", defaultValue));

      return sb.toString();
   }

   private String buildColumnSql(List<ColumnDefinition> columns)
   {
      StringBuilder sb = new StringBuilder();
      boolean first = true;
      for (ColumnDefinition col : columns)
      {
         sb.append(first ? "\n    " : ",\n    ")
           .append(getCreateSql(col));

         first = false;
      }

      return sb.toString();
   }

   private String buildConstraints()
   {
      return "";
//      StringBuilder sb = new StringBuilder();
//
//      // TODO create PRIMARY KEY CONSTRAINT
//      sb.append(MessageFormat.format(",  CONSTRAINT {0}_pkey PRIMARY KEY ({1})", tablename, idField));
//
//
//      return sb.toString();
   }

   private boolean checkColumnsMatch(Connection conn, TableDefinition table) throws SQLException
   {
      // (20150814) Adapted from
      // http://stackoverflow.com/questions/4336259/query-the-schema-details-of-a-table-in-postgresql
      String sql = "SELECT"
                 +   " a.attname as column,"
                 +   " pg_catalog.format_type(a.atttypid, a.atttypmod) as datatype"
                 + "  FROM pg_catalog.pg_attribute a"
                 + " WHERE a.attnum > 0 AND NOT a.attisdropped"
                 + "   AND a.attrelid = ("
                        + "SELECT c.oid"
                        + "  FROM pg_catalog.pg_class c"
                        + "  LEFT JOIN pg_catalog.pg_namespace n ON n.oid = c.relnamespace"
                        + " WHERE c.relname = ?"
                        + "   AND pg_catalog.pg_table_is_visible(c.oid)"
                 +    ")";

      // NOTE for now, we'll just check that the columns exist and trust that if we have a
      //      table that matches the schema someone knew what they were doing. In theory,
      //      we should check column types to make sure everything works.

      String tablename = table.getName();
      Map<String, ColumnMeta> definedColumns = new HashMap<>();
      try (PreparedStatement stmt = conn.prepareStatement(sql))
      {
         stmt.setString(1, tablename);

         ResultSet rs = stmt.executeQuery();
         while (rs.next())
         {
            ColumnMeta def = new ColumnMeta();
            def.name = rs.getString("column");
            def.type = rs.getString("datatype");

            definedColumns.put(def.name, def);
         }
      }

      return table.getColumns().stream().allMatch(col -> matchColumType(definedColumns, col));
   }

   private boolean matchColumType(Map<String, ColumnMeta> definedColumns, ColumnDefinition column)
   {
      String colName = column.getName();
      String typeRegex = column.getType().regex;

      ColumnMeta columnDef = definedColumns.get(colName);
      return columnDef != null && columnDef.type.matches(typeRegex);
   }

   private static boolean tableExists(Connection conn, String tablename) throws SQLException
   {
      // (20150814) Adapted from
      // http://stackoverflow.com/questions/20582500/how-to-check-if-a-table-exists-in-a-given-schema
      String sql = "SELECT EXISTS ("
            + "SELECT 1 FROM pg_catalog.pg_class c"
            + "  JOIN pg_catalog.pg_namespace n ON n.oid = c.relnamespace"
            + " WHERE c.relname = ? AND c.relkind = 'r'"
            + ")";

      try (PreparedStatement stmt = conn.prepareStatement(sql))
      {
         stmt.setString(1, tablename);

         ResultSet rs = stmt.executeQuery();
         rs.next();
         return rs.getBoolean(1);
      }
   }

   /**
    * Convenience method to unwrap a {@link Future} and return the result or
    * package the underlying exception in a {@link RepositoryException}.
    *
    * @param result The future to be unwrapped.
    * @param message A message to supply if an exception was thrown by the future.
    *
    * @return The object returned by the future.
    *
    * @throws IllegalStateException If an {@link InterruptedException} or
    *       {@link TimeoutException} occurs while waiting on the supplied future.
    *       To avoid blocked threads, this will wait 10 minutes for the future to
    *       complete. For tasks that require longer execution, this method is not
    *       appropriate.
    * @throws RuntimeException If the future throws an {@link ExecutionException}
    *    whose cause is an unchecked exception, this method will propagate that
    *    exception directly.
    * @throws RepositoryException If the future throws an {@link ExecutionException}
    *    whose cause is a checked exception, this will wrap the checked exception in
    *    an unchecked {@link RepositoryException}.
    */
   public static <X> X unwrap(Future<X> result, Supplier<String> message)
      throws IllegalStateException, RepositoryException, RuntimeException
   {
      // HACK: ridiculously long timeout is better than nothing.
      try
      {
         return result.get(10, TimeUnit.SECONDS);
      }
      catch (InterruptedException | TimeoutException e)
      {
         String msg = message.get();
         throw new IllegalStateException(msg + " Failed to obtain result in a timely manner.", e);
      }
      catch (ExecutionException e)
      {
         Throwable cause = e.getCause();
         if (cause instanceof RuntimeException)
            throw (RuntimeException)cause;
         if (cause instanceof Error)
            throw (Error)cause;

         String msg = message.get();
         throw new RepositoryException(msg, e);
      }
   }

}
