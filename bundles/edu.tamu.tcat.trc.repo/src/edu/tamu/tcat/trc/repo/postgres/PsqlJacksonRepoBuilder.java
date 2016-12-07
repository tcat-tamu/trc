package edu.tamu.tcat.trc.repo.postgres;

import static edu.tamu.tcat.trc.repo.DocumentRepository.unwrap;
import static java.text.MessageFormat.format;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.tamu.tcat.db.exec.sql.SqlExecutor;
import edu.tamu.tcat.trc.repo.DocRepoBuilder;
import edu.tamu.tcat.trc.repo.DocumentRepository;
import edu.tamu.tcat.trc.repo.EditCommandFactory;
import edu.tamu.tcat.trc.repo.RepositoryException;

/**
 *  Constructs {@link DocumentRepository} instances that are connected to a PostgreSQL database
 *  and store their data as JSON. Framework clients are expected to instantiate a builder
 *  directly to configure and build a repository instance. Once built, the returned repository
 *  will be connected to the database and the builder should be disposed.
 *
 *  @implNote
 *  Uses Jackson internally to convert POJO data transfer objects into serialized JSON for
 *  for persistence. Instances of {@code StorageType} must be serializable using the default
 *  Jackson {@link ObjectMapper}. Please consult the Jackson documentation
 *  {@link https://github.com/FasterXML/jackson }for additional  detail.
 *
 *  <p>
 *  The {@link DocumentRepository} implementation returned by this factory caches results in
 *  order to improve performance. Consequently, it is critical that applications close the
 *  returned repository once it is no longer needed.
 *
 * @param <RecordType> The type of records to be provided by the created document repository.
 * @param <EditCmdType> The type of the edit command returned by the the created repository
 *       for use in updating records.
 * @param <StorageType> The internal data storage type. This type is not visible through the
 *       repository API. It is used by the {@link EditCommandFactory} and the data adapter
 *       function for data editing and transformation.
 */
public class PsqlJacksonRepoBuilder<RecordType, StorageType, EditCmdType> implements DocRepoBuilder<RecordType, StorageType, EditCmdType>
{
   private final static String CREATE_SQL =
         "CREATE TABLE {0} ("
               + "  id VARCHAR(255) NOT NULL, "
               + "  data JSON,"
               + "  removed TIMESTAMP DEFAULT NULL,"
               + "  date_created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,"
               + "  last_modified TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,"
               + "  CONSTRAINT {1}_pkey PRIMARY KEY (id)"
               + ")";

   private static final Logger logger = Logger.getLogger(PsqlJacksonRepoBuilder.class.getName());

   private SqlExecutor exec;
   private boolean enableCreation = false;

   private String tablename;
   private Function<StorageType, RecordType> adapter;
   private EditCommandFactory<StorageType, EditCmdType> cmdFactory;
   private Class<StorageType> storageType;

   public PsqlJacksonRepoBuilder()
   {
   }

   @Override
   public PsqlJacksonRepoBuilder<RecordType, StorageType, EditCmdType> setEnableCreation(boolean value)
   {
      this.enableCreation = value;
      return this;
   }

   /**
    * @param exec The database executor to use.
    */
   public PsqlJacksonRepoBuilder<RecordType, StorageType, EditCmdType> setDbExecutor(SqlExecutor exec)
   {
      this.exec = exec;
      return this;
   }

   @Override
   public PsqlJacksonRepoBuilder<RecordType, StorageType, EditCmdType> setTableName(String tablename)
   {
      this.tablename = tablename;
      return this;
   }

   @Override
   public PsqlJacksonRepoBuilder<RecordType, StorageType, EditCmdType> setStorageType(Class<StorageType> type)
   {
      this.storageType = type;
      return this;
   }

   @Override
   public PsqlJacksonRepoBuilder<RecordType, StorageType, EditCmdType> setDataAdapter(Function<StorageType, RecordType> adapter)
   {
      this.adapter = adapter;
      return this;
   }

   @Override
   public PsqlJacksonRepoBuilder<RecordType, StorageType, EditCmdType> setEditCommandFactory(EditCommandFactory<StorageType, EditCmdType> cmdFactory)
   {
      this.cmdFactory = cmdFactory;
      return this;
   }

   @Override
   public PsqlJacksonRepo<RecordType, StorageType, EditCmdType> build() throws RepositoryException
   {
      if (!this.exists() && enableCreation)
         this.create();

      PsqlJacksonRepo<RecordType, StorageType, EditCmdType> repo = new PsqlJacksonRepo<>();
      repo.setSqlExecutor(exec);
      repo.setTableName(tablename);
      repo.setCommandFactory(cmdFactory);
      repo.setAdapter(adapter);
      repo.setStorageType(storageType);

      repo.activate();

      return repo;
   }

   private boolean exists() throws RepositoryException
   {
      Future<Boolean> result = exec.submit(conn -> {
         return Boolean.valueOf(tableExists(conn, tablename) && checkColumnsMatch(conn));
      });

      return unwrap(result, () -> format("Failed to determine whether table {0} exists.", tablename)).booleanValue();
   }

   private boolean checkColumnsMatch(Connection conn) throws SQLException
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
         stmt.setString(1, tablename);

         ResultSet rs = stmt.executeQuery();
         while (rs.next())
         {
            ColumnDef def = new ColumnDef();
            def.name = rs.getString("column");
            def.type = rs.getString("datatype");

            definedColumns.put(def.name, def);
         }
      }

      return  matchColumType(definedColumns, "id", "^char.+")
              && matchColumType(definedColumns, "data", "^json")
              && matchColumType(definedColumns, "date_created", "^time.+")
              && matchColumType(definedColumns, "last_modified", "^time.+")
              &&  matchColumType(definedColumns, "removed", "^time.+");

   }

   private boolean matchColumType(Map<String, ColumnDef> definedColumns, String fname, String regex)
   {
      if (fname == null)
         return true;      // this column is not used - does not matter if it is in the table

      ColumnDef columnDef = definedColumns.get(fname);
      return columnDef != null && columnDef.type.matches(regex);
   }

   private static class ColumnDef
   {
      public String name;
      public String type;
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

   private boolean create() throws RepositoryException
   {
      if (exists())
         return false;

      String sql = format(CREATE_SQL, tablename, tablename);
      Future<Boolean> result = exec.submit((conn) -> createTable(conn, sql));
      return unwrap(result, () -> format("Failed to create database table\n{0}", sql)).booleanValue();
   }

   // TODO truncate, drop?

   private Boolean createTable(Connection conn, String sql) throws RepositoryException, SQLException
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

}
