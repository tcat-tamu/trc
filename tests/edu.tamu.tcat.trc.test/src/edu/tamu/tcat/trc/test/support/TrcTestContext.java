package edu.tamu.tcat.trc.test.support;

import static org.junit.Assert.assertFalse;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import edu.tamu.tcat.account.Account;
import edu.tamu.tcat.account.login.LoginData;
import edu.tamu.tcat.account.store.AccountStore;
import edu.tamu.tcat.db.exec.sql.SqlExecutor;
import edu.tamu.tcat.db.postgresql.exec.PostgreSqlExecutor;
import edu.tamu.tcat.osgi.config.ConfigurationProperties;
import edu.tamu.tcat.osgi.config.file.SimpleFileConfigurationProperties;
import edu.tamu.tcat.trc.TrcApplication;
import edu.tamu.tcat.trc.auth.account.TrcAccount;
import edu.tamu.tcat.trc.auth.account.TrcSystemAccount;
import edu.tamu.tcat.trc.entries.core.repo.EntryRepositoryRegistrar;
import edu.tamu.tcat.trc.impl.psql.TrcApplicationImpl;
import edu.tamu.tcat.trc.impl.psql.entries.DbEntryRepositoryRegistry;
import edu.tamu.tcat.trc.impl.psql.services.TrcSvcMgrImpl;
import edu.tamu.tcat.trc.repo.id.IdFactoryProvider;
import edu.tamu.tcat.trc.repo.postgres.PostgresDataSourceProvider;
import edu.tamu.tcat.trc.resolver.EntryResolverRegistry;
import edu.tamu.tcat.trc.search.solr.BasicSearchSvcMgr;

/**
 *  Initializes the core application components required for a TRC application in a
 *  manner suitable for unit tests. In general, unit tests should create a
 *  {@link TrcTestContext} in their <code>beforeClass</code> phase and destroy it
 *  in their <code>afterClass</code> phase.
 */
public class TrcTestContext implements AutoCloseable
{
   public static final TrcAccount TEST_ACCOUNT = new TrcSystemAccount("Test User", "internal.system.test", 1001);

   private final TrcApplicationImpl ctx;

   private final IdFactoryProvider idProvider = new MockIdFactoryProvider();
   private final ConfigurationProperties config = initConfigFile();
   private final ClosableSqlExecutor sqlExecutor = initPostgreSqlExecutor(config);
   private final DbEntryRepositoryRegistry registrar = initRepoRegistry(idProvider, config, sqlExecutor);
   private final TrcSvcMgrImpl svcMgr = initServiceManager(sqlExecutor, registrar);
   private final BasicSearchSvcMgr searchMgr = initSearchManager(config);

   public TrcTestContext()
   {
      ctx = new TrcApplicationImpl();
      ctx.setIdFactory(idProvider);
      ctx.setConfiguration(config);
      ctx.setEntryRepoRegistry(registrar);
      ctx.setServiceManager(svcMgr);
      ctx.setSearchManager(searchMgr);

      ctx.activate();
   }

   @Override
   public void close() throws Exception
   {
      sqlExecutor.close();
      registrar.dispose();
      svcMgr.dispose();
      searchMgr.close();
      ctx.deactivate();
   }

   private static BasicSearchSvcMgr initSearchManager(ConfigurationProperties config)
   {
      BasicSearchSvcMgr searchMgr = new BasicSearchSvcMgr();
      searchMgr.setConfigurationProperties(config);
      searchMgr.activate();
      return searchMgr;
   }

   private static TrcSvcMgrImpl initServiceManager(ClosableSqlExecutor sqlExecutor, DbEntryRepositoryRegistry repos)
   {
      TrcSvcMgrImpl svcMgr = new TrcSvcMgrImpl();
      svcMgr.bind(new AccountStoreImpl());
      svcMgr.bind(repos);
      svcMgr.bind(sqlExecutor);
      svcMgr.activate();
      return svcMgr;
   }

   private static DbEntryRepositoryRegistry initRepoRegistry(IdFactoryProvider idProvider, ConfigurationProperties config, ClosableSqlExecutor sqlExecutor)
   {
      DbEntryRepositoryRegistry repos = new DbEntryRepositoryRegistry();
      repos.setConfiguration(config);
      repos.setIdFactory(idProvider);
      repos.setSqlExecutor(sqlExecutor);
      repos.activate();
      return repos;
   }

   private static ClosableSqlExecutor initPostgreSqlExecutor(ConfigurationProperties config)
   {
      try
      {
         PostgresDataSourceProvider dsp = new PostgresDataSourceProvider();
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
            public <X> CompletableFuture<X> submit(ExecutorTask<X> task)
            {
               return exec.submit(task);
            }
         };
      }
      catch (Exception ex)
      {
         assertFalse("Failed to spin up Unit Test framework support.", true);
         throw new IllegalStateException(ex);
      }
   }

   private static ConfigurationProperties initConfigFile()
   {
      Map<String, Object> params = new HashMap<>();
      params.put(SimpleFileConfigurationProperties.PROP_FILE, "config.path");
      SimpleFileConfigurationProperties config = new SimpleFileConfigurationProperties();
      config.activate(params);

      return config;
   }

   public TrcApplication getApplicationContext()
   {
      return ctx;
   }

   public EntryRepositoryRegistrar getRepoRegistrar()
   {
      return registrar;
   }

   public EntryResolverRegistry getResolverRegistry()
   {
      return registrar.getResolverRegistry();
   }

   public IdFactoryProvider getIdFactoryProvider()
   {
      return idProvider;
   }

   public SqlExecutor getSqlExecutor()
   {
      return sqlExecutor;
   }

   public ConfigurationProperties getConfigFile()
   {
      return config;
   }

   private static class AccountStoreImpl implements AccountStore
   {

      @Override
      public Account lookup(LoginData loginData)
      {
         String userId = loginData.getLoginUserId();
         return (userId.equals(TEST_ACCOUNT.getUsername())) ? TEST_ACCOUNT : null;
      }

      @Override
      public Account getAccount(UUID accountId)
      {
         return (accountId.equals(TEST_ACCOUNT.getId())) ? TEST_ACCOUNT : null;
      }

   }
}