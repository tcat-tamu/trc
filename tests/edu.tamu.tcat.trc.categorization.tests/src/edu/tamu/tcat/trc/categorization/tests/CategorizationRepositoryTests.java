package edu.tamu.tcat.trc.categorization.tests;

import static java.text.MessageFormat.format;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.sql.PreparedStatement;
import java.util.List;
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

import edu.tamu.tcat.account.Account;
import edu.tamu.tcat.db.core.DataSourceException;
import edu.tamu.tcat.osgi.config.ConfigurationProperties;
import edu.tamu.tcat.osgi.config.file.SimpleFileConfigurationProperties;
import edu.tamu.tcat.trc.impl.psql.entries.DbEntryRepositoryRegistry;
import edu.tamu.tcat.trc.impl.psql.services.categorization.CategorizationServiceFactory;
import edu.tamu.tcat.trc.repo.id.IdFactoryProvider;
import edu.tamu.tcat.trc.resolver.EntryId;
import edu.tamu.tcat.trc.services.categorization.CategorizationScheme;
import edu.tamu.tcat.trc.services.categorization.CategorizationScheme.Strategy;
import edu.tamu.tcat.trc.services.categorization.CategorizationService;
import edu.tamu.tcat.trc.services.categorization.EditCategorizationCommand;
import edu.tamu.tcat.trc.services.categorization.strategies.tree.EditTreeCategorizationCommand;
import edu.tamu.tcat.trc.services.categorization.strategies.tree.PreOrderTraversal;
import edu.tamu.tcat.trc.services.categorization.strategies.tree.TreeCategorization;
import edu.tamu.tcat.trc.services.categorization.strategies.tree.TreeNode;
import edu.tamu.tcat.trc.services.categorization.strategies.tree.TreeNodeMutator;
import edu.tamu.tcat.trc.test.ClosableSqlExecutor;
import edu.tamu.tcat.trc.test.TestUtils;

public abstract class CategorizationRepositoryTests
{
   private static final String TBL_NAME = "test_categorizations";

   private static Account account = new MockAccount(UUID.randomUUID(), "Test Account");

   private ClosableSqlExecutor exec;
   private ConfigurationProperties config;

   protected CategorizationServiceFactory svc;

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
      repos.registerResolver(entryResolver);

      svc = new CategorizationServiceFactory(repos);
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

      svc.shutdown();
      exec.close();

      if (config instanceof SimpleFileConfigurationProperties)
         ((SimpleFileConfigurationProperties)config).dispose();
   }

   @Test
   public void testAcquireRepository()
   {
      String scopeId = "test.categorizations";
      CategorizationService repository = svc.getService(CategorizationService.makeContext(account, scopeId));
      assertNotNull("No categorization repo created", repository);
   }

   @Test
   public void testCreateCategorization() throws Exception
   {
      CategorizationService repository = getDefaultRepo();

      String key = "test";
      String label = "Hierarchical Test Categorization";
      String description = "A simple hierarchical categorization for unit testing purposes.";

      EditCategorizationCommand cmd = repository.create(getStrategy(), key);
      cmd.setLabel(label);
      cmd.setDescription(description);

      String id = cmd.execute().get(10, TimeUnit.SECONDS);
      assertNotNull("No id provided", id);

      CategorizationScheme scheme = repository.getById(id).orElse(null);
      assertEquals(id, scheme.getId());
      assertEquals(key, scheme.getKey());
      assertEquals(label, scheme.getLabel());
      assertEquals(description, scheme.getDescription());
   }

   @Test
   public void testGetByKey() throws Exception
   {
      CategorizationService repository = getDefaultRepo();

      String key = "test";
      String label = "Hierarchical Test Categorization";
      String description = "A simple hierarchical categorization for unit testing purposes.";

      EditCategorizationCommand cmd = repository.create(getStrategy(), key);
      cmd.setLabel(label);
      cmd.setDescription(description);

      String id = cmd.execute().get(10, TimeUnit.SECONDS);

      CategorizationScheme scheme = repository.get(key).orElse(null);
      assertEquals(id, scheme.getId());
      assertEquals(key, scheme.getKey());
      assertEquals(label, scheme.getLabel());
      assertEquals(description, scheme.getDescription());
   }

   @Test
   public void testEditCategorization() throws Exception
   {
      CategorizationService repository = getDefaultRepo();

      String key = "test";
      String label = "Hierarchical Test Categorization";
      String description = "A simple hierarchical categorization for unit testing purposes.";

      EditCategorizationCommand cmd = repository.create(getStrategy(), key);
      cmd.setLabel(label);
      cmd.setDescription(description);

      String id = cmd.execute().get(10, TimeUnit.SECONDS);

      CategorizationScheme first = repository.getById(id).orElse(null);

      String key2 = "edit test";
      String label2 = "Revised Hierarchical Test Categorization";
      String description2 = "An updated definition for simple hierarchical categorization for unit testing purposes.";

      cmd = repository.edit(id);
      cmd.setKey(key2);
      cmd.setLabel(label2);
      cmd.setDescription(description2);

      cmd.execute().get(10, TimeUnit.SECONDS);

      // ensure the values are modified
      CategorizationScheme second = repository.getById(id).orElse(null);
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
      CategorizationService repository = getDefaultRepo();

      String key = "test";
      String label = "Hierarchical Test Categorization";
      String description = "A simple hierarchical categorization for unit testing purposes.";

      EditCategorizationCommand cmd = repository.create(getStrategy(), key);
      cmd.setLabel(label);
      cmd.setDescription(description);

      String id = cmd.execute().get(10, TimeUnit.SECONDS);

      CategorizationScheme first = repository.getById(id).orElse(null);
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

   @Test
   public void testReferenceTokenization()
   {
      String entryDesc = "mock entry description";
      MockEntry mockEntry = entryResolver.create(entryDesc);
      EntryId mockRef = entryResolver.makeReference(mockEntry);

      String token = repos.getResolverRegistry().tokenize(mockRef);
      System.out.println(token);
      EntryId restored = repos.getResolverRegistry().decodeToken(token);
      assertEquals(mockRef.getId(), restored.getId());
      assertEquals(mockRef.getType(), restored.getType());
   }

   protected abstract Strategy getStrategy();

   protected CategorizationService getDefaultRepo()
   {
      String scopeId = "test.categorizations";
      return svc.getService(CategorizationService.makeContext(account, scopeId));
   }

   public static class TreeCategorizationRepoTests extends CategorizationRepositoryTests
   {
      private static final String NODE_DESC = "Node {0} is a node within this tree";

      @Override
      protected Strategy getStrategy()
      {
         return CategorizationScheme.Strategy.TREE;
      }

      private TreeCategorization getDefaultScheme(CategorizationService repository)
      {
         EditCategorizationCommand cmd = repository.create(getStrategy(), "test");
         cmd.setLabel("Hierarchical Test Categorization");
         cmd.setDescription("A simple hierarchical categorization for unit testing purposes.");

         try
         {
            String id = cmd.execute().get(10, TimeUnit.SECONDS);
            return (TreeCategorization)repository.getById(id).orElse(null);
         }
         catch (Exception ex)
         {
            throw new IllegalStateException("Failed to create default categorization scheme", ex);
         }
      }

      @Test
      public void testCreateNodes() throws Exception
      {
         CategorizationService repository = getDefaultRepo();
         TreeCategorization scheme = getDefaultScheme(repository);
         buildDefaultTree(repository, scheme);

         String id = scheme.getId();
         scheme = (TreeCategorization)repository.getById(id).orElse(null);
         TreeNode root = scheme.getRootNode();

         testChildren(root, "A");
         testChildren(findByLabel(scheme, "A"), "B", "C");
         testChildren(findByLabel(scheme, "B"), "D", "E", "F");
         testChildren(findByLabel(scheme, "C"), "G", "H");
         testChildren(findByLabel(scheme, "D"));
         testChildren(findByLabel(scheme, "E"));
         testChildren(findByLabel(scheme, "F"));
         testChildren(findByLabel(scheme, "G"));
         testChildren(findByLabel(scheme, "H"), "I");
         testChildren(findByLabel(scheme, "I"));
      }

      @Test
      public void testAssociateEntry() throws Exception
      {
         String entryDesc = "mock entry description";
         MockEntry mockEntry = entryResolver.create(entryDesc);
         EntryId mockRef = entryResolver.makeReference(mockEntry);

         CategorizationService repository = getDefaultRepo();
         TreeCategorization scheme = getDefaultScheme(repository);
         buildDefaultTree(repository, scheme);

         String id = scheme.getId();
         scheme = repository.getById(id, TreeCategorization.class).orElse(null);

         TreeNode nodeA = findByLabel(scheme, "A");

         EditTreeCategorizationCommand command = repository.edit(id, EditTreeCategorizationCommand.class);
         TreeNodeMutator mutator = command.editNode(nodeA.getId());
         mutator.associateEntryRef(mockRef);

         command.execute().get(10, TimeUnit.SECONDS);

         scheme = repository.getById(id, TreeCategorization.class).orElse(null);
         nodeA = findByLabel(scheme, "A");
         EntryId ref = nodeA.getAssociatedEntryRef();
         MockEntry entry = nodeA.getAssociatedEntry(MockEntry.class);

         assertEquals(mockRef.getId(), ref.getId());
         assertEquals(mockRef.getType(), ref.getType());

         assertEquals(mockEntry.getId(), entry.getId());
         assertEquals(mockEntry.getDescription(), entry.getDescription());
      }

      @Test
      public void testRemoveNode() throws Exception
      {
         CategorizationService repository = getDefaultRepo();
         TreeCategorization scheme = getDefaultScheme(repository);
         buildDefaultTree(repository, scheme);

         String id = scheme.getId();
         scheme = repository.getById(id, TreeCategorization.class).orElse(null);

         String fId = findByLabel(scheme, "F").getId();
         String hId = findByLabel(scheme, "H").getId();

         EditTreeCategorizationCommand command = repository.edit(id, EditTreeCategorizationCommand.class);
         command.removeNode(fId); // remove node F
         command.removeNode(hId); // remove node H (and I as part of sub-tree)
         command.execute().get(10, TimeUnit.SECONDS);

         scheme = repository.getById(id, TreeCategorization.class).orElse(null);
         assertNull(findByLabel(scheme, "F"));
         assertNull(findByLabel(scheme, "H"));
         assertNull(findByLabel(scheme, "I"));

         testChildren(scheme.getRootNode(), "A");
         testChildren(findByLabel(scheme, "A"), "B", "C");
         testChildren(findByLabel(scheme, "B"), "D", "E");
         testChildren(findByLabel(scheme, "C"), "G");
         testChildren(findByLabel(scheme, "D"));
         testChildren(findByLabel(scheme, "E"));
         testChildren(findByLabel(scheme, "G"));
      }

      @Test
      public void testMoveNode() throws Exception
      {
         CategorizationService repository = getDefaultRepo();
         TreeCategorization scheme = getDefaultScheme(repository);
         buildDefaultTree(repository, scheme);

         String id = scheme.getId();
         scheme = repository.getById(id, TreeCategorization.class).orElse(null);

         String bId = findByLabel(scheme, "B").getId();
         TreeNode cNode = findByLabel(scheme, "C");
         TreeNode hNode = findByLabel(scheme, "H");
         String cId = cNode.getId();
         int ix = cNode.getChildren().indexOf(hNode);

         // move B between G and H
         EditTreeCategorizationCommand command = repository.edit(id, EditTreeCategorizationCommand.class);
         command.move(bId, cId, ix);
         command.execute().get(10, TimeUnit.SECONDS);

         scheme = repository.getById(id, TreeCategorization.class).orElse(null);
         testChildren(scheme.getRootNode(), "A");
         testChildren(findByLabel(scheme, "A"), "C");
         testChildren(findByLabel(scheme, "B"), "D", "E", "F");
         testChildren(findByLabel(scheme, "C"), "G", "B", "H");
         testChildren(findByLabel(scheme, "D"));
         testChildren(findByLabel(scheme, "E"));
         testChildren(findByLabel(scheme, "F"));
         testChildren(findByLabel(scheme, "G"));
         testChildren(findByLabel(scheme, "H"), "I");
         testChildren(findByLabel(scheme, "I"));
      }

      //  Creates a default tree structer
      //        A
      //   B         C
      // D E F     G   H
      //               I
      @SuppressWarnings("unused")
      private void buildDefaultTree(CategorizationService repository, TreeCategorization scheme) throws InterruptedException, ExecutionException, TimeoutException
      {
         // TODO note that we shouldn't be able to edit various properties of the root node . . .
         //      not sure how to handle this.
         EditTreeCategorizationCommand command = (EditTreeCategorizationCommand)repository.edit(scheme.getId());
         TreeNodeMutator rootMutator = command.editNode(scheme.getId());

         TreeNodeMutator a = createNode(rootMutator, "A");
         TreeNodeMutator b = createNode(a, "B");
         TreeNodeMutator c = createNode(a, "C");
         TreeNodeMutator d = createNode(b, "D");
         TreeNodeMutator e = createNode(b, "E");
         TreeNodeMutator f = createNode(b, "F");
         TreeNodeMutator g = createNode(c, "G");
         TreeNodeMutator h = createNode(c, "H");
         TreeNodeMutator i = createNode(h, "I");

         command.execute().get(10, TimeUnit.SECONDS);
      }

      private TreeNode findByLabel(TreeCategorization scheme, String label)
      {
         PreOrderTraversal traversal = new PreOrderTraversal(n -> label.equals(n.getLabel()));
         List<TreeNode> matches = traversal.apply(scheme);

         // this is a unit test -- ignoring all manner of potential errors.
         return !matches.isEmpty() ? matches.get(0) : null;
      }

      private void testChildren(TreeNode parent, String... labels)
      {
         List<TreeNode> children = parent.getChildren();

         assertEquals("Unexpected number children for root", labels.length, children.size());
         for (int ix = 0; ix < labels.length; ix++)
         {
            assertEquals("Incorrect Label", labels[ix], children.get(ix).getLabel());
            assertEquals("Incorrect Label", format(NODE_DESC, labels[ix]), children.get(ix).getDescription());
         }
      }

      private TreeNodeMutator createNode(TreeNodeMutator parent, String label)
      {
         TreeNodeMutator a = parent.add(label);
         a.setDescription(format(NODE_DESC, label));
         return a;
      }

      // TODO test associate entries with nodes
      // TODO create TreeNodeVisitor (predicate)

      // TODO
      //       create on different scopes (same/different key)
      //       create on same scope (same/different key)

   }
}
