package org.tamu.tcat.trc.persist.postgres;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.tamu.tcat.trc.persist.DocumentRepository;
import org.tamu.tcat.trc.persist.RepositoryDataStore;
import org.tamu.tcat.trc.persist.RepositoryException;
import org.tamu.tcat.trc.persist.RepositorySchema;

import com.google.common.util.concurrent.Futures;

import edu.tamu.tcat.db.exec.sql.SqlExecutor;

public class PsqlJsonStore implements RepositoryDataStore<String>
{
   private static final Logger logger = Logger.getLogger(PsqlJsonStore.class.getName());

   private static final String ERR_NO_SUCH_SCHEMA = "Cannot register repository [{0}]. The supplied schema [{1}]does not exist on this data store. ";
   private static final String ERR_NOT_REGISTERED = "No repository has been registerd for id '{0}'";
   private static final String ERR_ALREADY_REGISTERED = "A repository has already been registered for the id '{0}'";

   private SqlExecutor exec;
   private final ConcurrentHashMap<String, PsqlJsonRepo<?>> repos = new ConcurrentHashMap<>();

   public PsqlJsonStore()
   {
   }

   public void setDbExecutor(SqlExecutor exec)
   {
      this.exec = exec;
   }

   // intended to be called by DS
   public void activate()
   {
      Objects.requireNonNull(exec, "No database executor service has been supplied.");
   }

   // intended to be called by DS
   public void dispose()
   {
      repos.clear();
   }

   @Override
   public boolean exists(RepositorySchema schema) throws RepositoryException
   {
      Future<Boolean> result = exec.submit(conn -> {
         return Boolean.valueOf(tableExists(schema, conn) && checkColumnsMatch(schema, conn));
      });

      return Futures.get(result, RepositoryException.class);
   }

   private boolean checkColumnsMatch(RepositorySchema schema, Connection conn) throws SQLException
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

      Map<String, ColumnDef> definedColumns = new HashMap<>();
      try (PreparedStatement stmt = conn.prepareStatement(sql))
      {
         stmt.setString(1, schema.getName());

         ResultSet rs = stmt.executeQuery();
         while (rs.next())
         {
            ColumnDef def = new ColumnDef();
            def.name = rs.getString("column");
            def.type = rs.getString("datatype");

            definedColumns.put(def.name, def);
         }
      }

      return matchColumType(definedColumns, schema.getIdField(), "^character")
          && matchColumType(definedColumns, schema.getDataField(), "^json")
          && matchColumType(definedColumns, schema.getCreatedField(), "^timestamp")
          && matchColumType(definedColumns, schema.getModifiedField(), "^timestamp")
          && matchColumType(definedColumns, schema.getRemovedField(), "boolean");

   }

   private boolean matchColumType(Map<String, ColumnDef> definedColumns, String fname, String regex)
   {
      if (fname == null)
         return true;      // this column is not used - does not matter if it is in the table

      ColumnDef columnDef = definedColumns.get(fname);
      return columnDef != null && columnDef.type.matches(regex);
   }


   private boolean tableExists(RepositorySchema schema, Connection conn) throws SQLException
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
         stmt.setString(1, schema.getName());

         ResultSet rs = stmt.executeQuery();
         rs.next();
         return Boolean.valueOf(rs.getBoolean(1));
      }
   }

   @Override
   public boolean create(RepositorySchema schema) throws RepositoryException
   {
      String sql = buildCreateSql(schema);

      if (exists(schema))
         return false;

      Future<Boolean> result = exec.submit((conn) -> createTable(conn, schema, sql));
      return Futures.get(result, RepositoryException.class);
   }

   // TODO truncate, drop?

   private String buildCreateSql(RepositorySchema schema)
   {
      String tableName = schema.getName();
      String idField = schema.getIdField();
      String dataField = schema.getDataField();

      Objects.requireNonNull(tableName, "A table name must be supplied.");
      Objects.requireNonNull(idField, "An id field name must be supplied.");
      Objects.requireNonNull(dataField, "A data field name must be supplied.");

      StringBuilder sb = new StringBuilder();
      sb.append(MessageFormat.format("CREATE TABLE {0} (", tableName));
      sb.append(MessageFormat.format("{0} VARCHAR(255) NOT NULL, ", idField));
      sb.append(MessageFormat.format("{0} JSON", dataField));

      // optional fields
      String removedField = schema.getRemovedField();
      String createdField = schema.getCreatedField();
      String modifiedField = schema.getModifiedField();

      if (removedField != null)
         sb.append(MessageFormat.format(",  {0} TIMESTAMP DEFAULT NULL", removedField));
      if (createdField != null)
         sb.append(MessageFormat.format(",  {0} TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP", createdField));
      if (modifiedField != null)
         sb.append(MessageFormat.format(",  {0} TIMESTAMP", modifiedField));

      // primary key
      sb.append(MessageFormat.format(",  CONSTRAINT {0}_pkey PRIMARY KEY ({1})", tableName, idField));
      sb.append(")");

      return sb.toString();
   }

   private Boolean createTable(Connection conn, RepositorySchema schema, String sql) throws RepositoryException, SQLException
   {
      String tableName = schema.getName();
      if (tableExists(schema, conn))
         throw new RepositoryException(MessageFormat.format("A table with this name {0} already exists.", tableName));

      logger.log(Level.INFO, "Creating database tables for repository.\n" + sql);
      try (Statement stmt = conn.createStatement())
      {
         stmt.executeUpdate(sql);
      }

      return Boolean.valueOf(true);
   }

   @Override
   public boolean isRegistered(String repoId)
   {
      return repos.containsKey(repoId);
   }

   @Override
   public Set<String> listRepositories()
   {
      return repos.keySet();
   }

   @Override
   public <X> String registerRepository(RepositoryConfiguration<String, X> config) throws RepositoryException
   {
      String repoId = config.getId();
      RepositorySchema schema = config.getSchema();
      if (!this.exists(schema))
      {
         throw new RepositoryException(
               MessageFormat.format(ERR_NO_SUCH_SCHEMA, repoId, schema.getName()));
      }

      PsqlJsonRepo<X> repo = new PsqlJsonRepo<>(config, exec);
      if (repo != repos.putIfAbsent(repoId, repo))
         throw new RepositoryException(
               MessageFormat.format(ERR_ALREADY_REGISTERED, repoId));

      return config.getId();
   }

   @Override
   @SuppressWarnings("unchecked") // type safety assumed by caller
   public <X> DocumentRepository<String, X> get(String schemaId, Class<X> type) throws RepositoryException
   {
      PsqlJsonRepo<?> repo = repos.get(schemaId);
      if (repo == null)
         throw new RepositoryException(MessageFormat.format(ERR_NOT_REGISTERED, schemaId));

      return (DocumentRepository<String, X>)repo;
   }

   private static class ColumnDef
   {
      public String name;
      public String type;
   }

}
