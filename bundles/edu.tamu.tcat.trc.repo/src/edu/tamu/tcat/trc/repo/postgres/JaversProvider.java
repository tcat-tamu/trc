package edu.tamu.tcat.trc.repo.postgres;

import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.sql.DataSource;

import org.javers.common.collections.Optional;
import org.javers.core.IdBuilder;
import org.javers.core.Javers;
import org.javers.core.JaversBuilder;
import org.javers.core.changelog.ChangeProcessor;
import org.javers.core.commit.Commit;
import org.javers.core.diff.Change;
import org.javers.core.diff.Diff;
import org.javers.core.diff.changetype.PropertyChange;
import org.javers.core.json.JsonConverter;
import org.javers.core.metamodel.object.CdoSnapshot;
import org.javers.core.metamodel.property.Property;
import org.javers.core.metamodel.type.JaversType;
import org.javers.repository.jql.GlobalIdDTO;
import org.javers.repository.jql.JqlQuery;
import org.javers.repository.sql.DialectName;
import org.javers.repository.sql.JaversSqlRepository;
import org.javers.repository.sql.SqlRepositoryBuilder;

import edu.tamu.tcat.db.provider.DataSourceProvider;

public class JaversProvider
{

   private DataSourceProvider dsProvider;

   public JaversProvider()
   {
   }
   
   public void setDataSourceProvider(DataSourceProvider dataSourceProvider)
   {
      this.dsProvider = dataSourceProvider;
   }
   
   public void activate()
   {
   }
   
   public void dispose()
   {
   }
   
   public Javers getJavers()
   {
      
      try
      {
         ConnectionManager connManager = new ConnectionManager(dsProvider.getDataSource());
         return connManager.exec(()-> { 
            JaversSqlRepository javersRepo = SqlRepositoryBuilder.sqlRepository()
                  .withConnectionProvider(connManager::getConnection)
                  .withDialect(DialectName.POSTGRES)
                  .build();
            
            Javers javers = JaversBuilder.javers().registerJaversRepository(javersRepo).build();
            return new ConnIsoJavers(javers, connManager);
         });
      }
      catch (SQLException e)
      {
         throw new IllegalStateException("Faild to instantiate Javers.", e);
      }
   }
   
   private class ConnectionManager
   {
      private final DataSource ds;
      private Connection conn;
      ConnectionManager(DataSource ds)
      {
         this.ds = ds;
      }
      
      public Connection getConnection()
      {
         if (conn == null)
            throw new IllegalStateException("No connection initialized.");
         
         return conn;
      }
      
      /**
       *  
       * @param fn
       * @return 
       * @throws RuntimeException If the supplied callable throws an exception. The original exception 
       *       will be wrapped as the cause of the thrown exception.
       */
      public synchronized <X> X exec(Callable<X> fn) throws RuntimeException
      {
         if (conn != null)
            throw new IllegalStateException("Execution is already in progress.");
         
         try (Connection c = ds.getConnection())
         {
            conn = c;
            return fn.call();
         }
         catch (Exception e)
         {
            throw new RuntimeException("Failed to execute supplied callable.", e);
         }
         finally
         {
            conn = null;
         }
      }
   }
   
   private class ConnIsoJavers implements Javers
   {
      
      private final Javers javers;
      private final ConnectionManager manager;

      ConnIsoJavers(Javers javers, ConnectionManager manager)
      {
         this.javers = javers;
         this.manager = manager;
      }

      @Override
      public Commit commit(String actorId, Object data)
      {
         return manager.exec(() -> 
           javers.commit(actorId, data));
      }

      @Override
      public Commit commit(String actorId, Object data, Map<String, String> commitProps)
      {
         return manager.exec(() -> javers.commit(actorId, data, commitProps));
      }

      @Override
      public Commit commitShallowDelete(String actorId, Object data)
      {
         return manager.exec(() -> javers.commitShallowDelete(actorId, data));
      }

      @Override
      public Commit commitShallowDelete(String actorId, Object data, Map<String, String> commitProps)
      {
         return manager.exec(() -> javers.commitShallowDelete(actorId, data, commitProps));
      }

      @Override
      public Commit commitShallowDeleteById(String actorId, GlobalIdDTO dataId)
      {
         return manager.exec(() -> javers.commitShallowDeleteById(actorId, dataId));
      }

      @Override
      public Commit commitShallowDeleteById(String actorId, GlobalIdDTO dataId, Map<String, String> commitProps)
      {
         return manager.exec(() -> javers.commitShallowDeleteById(actorId, dataId, commitProps));
      }

      @Override
      public Diff compare(Object oldData, Object newData)
      {
         return manager.exec(() -> javers.compare(oldData, newData));
      }

      @Override
      public <T> Diff compareCollections(Collection<T> oldData, Collection<T> newData, Class<T> entityClass)
      {
         return manager.exec(() -> javers.compareCollections(oldData, newData, entityClass));
      }

      @Override
      public List<Change> findChanges(JqlQuery query)
      {
         return manager.exec(() -> javers.findChanges(query));
      }

      @Override
      public List<CdoSnapshot> findSnapshots(JqlQuery query)
      {
         return manager.exec(() -> javers.findSnapshots(query));
      }

      @Override
      public JsonConverter getJsonConverter()
      {
         return manager.exec(() -> javers.getJsonConverter());
      }

      @Override
      public Optional<CdoSnapshot> getLatestSnapshot(Object dataId, Class entityClass)
      {
         return manager.exec(() -> javers.getLatestSnapshot(dataId, entityClass));
      }

      @Override
      public Property getProperty(PropertyChange propChange)
      {
         return manager.exec(() -> javers.getProperty(propChange));
      }

      @Override
      public <T extends JaversType> T getTypeMapping(Type type)
      {
         return manager.exec(() -> javers.getTypeMapping(type));
      }

      @Override
      public IdBuilder idBuilder()
      {
         return manager.exec(() -> javers.idBuilder());
      }

      @Override
      public Diff initial(Object data)
      {
         return manager.exec(() -> javers.initial(data));
      }

      @Override
      public <T> T processChangeList(List<Change> changes, ChangeProcessor<T> changeProcessor)
      {
         return manager.exec(() -> javers.processChangeList(changes, changeProcessor));
      }
   }
}
