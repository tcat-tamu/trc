package edu.tamu.tcat.trc.test.persist.postgres;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.net.URI;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.util.concurrent.Futures;

import edu.tamu.tcat.account.Account;
import edu.tamu.tcat.db.core.DataSourceException;
import edu.tamu.tcat.db.exec.sql.SqlExecutor;
import edu.tamu.tcat.osgi.config.ConfigurationProperties;
import edu.tamu.tcat.trc.refman.BibliographicReference;
import edu.tamu.tcat.trc.refman.BibliographicReference.CreatorValue;
import edu.tamu.tcat.trc.refman.BibliographicReference.FieldValue;
import edu.tamu.tcat.trc.refman.EditReferenceCommand;
import edu.tamu.tcat.trc.refman.RefManagerException;
import edu.tamu.tcat.trc.refman.dto.CreatorDTO;
import edu.tamu.tcat.trc.refman.postgres.EditCmdFactoryImpl.EditRefCommand;
import edu.tamu.tcat.trc.refman.postgres.EditCollectionCmdFactory.EditCollectionCommand;
import edu.tamu.tcat.trc.refman.postgres.LocalReferenceCollection;
import edu.tamu.tcat.trc.refman.postgres.RefCollectionMeta;
import edu.tamu.tcat.trc.refman.types.CreatorRole;
import edu.tamu.tcat.trc.refman.types.ItemFieldType;
import edu.tamu.tcat.trc.refman.types.ItemType;
import edu.tamu.tcat.trc.refman.types.ItemTypeProvider;
import edu.tamu.tcat.trc.refman.types.zotero.ZoteroTypeProvider;
import edu.tamu.tcat.trc.repo.BasicSchemaBuilder;
import edu.tamu.tcat.trc.repo.DocumentRepository;
import edu.tamu.tcat.trc.repo.RepositoryException;
import edu.tamu.tcat.trc.repo.RepositorySchema;
import edu.tamu.tcat.trc.test.TestUtils;

public class TestJsonDataStore
{

   private SqlExecutor sqlExec;
   private Map<String, ItemTypeProvider> providers = new HashMap<>();
   private ReferenceManagerCollection refColl;
   private static final String collectionName = "My Reference Collection";
   private static final String collectionDesc = "Books found in the early 1700's";

   public TestJsonDataStore()
   {
   }

   @Before
   public void setUp() throws DataSourceException
   {
      ConfigurationProperties config = TestUtils.loadConfigFile();
      sqlExec = TestUtils.initPostgreSqlExecutor(config);
      ZoteroTypeProvider provider = new ZoteroTypeProvider();
      provider.setConfiguration(config);
      provider.activate();
      providers.put(ZoteroTypeProvider.ZOTERO_PROVIDER_ID, provider);
      refColl = new ReferenceManagerCollection(sqlExec, providers);
   }

   @After
   public void tearDown() throws Exception
   {
      sqlExec = null;
   }

   @Test
   public void testSchemaBuilder()
   {
      BasicSchemaBuilder builder = new BasicSchemaBuilder();

      RepositorySchema schema = refColl.buildDefaultSchema(builder);

      assertEquals("table names do not match", refColl.getTableId(), schema.getId());
      assertEquals("id fields do not match", refColl.getIdfield(), schema.getIdField());
      assertEquals("data fields do not match", refColl.getDatafield(), schema.getDataField());
      assertEquals("created fields do not match", refColl.getCreatedfield(), schema.getCreatedField());
      assertEquals("modified fields do not match", refColl.getModifiedfield(), schema.getModifiedField());
      assertEquals("removed fields do not match", refColl.getRemovedfield(), schema.getRemovedField());

      try {
         builder.setId("bob");
         assertFalse("set value after use.", true);
      } catch (Exception ex) {
         // expected result
      }

   }

   @Test
   public void createReferenceCollection() throws Exception
   {
      String collectionProv = providers.get(ZoteroTypeProvider.ZOTERO_PROVIDER_ID).getId();
      DocumentRepository<RefCollectionMeta, EditCollectionCommand> docRepoColl = refColl.buildCollectionRepo();
      try
      {
         assertFalse(MessageFormat.format("Table can not be found {0} ", refColl.getRefmancoll()), !exists(refColl.getRefmancoll()));

         String collectionId = createCollection(collectionProv, docRepoColl);

         RefCollectionMeta refCollectionMeta = docRepoColl.get(collectionId);

         assertEquals(collectionName, refCollectionMeta.name);
         assertEquals(collectionDesc, refCollectionMeta.description);
         assertEquals(collectionProv, refCollectionMeta.providerId);
      }
      finally
      {
         Future<Object> future = sqlExec.submit((conn) ->
         {
            try (Statement stmt = conn.createStatement())
            {
               stmt.executeUpdate(MessageFormat.format("DROP TABLE {0}", refColl.getRefmancoll()));
               return null;
            }
         });

         future.get();
         docRepoColl.dispose();
      }
   }

   @Test
   public void editReferenceCollection() throws Exception
   {
      String collectionProv = providers.get(ZoteroTypeProvider.ZOTERO_PROVIDER_ID).getId();
      DocumentRepository<RefCollectionMeta, EditCollectionCommand> docRepoColl = refColl.buildCollectionRepo();
      try
      {
         assertFalse(MessageFormat.format("Table can not be found {0} ", refColl.getRefmancoll()), !exists(refColl.getRefmancoll()));

         String collectionId = createCollection(collectionProv, docRepoColl);

         EditCollectionCommand editCmd = docRepoColl.edit(collectionId);
         editCmd.setName("New Collection Name");
         editCmd.setDescription("New description for this new collection name");
         editCmd.setProvider("Local Storage");
         editCmd.execute();

         RefCollectionMeta refCollectionMeta = docRepoColl.get(collectionId);

         assertEquals(collectionId, refCollectionMeta.id);
         assertFalse("Collection Name did not change.", refCollectionMeta.name.equals(collectionName));
         assertFalse("Collection Description did not change.", refCollectionMeta.description.equals(collectionDesc));
         assertFalse("Collection Provider did not change.", refCollectionMeta.providerId.equals(collectionProv));

      }
      finally
      {
         Future<Object> future = sqlExec.submit((conn) ->
         {
            try (Statement stmt = conn.createStatement())
            {
               stmt.executeUpdate(MessageFormat.format("DROP TABLE {0}", refColl.getRefmancoll()));
               return null;
            }
         });

         future.get();
         docRepoColl.dispose();
      }

   }

   @Test
   public void deleteReferenceCollection() throws Exception
   {
      String collectionProv = providers.get(ZoteroTypeProvider.ZOTERO_PROVIDER_ID).getId();
      DocumentRepository<RefCollectionMeta, EditCollectionCommand> docRepoColl = refColl.buildCollectionRepo();
      try
      {
         assertFalse(MessageFormat.format("Table can not be found {0} ", refColl.getRefmancoll()), !exists(refColl.getRefmancoll()));

         String collectionId = createCollection(collectionProv, docRepoColl);

         Boolean condition = docRepoColl.delete(collectionId).get();
         assertFalse("Collection failed to be deleted.", !condition);

         try
         {
            docRepoColl.get(collectionId);
            assertFalse(MessageFormat.format("Collection {0} was not deleted from the db", collectionId), true);
         }
         catch(RepositoryException repoE)
         {
            assertFalse("Expected result", false);
         }
      }
      finally
      {
         Future<Object> future = sqlExec.submit((conn) ->
         {
            try (Statement stmt = conn.createStatement())
            {
               stmt.executeUpdate(MessageFormat.format("DROP TABLE {0}", refColl.getRefmancoll()));
               return null;
            }
         });

         future.get();
         docRepoColl.dispose();
      }

   }


   @Test
   public void createBiblioReference() throws Exception
   {
      ItemTypeProvider itemTypeProvider = providers.get(ZoteroTypeProvider.ZOTERO_PROVIDER_ID);
      String collectionProv = itemTypeProvider.getId();
      DocumentRepository<RefCollectionMeta, EditCollectionCommand> buildCollectionRepo = refColl.buildCollectionRepo();
      DocumentRepository<BibliographicReference, EditReferenceCommand> bibRefRepo = null;

      try
      {
         assertFalse(MessageFormat.format("Table can not be found {0} ", refColl.getRefmancoll()), !exists(refColl.getRefmancoll()));
         String collectionId = createCollection(collectionProv, buildCollectionRepo);

         bibRefRepo = refColl.buildReferenceRepo();
         assertFalse(MessageFormat.format("Table can not be found {0} ", refColl.getRefmanrefs()), !exists(refColl.getRefmanrefs()));

         String bibRefId = createReference(itemTypeProvider, bibRefRepo, collectionId);

         BibliographicReference bibRef = bibRefRepo.get(bibRefId);

         ItemType type = bibRef.getType();
         List<ItemFieldType> itemFieldTypes = type.getFields();
         Set<FieldValue> values = bibRef.getValues();
         for(ItemFieldType ift : itemFieldTypes)
         {
            for(FieldValue vVal : values)
            {
               ItemFieldType fieldType = vVal.getFieldType();
               if(fieldType.getId().equals(ift.getId()))
                  break;
            }
            assertFalse("The value was not found", false);
         }

         List<CreatorDTO> creators = refColl.getCreators();
         for(CreatorValue creator : bibRef.getCreators())
         {
            for(CreatorDTO dto : creators)
            {
               if(creator.getRoleId().equals(dto.role))
               {
                  assertEquals(creator.getAuthId(), dto.authoritiveId);
                  assertEquals(creator.getGivenName(), dto.firstName);
                  assertEquals(creator.getFamilyName(), dto.lastName);
               }
            }
         }
      }
      finally
      {
         Future<Object> future = sqlExec.submit((conn) ->
         {
            try (Statement stmt = conn.createStatement())
            {
               stmt.executeUpdate(MessageFormat.format("DROP TABLE {0}", refColl.getRefmancoll()));
               stmt.executeUpdate(MessageFormat.format("DROP TABLE {0}", refColl.getRefmanrefs()));
               return null;
            }
         });

         future.get();
         bibRefRepo.dispose();
         buildCollectionRepo.dispose();
      }
   }
   @Test
   public void editBiblioReference() throws Exception
   {
      ItemTypeProvider itemTypeProvider = providers.get(ZoteroTypeProvider.ZOTERO_PROVIDER_ID);
      String collectionProv = itemTypeProvider.getId();
      DocumentRepository<RefCollectionMeta, EditCollectionCommand> buildCollectionRepo = refColl.buildCollectionRepo();
      DocumentRepository<BibliographicReference, EditReferenceCommand> bibRefRepo = null;

      try
      {
         assertFalse(MessageFormat.format("Table can not be found {0} ", refColl.getRefmancoll()), !exists(refColl.getRefmancoll()));
         String collectionId = createCollection(collectionProv, buildCollectionRepo);

         bibRefRepo = refColl.buildReferenceRepo();
         assertFalse(MessageFormat.format("Table can not be found {0} ", refColl.getRefmanrefs()), !exists(refColl.getRefmanrefs()));

         String bibRefId = createReference(itemTypeProvider, bibRefRepo, collectionId);

         BibliographicReference original = bibRefRepo.get(bibRefId);

         EditReferenceCommand editCmd = bibRefRepo.edit(bibRefId);
         ((EditRefCommand)editCmd).setReferenceContext(localRefCollection, refColl.getAccount());


         List<CreatorDTO> creatorDTOs = new ArrayList<>(refColl.getCreators());
         ItemType itemType = itemTypeProvider.getItemType("videoRecording");
         editCmd.setType(itemType);

         for(CreatorRole cRole : itemType.getCreatorRoles())
         {
            for(CreatorDTO cDTO : creatorDTOs)
            {
               if(cRole.getId().equals(cDTO.role))
               {
                  cDTO.authoritiveId = UUID.randomUUID().toString();
                  cDTO.firstName = "Brad";
                  cDTO.lastName = "Bird";
               }
            }

         }
         editCmd.setCreators(creatorDTOs);

         List<ItemFieldType> fields = itemType.getFields();
         int num = 0;
         for(ItemFieldType field : fields)
         {
            editCmd.setField(field, "Change Text " + num++);
         }

         editCmd.execute();

         BibliographicReference edited = bibRefRepo.get(bibRefId);

         assertFalse("The id's should not be the same.", !original.getId().equals(edited.getId()));
         assertFalse("The Biblical Reference Type is the same.",original.getType().equals(edited.getType()));
         assertFalse("The Creator is the same.", original.getCreators().equals(edited.getCreators()));
         assertFalse("The Field values are the same.", original.getValues().equals(edited.getValues()));

      }
      finally
      {
         Future<Object> future = sqlExec.submit((conn) ->
         {
            try (Statement stmt = conn.createStatement())
            {
               stmt.executeUpdate(MessageFormat.format("DROP TABLE {0}", refColl.getRefmancoll()));
               stmt.executeUpdate(MessageFormat.format("DROP TABLE {0}", refColl.getRefmanrefs()));
               return null;
            }
         });

         future.get();
         bibRefRepo.dispose();
         buildCollectionRepo.dispose();
      }
   }

   private static LocalReferenceCollection localRefCollection;
   private String createReference(ItemTypeProvider itemTypeProvider, DocumentRepository<BibliographicReference, EditReferenceCommand> buildReferenceRepo, String collectionId) throws RefManagerException, InterruptedException, ExecutionException
   {
      Account myAccount = refColl.getAccount();

      URI id = URI.create(collectionId);

      localRefCollection = new LocalReferenceCollection(id, collectionName, myAccount, itemTypeProvider, buildReferenceRepo);

      EditReferenceCommand cmdRef = buildReferenceRepo.create();
      ((EditRefCommand)cmdRef).setReferenceContext(localRefCollection, myAccount);
      ItemType itemType = itemTypeProvider.getItemType("book");
      List<ItemFieldType> fields = itemType.getFields();
      List<CreatorRole> creatorRoles = itemType.getCreatorRoles();
      int num = 0;
      cmdRef.setType(itemType);

      for(ItemFieldType field : fields)
      {
         cmdRef.setField(field, "test " + num++);
      }
      refColl.setCreators(creatorRoles);
      cmdRef.setCreators(refColl.getCreators());

      Future<String> refFuture = cmdRef.execute();
      return refFuture.get();
   }

   private String createCollection(String collectionProv, DocumentRepository<RefCollectionMeta, EditCollectionCommand> docRepoColl) throws InterruptedException, ExecutionException
   {
      EditCollectionCommand cmd = docRepoColl.create();
      cmd.setName(collectionName);
      cmd.setDescription(collectionDesc);
      cmd.setProvider(collectionProv);

      Future<String> cmdFuture = cmd.execute();
      String collectionId = cmdFuture.get();
      return collectionId;
   }

   private boolean exists(String tableName) throws RepositoryException
   {
      Future<Boolean> result = sqlExec.submit(conn -> {
         return Boolean.valueOf(tableExists(conn, tableName));
      });

      return Futures.get(result, RepositoryException.class);
   }

   private boolean tableExists(Connection conn, String tablename) throws SQLException
   {
      // (20150814) Adapted from
      // http://stackoverflow.com/questions/20582500/how-to-check-if-a-table-exists-in-a-given-schema
      String sql = "SELECT EXISTS ("
            + "SELECT 1 FROM pg_catalog.pg_class c"
            + "  JOIN pg_catalog.pg_namespace n ON n.oid = c.relnamespace"
            + " WHERE c.relname = ? AND c.relkind = 'r'"
            + ")";

      try (PreparedStatement stmt = conn.prepareStatement(sql))
      {
         stmt.setString(1, tablename);

         ResultSet rs = stmt.executeQuery();
         rs.next();
         return Boolean.valueOf(rs.getBoolean(1));
      }
   }
}
