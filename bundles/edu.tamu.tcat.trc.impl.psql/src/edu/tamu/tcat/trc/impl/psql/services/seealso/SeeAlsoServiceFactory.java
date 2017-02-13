package edu.tamu.tcat.trc.impl.psql.services.seealso;

import java.text.MessageFormat;
import java.util.Objects;
import java.util.logging.Logger;

import edu.tamu.tcat.db.exec.sql.SqlExecutor;
import edu.tamu.tcat.trc.impl.psql.dbutils.DbTableManager;
import edu.tamu.tcat.trc.impl.psql.dbutils.TableDefinition;
import edu.tamu.tcat.trc.services.ServiceContext;
import edu.tamu.tcat.trc.services.ServiceFactory;
import edu.tamu.tcat.trc.services.TrcServiceException;
import edu.tamu.tcat.trc.services.seealso.SeeAlsoService;

public class SeeAlsoServiceFactory implements ServiceFactory<SeeAlsoService>
{
   private static final Logger logger = Logger.getLogger(SeeAlsoServiceFactory.class.getName());

   private static final String TABLE_NAME = "see_also";

   private final SqlExecutor sqlExecutor;

   public SeeAlsoServiceFactory(SqlExecutor sqlExecutor)
   {
      this.sqlExecutor = Objects.requireNonNull(sqlExecutor, "no sql executor provided");
      initializeTable();
   }

   private void initializeTable()
   {
      try
      {
         DbTableManager tableManager = new DbTableManager(sqlExecutor);
         TableDefinition tableDefinition = SeeAlsoServiceImpl.getTableDefinition(TABLE_NAME);
         if (!tableManager.exists(tableDefinition))
         {
            tableManager.create(tableDefinition);
         }
         else if (!tableManager.isConsistent(tableDefinition))
         {
            logger.warning(() -> TABLE_NAME + " table definition is not consistent. Attempting to continue anyway.");
         }
      }
      catch (Exception e)
      {
         throw new TrcServiceException(MessageFormat.format("failed to initialize {0} table ({1})", getClass().getSimpleName(), TABLE_NAME), e);
      }
   }

   @Override
   public Class<SeeAlsoService> getType()
   {
      return SeeAlsoService.class;
   }

   @Override
   public SeeAlsoService getService(ServiceContext<SeeAlsoService> ctx)
   {
      return new SeeAlsoServiceImpl(TABLE_NAME, sqlExecutor);
   }

   @Override
   public void shutdown()
   {
      // no-op
   }

}
