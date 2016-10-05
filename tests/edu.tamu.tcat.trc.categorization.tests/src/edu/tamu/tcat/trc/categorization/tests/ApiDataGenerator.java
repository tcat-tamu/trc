package edu.tamu.tcat.trc.categorization.tests;

import static java.text.MessageFormat.format;

import java.io.File;
import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.tamu.tcat.account.Account;
import edu.tamu.tcat.db.core.DataSourceException;
import edu.tamu.tcat.osgi.config.ConfigurationProperties;
import edu.tamu.tcat.osgi.config.file.SimpleFileConfigurationProperties;
import edu.tamu.tcat.trc.categorization.impl.CategorizationSchemeService;
import edu.tamu.tcat.trc.categorization.rest.v1.ModelAdapterV1;
import edu.tamu.tcat.trc.categorization.rest.v1.RestApiV1;
import edu.tamu.tcat.trc.impl.psql.entries.DbEntryRepositoryRegistry;
import edu.tamu.tcat.trc.repo.IdFactoryProvider;
import edu.tamu.tcat.trc.services.categorization.CategorizationService;
import edu.tamu.tcat.trc.services.categorization.CategorizationScheme;
import edu.tamu.tcat.trc.services.categorization.CategorizationScope;
import edu.tamu.tcat.trc.services.categorization.strategies.tree.EditTreeCategorizationCommand;
import edu.tamu.tcat.trc.services.categorization.strategies.tree.TreeCategorization;
import edu.tamu.tcat.trc.services.categorization.strategies.tree.TreeNode;
import edu.tamu.tcat.trc.services.categorization.strategies.tree.TreeNodeMutator;
import edu.tamu.tcat.trc.test.ClosableSqlExecutor;
import edu.tamu.tcat.trc.test.TestUtils;

public class ApiDataGenerator
{
   private static final String TBL_NAME = "test_categorizations";

   private static Account account = new MockAccount(UUID.randomUUID(), "Test Account");

   private ClosableSqlExecutor exec;
   private ConfigurationProperties config;

   protected CategorizationSchemeService svc;

   protected MockEntryResolver entryResolver;

   protected DbEntryRepositoryRegistry repos;

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

      IdFactoryProvider idProvider = TestUtils.makeIdFactoryProvider();

      repos = new DbEntryRepositoryRegistry();
      repos.setConfiguration(config);
      repos.setIdFactory(idProvider);
      repos.setSqlExecutor(exec);

      entryResolver = new MockEntryResolver();
      repos.getResolverRegistry().register(entryResolver);

      svc = new CategorizationSchemeService();
      svc.bindSqlExecutor(exec);
      svc.bindIdProvider(idProvider);
      svc.bindEntryRepoResolver(repos);
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
   public void buildThematicOverviews() throws Exception
   {
      ObjectMapper mapper = new ObjectMapper();
      File datafile = new File("res/data/sda_themes.json");
      File outfile = new File("res/data/sda_themes.api.json");
      Node node = mapper.readValue(datafile, Node.class);

      String scopeId = "test.categorizations";
      CategorizationScope scope = svc.createScope(account, scopeId);

      CategorizationService repository = svc.getRepository(scope);
      TreeCategorization overviews = createCategorization(repository, node);

      RestApiV1.Categorization apiScheme = ModelAdapterV1.adapt(overviews);
      mapper.writeValue(outfile, apiScheme);
   }

   private TreeCategorization createCategorization(CategorizationService repository, Node node) throws InterruptedException, ExecutionException, TimeoutException
   {
      String schemeKey = "overviews";
      EditTreeCategorizationCommand command =
            (EditTreeCategorizationCommand)repository.create(CategorizationScheme.Strategy.TREE, schemeKey);
      command.setLabel("Thematic Overview Articles");
      command.execute().get(10, TimeUnit.SECONDS);    // TODO need an easier way to do this.

      TreeCategorization scheme = (TreeCategorization)repository.get(schemeKey);
      TreeNode root = scheme.getRootNode();

      command = (EditTreeCategorizationCommand)repository.edit(scheme.getId());
      TreeNodeMutator rootNodeMutator = command.editNode(root.getId());
      node.children.stream().forEach(child -> this.apply(rootNodeMutator, child));

      command.execute().get(10, TimeUnit.SECONDS);
      return (TreeCategorization)repository.get(schemeKey);
   }

   private void apply(TreeNodeMutator mutator, Node node)
   {
      TreeNodeMutator childMutator = mutator.add(node.label);
      node.children.stream().forEach(child -> this.apply(childMutator, child));
   }

   public static class Node
   {
      public String label;
      public List<Node> children;
   }
}
