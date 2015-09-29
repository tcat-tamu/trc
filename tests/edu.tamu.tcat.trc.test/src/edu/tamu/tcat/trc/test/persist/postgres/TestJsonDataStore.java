package edu.tamu.tcat.trc.test.persist.postgres;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.tamu.tcat.trc.persist.BasicSchemaBuilder;
import org.tamu.tcat.trc.persist.RepositorySchema;

import edu.tamu.tcat.db.core.DataSourceException;
import edu.tamu.tcat.db.exec.sql.SqlExecutor;

public class TestJsonDataStore
{

//   private PsqlJsonStore dataStore;
   private SqlExecutor sqlExec;

   public TestJsonDataStore()
   {
   }

   @Before
   public void setUp() throws DataSourceException
   {
//      dataStore = new PsqlJsonStore();
//      ConfigurationProperties config = TestUtils.loadConfigFile();
//      sqlExec = TestUtils.initPostgreSqlExecutor(config);
//
//      dataStore.setDbExecutor(sqlExec);
   }

   @After
   public void tearDown() throws Exception
   {
      sqlExec.close();
//      dataStore.dispose();
   }

   private RepositorySchema buildDefaultSchema(BasicSchemaBuilder builder)
   {
      builder.setIdField(idField);
      builder.setDataField(dateField);
      builder.setCreatedField(createdField);
      builder.setModifiedField(modifiedField);
      builder.setRemovedField(removedField);
      RepositorySchema schema = builder.build();
      return schema;
   }

   private static final String tableName = "test_table";
   private static final String idField = "id";
   private static final String dateField = "person";
   private static final String createdField = "date_created";
   private static final String modifiedField = "date_modified";
   private static final String removedField = "removed";

   @Test
   public void testSchemaBuilder()
   {
      BasicSchemaBuilder builder = new BasicSchemaBuilder();

      RepositorySchema schema = buildDefaultSchema(builder);

      assertEquals("id fields do not match", idField, schema.getIdField());
      assertEquals("data fields do not match", dateField, schema.getDataField());
      assertEquals("created fields do not match", createdField, schema.getCreatedField());
      assertEquals("modified fields do not match", modifiedField, schema.getModifiedField());
      assertEquals("removed fields do not match", removedField, schema.getRemovedField());

//      try {
//         builder.setName("bob");
//         assertFalse("set value after use.", true);
//      } catch (Exception ex) {
//         // expected result
//      }

   }

   @Test
   public void createSchema() throws Exception
   {
      BasicSchemaBuilder builder = new BasicSchemaBuilder();
      RepositorySchema schema = buildDefaultSchema(builder);
//      try
//      {
//         if (!dataStore.create(schema))
//            assertFalse("Failed to create new schema", true);
//
//         if (dataStore.exists(schema))
//            assertFalse("Schema does not exist after creation", true);
//      }
//      finally {
//         Future<Object> future = sqlExec.submit((conn) ->
//         {
//            try (Statement stmt = conn.createStatement())
//            {
//               stmt.executeUpdate(MessageFormat.format("DROP TABLE {0}", schema.getName()));
//               return null;
//            }
//         });
//
//         future.get();
//
//      }
   }
}
