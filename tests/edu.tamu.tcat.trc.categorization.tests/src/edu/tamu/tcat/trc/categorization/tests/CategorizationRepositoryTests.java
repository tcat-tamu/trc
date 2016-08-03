package edu.tamu.tcat.trc.categorization.tests;

import static java.text.MessageFormat.format;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.tamu.tcat.account.Account;
import edu.tamu.tcat.db.core.DataSourceException;
import edu.tamu.tcat.osgi.config.ConfigurationProperties;
import edu.tamu.tcat.osgi.config.file.SimpleFileConfigurationProperties;
import edu.tamu.tcat.trc.categorization.CategorizationRepo;
import edu.tamu.tcat.trc.categorization.CategorizationScheme;
import edu.tamu.tcat.trc.categorization.CategorizationScheme.Strategy;
import edu.tamu.tcat.trc.categorization.CategorizationScope;
import edu.tamu.tcat.trc.categorization.EditCategorizationCommand;
import edu.tamu.tcat.trc.categorization.impl.CategorizationSchemeService;
import edu.tamu.tcat.trc.entries.core.BasicResolverRegistry;
import edu.tamu.tcat.trc.entries.core.EntryResolverRegistry;
import edu.tamu.tcat.trc.repo.IdFactoryProvider;
import edu.tamu.tcat.trc.test.ClosableSqlExecutor;
import edu.tamu.tcat.trc.test.TestUtils;

public abstract class CategorizationRepositoryTests
{
   private static final String TBL_NAME = "test_categorizations";

   private static Account account = new MockAccount(UUID.randomUUID(), "Test Account");

   private ClosableSqlExecutor exec;
   private ConfigurationProperties config;
   private EntryResolverRegistry registry;

   protected CategorizationSchemeService svc;

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
   public void testCreateScope()
   {
      String scopeId = "test.categorizations";
      CategorizationScope scope = svc.createScope(account, scopeId);

      assertNotNull("No categorization scope created", scope);
      assertEquals("Account Ids don't match", account.getId(), scope.getAccount().getId());
      assertEquals("Scope Ids don't match", scopeId, scope.getScopeId());
   }

   @Test
   public void testAcquireRepository()
   {
      String scopeId = "test.categorizations";
      CategorizationScope scope = svc.createScope(account, scopeId);

      CategorizationRepo repository = svc.getRepository(scope);
      assertNotNull("No categorization repo created", repository);

   }

   @Test
   public void testCreateCategorization() throws Exception
   {
      CategorizationRepo repository = getDefaultRepo();

      String key = "test";
      String label = "Hierarchical Test Categorization";
      String description = "A simple hierarchical categorization for unit testing purposes.";

      EditCategorizationCommand cmd = repository.create(getStrategy(), key);
      cmd.setLabel(label);
      cmd.setDescription(description);

      String id = cmd.execute().get(10, TimeUnit.SECONDS);
      assertNotNull("No id provided", id);

      CategorizationScheme scheme = repository.getById(id);
      assertEquals(id, scheme.getId());
      assertEquals(key, scheme.getKey());
      assertEquals(label, scheme.getLabel());
      assertEquals(description, scheme.getDescription());
   }

   @Test
   public void testEditCategorization() throws Exception
   {
      CategorizationRepo repository = getDefaultRepo();

      String key = "test";
      String label = "Hierarchical Test Categorization";
      String description = "A simple hierarchical categorization for unit testing purposes.";

      EditCategorizationCommand cmd = repository.create(getStrategy(), key);
      cmd.setLabel(label);
      cmd.setDescription(description);

      String id = cmd.execute().get(10, TimeUnit.SECONDS);

      CategorizationScheme first = repository.getById(id);

      String key2 = "edit test";
      String label2 = "Revised Hierarchical Test Categorization";
      String description2 = "An updated definition for simple hierarchical categorization for unit testing purposes.";

      cmd = repository.edit(id);
      cmd.setKey(key2);
      cmd.setLabel(label2);
      cmd.setDescription(description2);

      cmd.execute().get(10, TimeUnit.SECONDS);

      // ensure the values are modified
      CategorizationScheme second = repository.getById(id);
      assertEquals(id, second.getId());
      assertEquals(key2, second.getKey());
      assertEquals(label2, second.getLabel());
      assertEquals(description2, second.getDescription());

      // ensure the original values are not modified
      assertEquals(id, first.getId());
      assertEquals(key, first.getKey());
      assertEquals(label, first.getLabel());
      assertEquals(description, first.getDescription());
   }

   // TODO scope isolation tests

   @Test
   public void testRemoveCategorization() throws Exception
   {
      CategorizationRepo repository = getDefaultRepo();

      String key = "test";
      String label = "Hierarchical Test Categorization";
      String description = "A simple hierarchical categorization for unit testing purposes.";

      EditCategorizationCommand cmd = repository.create(getStrategy(), key);
      cmd.setLabel(label);
      cmd.setDescription(description);

      String id = cmd.execute().get(10, TimeUnit.SECONDS);

      CategorizationScheme first = repository.getById(id);
      assertNotNull("Failed to obtain initial repository", first);

      repository.remove(id).get(10, TimeUnit.SECONDS);
      try
      {
         repository.getById(id);
         assertFalse("Retrieved deleted entry", true);
      }
      catch (IllegalArgumentException ex)
      {
         // expected behavior
      }
   }

   protected abstract Strategy getStrategy();

   private CategorizationRepo getDefaultRepo()
   {
      String scopeId = "test.categorizations";
      CategorizationScope scope = svc.createScope(account, scopeId);
      CategorizationRepo repository = svc.getRepository(scope);
      return repository;
   }

   public static class TreeCategorizationRepoTests extends CategorizationRepositoryTests
   {
      @Override
      protected Strategy getStrategy()
      {
         return CategorizationScheme.Strategy.TREE;
      }

      // TODO
      //       create on different scopes (same/different key)
      //       create on same scope (same/different key)

   }

   private static class MockAccount implements Account
   {

      private final UUID id;
      private final String displayName;

      public MockAccount(UUID id, String displayName)
      {
         this.id = id;
         this.displayName = displayName;

      }

      @Override
      public UUID getId()
      {
         return id;
      }

      @Override
      public String getDisplayName()
      {
         return displayName;
      }

      @Override
      public boolean isActive()
      {
         return true;
      }

   }
}
