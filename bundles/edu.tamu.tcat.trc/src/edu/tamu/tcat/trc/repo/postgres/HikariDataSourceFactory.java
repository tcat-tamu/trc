package edu.tamu.tcat.trc.repo.postgres;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;

import org.postgresql.ds.PGSimpleDataSource;

import com.zaxxer.hikari.HikariDataSource;

import edu.tamu.tcat.db.core.DataSourceException;
import edu.tamu.tcat.db.postgresql.PostgreSqlDbcp2DataSourceFactory;
import edu.tamu.tcat.db.postgresql.PostgreSqlPropertiesBuilder;
import edu.tamu.tcat.osgi.config.ConfigurationProperties;

/**
 * @since 3.3
 */
public class HikariDataSourceFactory
{
   // An app id that can be passed to the db during connections for logging
   private static final String KEY_APP_DB_ID = "edu.tamu.tcat.trc.connpool.appid";

   // The Properties key is ensured immutable to make it a safe key
   private Map<Properties, HikariDataSource> dataSources = new HashMap<>();

   private ConfigurationProperties config;

   public void bind(ConfigurationProperties props)
   {
      this.config = props;
   }

   public PostgreSqlPropertiesBuilder getPropertiesBuilder()
   {
      // HACK: get properties builder from somewhere
      return new PostgreSqlDbcp2DataSourceFactory().getPropertiesBuilder();
   }


   /**
    * Get the connection url specifying the database driver, host/file, and database name
    */
   protected String getConnectionUrl(Properties parameters) throws DataSourceException
   {
      return getConnectionUrl(parameters.getProperty(PostgreSqlPropertiesBuilder.HOST),
                              PostgreSqlPropertiesBuilder.getPort(parameters),
                              parameters.getProperty(PostgreSqlPropertiesBuilder.DATABASE));
   }

   /**
    * Get the connection properties for opening the connection<br>
    * This should include user, and password.  It should not include database or host name
    */
   protected Properties getConnectionProperties(Properties parameters)
   {
      Properties prop = new Properties();
      prop.putAll(parameters);
      // Remove properties used to pass into the PostgreSqlPropertiesBuilder that do not belong in the set sent to PostgreSQL
      prop.remove(PostgreSqlPropertiesBuilder.HOST);
      prop.remove(PostgreSqlPropertiesBuilder.DATABASE);
      prop.remove(PostgreSqlPropertiesBuilder.PORT);

      return prop;
   }

   protected String getConnectionUrl(String server, int port, String database) throws DataSourceException
   {
      StringBuilder sb = new StringBuilder();
      sb.append("jdbc:postgresql://");

      if (server == null)
      {
         throw new DataSourceException("Could not construct database URL. No host specified");
      }
      sb.append(server);

      if (port >= 0)
      {
         sb.append(":").append(port);
      }

      if (database == null)
      {
         throw new DataSourceException("Could not construct database URL. No database specified");
      }

      sb.append("/").append(database);
      return sb.toString();
   }

   /**
    * Create a new {@link BasicDataSource} from the specified {@link Properties}.
    *
    * <p>Valid properties include:</p>
    * <dl>
    *   <dt>hikari.maxpoolsize</dt>
    *     <dd>This property controls the maximum size that the pool is allowed to reach,
    *     including both idle and in-use connections. Basically this value will determine the
    *     maximum number of actual connections to the database backend. A reasonable value for
    *     this is best determined by your execution environment. When the pool reaches this
    *     size, and no idle connections are available, calls to getConnection() will block for
    *     up to {@code connectionTimeout} milliseconds before timing out. Default: 10</dd>
    *   <dt>hikari.connectiontimeout</dt>
    *     <dd>This property controls the maximum number of milliseconds that a client will
    *     wait for a connection from the pool. If this time is exceeded without a connection
    *     becoming available, a SQLException will be thrown. 1000ms is the minimum value.
    *     Default: 30000 (30 seconds)</dd>
    *   <dt>hikari.idletimeout</dt>
    *     <dd>This property controls the maximum amount of time that a connection is allowed
    *     to sit idle in the pool. Whether a connection is retired as idle or not is subject
    *     to a maximum variation of +30 seconds, and average variation of +15 seconds. A
    *     connection will never be retired as idle before this timeout. A value of 0 means
    *     that idle connections are never removed from the pool. Default: 600000 (10 minutes)</dd>
    *   <dt>hikari.minidle</dt>
    *     <dd>This property controls the minimum number of idle connections that HikariCP
    *     tries to maintain in the pool. If the idle connections dip below this value, HikariCP
    *     will make a best effort to add additional connections quickly and efficiently.
    *     However, for maximum performance and responsiveness to spike demands, we recommend
    *     not setting this value and instead allowing HikariCP to act as a fixed size
    *     connection pool. Default: same as {@code maximumPoolSize}</dd>
    * </dl>
    */
   protected synchronized HikariDataSource createDataSource(final Properties parameters) throws DataSourceException
   {
      // For additional configuration variables that could be applied see:
      // https://github.com/brettwooldridge/HikariCP#configuration-knobs-baby

      final String connectionUrl = getConnectionUrl(parameters);

      PGSimpleDataSource internalDS = new PGSimpleDataSource();
      try
      {
         internalDS.setUrl(connectionUrl);
         internalDS.setUser(parameters.getProperty(PostgreSqlPropertiesBuilder.USER));
         internalDS.setPassword(parameters.getProperty(PostgreSqlPropertiesBuilder.PASSWORD));

         String dbid = config.getPropertyValue(KEY_APP_DB_ID, String.class);
         if (dbid != null)
            internalDS.setApplicationName(dbid);
      }
      catch (Exception e)
      {
         throw new DataSourceException("Failed creating internal PG data source", e);
      }

      int maxPoolSize = Integer.parseInt(parameters.getProperty("hikari.maxpoolsize", "10")); // default = 10
      long connTimeout = Long.parseLong(parameters.getProperty("hikari.connectiontimeout", "30000")); // default = 30s
      long idleTimeout = Long.parseLong(parameters.getProperty("hikari.idletimeout", "600000")); // default = 10 minutes
      int minIdle = Integer.parseInt(parameters.getProperty("hikari.minidle", String.valueOf(maxPoolSize))); // default = max pool size

      HikariDataSource dataSource = new HikariDataSource() {
         @Override
         public String toString() {
            return super.toString() + " @ " + connectionUrl;
         };
      };
      dataSource.setDataSource(internalDS);
      dataSource.setIdleTimeout(idleTimeout);
      dataSource.setMaximumPoolSize(maxPoolSize);
      dataSource.setMinimumIdle(minIdle);
      dataSource.setConnectionTimeout(connTimeout);
      return dataSource;
   }

   // expose as javax.sql.DataSource - if exposed as org.apache.commons.dbcp.BasicDataSource, clients
   // of this class must import that package to link to any factory subclass
   public DataSource getDataSource(Properties parameters) throws DataSourceException
   {
      HikariDataSource dataSource = dataSources.get(parameters);
      if (dataSource == null)
      {
         Properties nonMutating = new Properties();
         nonMutating.putAll(parameters);
         dataSource = createDataSource(nonMutating);
         dataSources.put(nonMutating, dataSource);
      }
      return dataSource;
   }

   /**
    * Close all datasources<br>
    * If the provider is a service, this should be invoked on service deregistration
    * */
   public void shutdown() throws DataSourceException
   {
      DataSourceException exception = new DataSourceException("Error closing " + getClass().getName() + "datasources");
      for (HikariDataSource ds : dataSources.values())
      {
         try
         {
            ds.close();
         }
         catch (Exception e)
         {
            exception.addSuppressed(exception);
         }
      }
      if (exception.getSuppressed().length != 0)
         throw exception;
   }
}
