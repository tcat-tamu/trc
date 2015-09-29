package edu.tamu.tcat.trc.test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import edu.tamu.tcat.db.core.DataSourceException;
import edu.tamu.tcat.db.exec.sql.SqlExecutor;
import edu.tamu.tcat.db.postgresql.exec.PostgreSqlExecutor;
import edu.tamu.tcat.osgi.config.ConfigurationProperties;
import edu.tamu.tcat.osgi.config.file.SimpleFileConfigurationProperties;
import edu.tamu.tcat.trc.repo.IdFactory;
import edu.tamu.tcat.trc.repo.postgres.PsqlDataSourceProvider;

public class TestUtils
{

   public static SqlExecutor initPostgreSqlExecutor(ConfigurationProperties config) throws DataSourceException
   {
      PsqlDataSourceProvider dsp = new PsqlDataSourceProvider();
      dsp.bind(config);
      dsp.activate();

      PostgreSqlExecutor exec = new PostgreSqlExecutor();
      exec.init(dsp);

      // decorate the executor to ensure that the data source provider is properly disposed
      return new SqlExecutor() {
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

   public static IdFactory makeIdFactory()
   {
      return new IdFactoryImpl();
   }

   /**
    * Simple, thread-safe, in memory id factory.
    */
   private static final class IdFactoryImpl implements IdFactory
   {
      ConcurrentHashMap<String, AtomicInteger> counters = new ConcurrentHashMap<>();

      @Override
      public String getNextId(String context)
      {
         if (!counters.containsKey(context))
            counters.putIfAbsent(context, new AtomicInteger());

         int id = counters.get(context).incrementAndGet();
         return Integer.toString(id);
      }

   }
}