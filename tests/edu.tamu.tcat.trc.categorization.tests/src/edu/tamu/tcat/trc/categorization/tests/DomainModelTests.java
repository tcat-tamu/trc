package edu.tamu.tcat.trc.categorization.tests;

import static java.text.MessageFormat.format;
import static org.junit.Assert.assertEquals;

import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.tamu.tcat.db.core.DataSourceException;
import edu.tamu.tcat.osgi.config.ConfigurationProperties;
import edu.tamu.tcat.osgi.config.file.SimpleFileConfigurationProperties;
import edu.tamu.tcat.trc.categorization.impl.CategorizationSchemeService;
import edu.tamu.tcat.trc.entries.core.BasicResolverRegistry;
import edu.tamu.tcat.trc.entries.core.EntryResolverRegistry;
import edu.tamu.tcat.trc.repo.IdFactoryProvider;
import edu.tamu.tcat.trc.test.ClosableSqlExecutor;
import edu.tamu.tcat.trc.test.TestUtils;

public class DomainModelTests
{
   private static final String TBL_NAME = "test_categorizations";

   private ClosableSqlExecutor exec;
   private ConfigurationProperties config;
   private CategorizationSchemeService svc;
   private EntryResolverRegistry registry;

   @BeforeClass
   public static void setUp()
   {
      // TODO spin up DB, etc
   }

   @AfterClass
   public static void tearDown()
   {

   }

   @Before
   public void setupTest() throws DataSourceException
   {
      config = TestUtils.loadConfigFile();
      exec = TestUtils.initPostgreSqlExecutor(config);
      registry = new BasicResolverRegistry();
      IdFactoryProvider idProvider = TestUtils.makeIdFactoryProvider();

      svc = new CategorizationSchemeService();
      svc.bindSqlExecutor(exec);
      svc.bindIdProvider(idProvider);
      svc.bindEntityResolver(registry);
      // TODO configure search

      Map<String, Object> props = new HashMap<>();
      props.put(CategorizationSchemeService.PARAM_ID_CTX, "trc.services.categorization.schemes.ids");
      props.put(CategorizationSchemeService.PARAM_NODE_CTX, "trc.services.categorization.nodes.ids");
      props.put(CategorizationSchemeService.PARAM_TABLE_NAME, TBL_NAME);
      svc.activate(props);
   }

   @After
   public void tearDownTest() throws Exception
   {
      String sql = format("TRUNCATE {0}", TBL_NAME);
      Future<Void> future = exec.submit((conn) -> {
         try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.executeUpdate();
            return null;
         }
      });

      future.get();

      svc.deactivate();
      exec.close();

      if (config instanceof SimpleFileConfigurationProperties)
         ((SimpleFileConfigurationProperties)config).dispose();
   }

   @Test
   public void testHelloWorld()
   {
      // ensure that things spinup
      assertEquals("Hello", "Hello");
   }
}
