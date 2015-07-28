package edu.tamu.tcat.trc.entries.types.article.test.internal;

import java.sql.SQLException;
import java.util.Objects;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;

import edu.tamu.tcat.db.postgresql.PostgreSqlDataSourceFactory;
import edu.tamu.tcat.db.postgresql.PostgreSqlPropertiesBuilder;
import edu.tamu.tcat.db.provider.DataSourceProvider;
import edu.tamu.tcat.osgi.config.ConfigurationProperties;

public class PsqlDataSourceProvider implements DataSourceProvider
{
   // HACK This should not be here. Surely there is a PsqlDataSourceProvider in our db utilities?

   public static final Logger DB_LOGGER = Logger.getLogger("edu.tamu.tcat.oss.db.hsqldb");

   public static final String PROP_URL = "db.postgres.url";
   public static final String PROP_USER = "db.postgres.user";
   public static final String PROP_PASS = "db.postgres.pass";

   public static final String PROP_MAX_ACTIVE = "db.postgres.active.max";
   public static final String PROP_MAX_IDLE = "db.postgres.idle.max";
   public static final String PROP_MIN_IDLE = "db.postgres.idle.min";
   public static final String PROP_MIN_EVICTION = "db.postgres.eviction.min";
   public static final String PROP_BETWEEN_EVICTION = "db.postgres.eviction.between";

   private DataSource dataSource;
   private ConfigurationProperties props;

   public void bind(ConfigurationProperties cp)
   {
      this.props = cp;
   }

   private static int getIntValue(ConfigurationProperties props, String prop, int defaultValue)
   {
      Integer d = Integer.valueOf(defaultValue);
      Integer result = props.getPropertyValue(prop, Integer.class, d);

      return result.intValue();
   }

   // called by OSGi DS
   public void activate()
   {
      try
      {

         String url = props.getPropertyValue(PROP_URL, String.class);
         String user = props.getPropertyValue(PROP_USER, String.class);
         String pass = props.getPropertyValue(PROP_PASS, String.class);

         Objects.requireNonNull(url, "Database connection URL not supplied");
         Objects.requireNonNull(user, "Database username not supplied");
         Objects.requireNonNull(pass, "Database password not supplied");

         int maxActive = getIntValue(props, PROP_MAX_ACTIVE, 30);
         int maxIdle = getIntValue(props, PROP_MAX_IDLE, 3);
         int minIdle = getIntValue(props, PROP_MIN_IDLE, 0);
         int minEviction = getIntValue(props, PROP_MIN_EVICTION, 10 * 1000);
         int betweenEviction = getIntValue(props, PROP_BETWEEN_EVICTION, 100);

         PostgreSqlDataSourceFactory factory = new PostgreSqlDataSourceFactory();
         PostgreSqlPropertiesBuilder builder = factory.getPropertiesBuilder().create(url, user, pass);
         dataSource = factory.getDataSource(builder.getProperties());

         //HACK: should add this API to the properties builder instead of downcasting and overriding
         {
            BasicDataSource basic = (BasicDataSource)dataSource;

            basic.setMaxActive(maxActive);
            basic.setMaxIdle(maxIdle);
            basic.setMinIdle(minIdle);
            basic.setMinEvictableIdleTimeMillis(minEviction);
            basic.setTimeBetweenEvictionRunsMillis(betweenEviction);
         }
      }
      catch (Exception e)
      {
         throw new IllegalStateException("Failed initializing data source", e);
      }
   }

   public void dispose()
   {
   }

   @Override
   public DataSource getDataSource() throws SQLException
   {
      return dataSource;
   }
}
