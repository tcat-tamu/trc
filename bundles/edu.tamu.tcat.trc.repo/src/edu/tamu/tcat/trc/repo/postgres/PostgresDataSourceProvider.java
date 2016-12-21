package edu.tamu.tcat.trc.repo.postgres;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

import edu.tamu.tcat.db.postgresql.PostgreSqlEntityHelper;
import edu.tamu.tcat.db.postgresql.PostgreSqlPropertiesBuilder;
import edu.tamu.tcat.db.provider.DataSourceProvider;
import edu.tamu.tcat.osgi.config.ConfigurationProperties;

/**
 * A {@link DataSourceProvider} for use by the core data table engine.
 * @since 3.0
 */
public class PostgresDataSourceProvider implements DataSourceProvider
{
   // FIXME move to either SDA or TRC DBUtils
   // private static final Logger debug = Logger.getLogger(EngineDataSourceProvider.class.getName());

   //NOTE: No need for a "schema" parameter since the data source is for the entire db, not for a schema or table
   private static final String PROP_HOST = "edu.tamu.tcat.trc.repo.postgresql.host";
   private static final String PROP_DB = "edu.tamu.tcat.trc.repo.postgresql.database";
   private static final String PROP_PORT = "edu.tamu.tcat.trc.repo.postgresql.port";
   private static final String PROP_USER = "edu.tamu.tcat.trc.repo.postgresql.user";
   private static final String PROP_PASS = "edu.tamu.tcat.trc.repo.postgresql.password";
   private static final String PROP_SSL = "edu.tamu.tcat.trc.repo.postgresql.ssl";

   private ConfigurationProperties config;
   private DataSource dataSource;

   // provided as cause if no datasource is available when getDataSource is called.
   private Exception err;
   private Properties propsUnsafe;
   private Properties props;

   private HikariDataSourceFactory factory;

   public void bind(ConfigurationProperties cp)
   {
      this.config = cp;
      this.factory = new HikariDataSourceFactory(config);
   }

   @Override
   public String toString()
   {
      return props == null ? "uninitialized" : props.toString();
   }

   public void activate()
   {
      // TODO may need to push into background thread
      try
      {
         String host = config.getPropertyValue(PROP_HOST, String.class);
         int port = config.getPropertyValue(PROP_PORT, Integer.class, Integer.valueOf(5432)).intValue();
         if (port <= 0) {
            port = 5432;
         }

         String db = config.getPropertyValue(PROP_DB, String.class);
         String user = config.getPropertyValue(PROP_USER, String.class);
         String password = config.getPropertyValue(PROP_PASS, String.class);
         boolean ssl = config.getPropertyValue(PROP_SSL, Boolean.class, Boolean.FALSE).booleanValue();

         createDatabaseIfNotExists(host, port, user, password, db, ssl);


         PostgreSqlPropertiesBuilder builder =
               factory.getPropertiesBuilder().create(host, port, db, user, password);
         builder.setUseSsl(ssl);

         // Clone since the factory might retain this instance
         propsUnsafe = (Properties)builder.getProperties().clone();
         propsUnsafe.setProperty("hikari.maxpoolsize", "50");
         propsUnsafe.setProperty("hikari.minidle", "1");
         dataSource = factory.getDataSource(propsUnsafe);

         builder.setPassword("");
         // Store the properties locally to use for toString, but remove the password from logging
         props = builder.getProperties();
      }
      catch (Exception e)
      {
         err = e;
         throw new IllegalStateException(e);
      }
   }

   public void dispose()
   {

   }

   private void createDatabaseIfNotExists(String host, int port, String user, String password, String targetDatabase, boolean ssl) throws Exception
   {
      // creating this extra temporary connection properties instance,
      // because it is necessary to connect to the default ("") database
      // in order to check if the target database exists
      PostgreSqlPropertiesBuilder tmpProps = factory.getPropertiesBuilder().create(host, port, "", user, password);
      tmpProps.setUseSsl(ssl);

      //TODO: this should probably just bypass getting a pooled connection and use a raw pg data source connection
      Properties propsCreateDb = tmpProps.getProperties();
      propsCreateDb = (Properties)propsCreateDb.clone();
      propsCreateDb.setProperty("hikari.minidle", "0");              // allow these connections to be cleaned up after app startup: retain zero idle
      propsCreateDb.setProperty("hikari.idletimeout", "10000");      // short (10s) timeout since they are only needed to check the db
      DataSource tmpDataSource = factory.getDataSource(propsCreateDb);

      boolean created = false;
      try (Connection conn = tmpDataSource.getConnection())
      {
         created = PostgreSqlEntityHelper.createDatabase(conn, targetDatabase);
      }

      if (created)
      {
         // connecting to the newly created database here, in order to add postgis extensions
         tmpProps.setDatabase(targetDatabase);
         Properties propsPostGis = tmpProps.getProperties();
         propsPostGis.setProperty("hikari.minidle", "0");
         propsPostGis.setProperty("hikari.idletimeout", "10000"); // 10 s
         tmpDataSource = factory.getDataSource(propsPostGis);

//         try (Connection conn = tmpDataSource.getConnection())
//         {
//            PostgreSqlEntityHelper.createExtensionPostGis(conn);
//         }
      }
   }

   public Properties getDataSourceConfig()
   {
      return (Properties)propsUnsafe.clone();
   }

   @Override
   public DataSource getDataSource()
   {
      if (dataSource == null)
         // HACK: wrap in a runtime exception until IUserAuthenticationDataProvider does not provide a conflicting API
         throw new IllegalStateException(new SQLException("Failed initializing data source " + this, err));

      return dataSource;
   }
}
