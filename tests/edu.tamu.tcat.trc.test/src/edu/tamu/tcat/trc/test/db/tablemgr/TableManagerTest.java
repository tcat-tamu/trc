package edu.tamu.tcat.trc.test.db.tablemgr;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.tamu.tcat.db.core.DataSourceException;
import edu.tamu.tcat.trc.impl.psql.dbutils.ColumnDefinition;
import edu.tamu.tcat.trc.impl.psql.dbutils.TableDefinition;

public class TableManagerTest
{

   public TableManagerTest()
   {
      // TODO Auto-generated constructor stub
   }

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

   }

   @After
   public void teardownTest() throws DataSourceException
   {

   }

   @Test
   public void doTest()
   {
      TableDefinition.Builder builder = new TableDefinition.Builder();
      builder.setName("account_login_data");

      builder.addColumn(
            new ColumnDefinition.Builder()
                       .setName("account_id")
                       .setType(ColumnDefinition.ColumnType.varchar)
                       .notNull()
                       .build());

      builder.addColumn(
            new ColumnDefinition.Builder()
            .setName("provider")
            .setType(ColumnDefinition.ColumnType.varchar)
            .notNull()
            .build());

      builder.addColumn(
            new ColumnDefinition.Builder()
            .setName("provider_id")
            .setType(ColumnDefinition.ColumnType.varchar)
            .notNull()
            .build());

      TableDefinition table = builder.build();
      System.out.println(table.getCreateSql());
   }
}
