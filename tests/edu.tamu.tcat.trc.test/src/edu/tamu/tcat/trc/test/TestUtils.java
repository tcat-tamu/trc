package edu.tamu.tcat.trc.test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

import edu.tamu.tcat.db.core.DataSourceException;
import edu.tamu.tcat.db.postgresql.exec.PostgreSqlExecutor;
import edu.tamu.tcat.osgi.config.ConfigurationProperties;
import edu.tamu.tcat.osgi.config.file.SimpleFileConfigurationProperties;
import edu.tamu.tcat.sda.catalog.psql.provider.PsqlDataSourceProvider;
import edu.tamu.tcat.trc.repo.IdFactoryProvider;

public class TestUtils
{

   public static ClosableSqlExecutor initPostgreSqlExecutor(ConfigurationProperties config) throws DataSourceException
   {
      PsqlDataSourceProvider dsp = new PsqlDataSourceProvider();
      dsp.bind(config);
      dsp.activate();

      PostgreSqlExecutor exec = new PostgreSqlExecutor();
      exec.init(dsp);

      // decorate the executor to ensure that the data source provider is properly disposed
      return new ClosableSqlExecutor() {
         @Override
         public void close() throws Exception
         {
            exec.close();
            dsp.dispose();
         }

         @Override
         public <X> Future<X> submit(ExecutorTask<X> task)
         {
            return exec.submit(task);
         }
      };
   }

   public static ConfigurationProperties loadConfigFile()
   {
      Map<String, Object> params = new HashMap<>();
      params.put(SimpleFileConfigurationProperties.PROP_FILE, "config.path");
      SimpleFileConfigurationProperties config = new SimpleFileConfigurationProperties();
      config.activate(params);

      return config;
   }

   public static IdFactoryProvider makeIdFactoryProvider()
   {
      return new MockIdFactoryProvider();
   }
}