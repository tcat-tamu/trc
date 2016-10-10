package edu.tamu.tcat.trc.impl.psql.services.seealso.repo;

import static java.text.MessageFormat.format;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.tamu.tcat.db.exec.sql.SqlExecutor;
import edu.tamu.tcat.trc.impl.psql.services.seealso.model.LinkImpl;
import edu.tamu.tcat.trc.repo.db.ColumnDefinition;
import edu.tamu.tcat.trc.repo.db.ColumnDefinition.ColumnType;
import edu.tamu.tcat.trc.repo.db.TableDefinition;
import edu.tamu.tcat.trc.services.TrcServiceException;
import edu.tamu.tcat.trc.services.seealso.Link;
import edu.tamu.tcat.trc.services.seealso.repo.SeeAlsoService;

public class SeeAlsoServiceImpl implements SeeAlsoService
{
   private static final Logger logger = Logger.getLogger(SeeAlsoServiceImpl.class.getName());

   private static final String COLUMN_SOURCE = "source";
   private static final String COLUMN_TARGET = "target";

   private final String tableName;
   private final SqlExecutor sqlExecutor;

   public SeeAlsoServiceImpl(String tableName, SqlExecutor sqlExecutor)
   {
      this.tableName = tableName;
      this.sqlExecutor = sqlExecutor;
   }

   @Override
   public Link create(String source, String target)
   {
      if (!isRelated(source, target))
      {
         CompletableFuture<?> result = sqlExecutor.submit(conn -> {
            String sql = format("INSERT INTO {2} ({0}, {1}) VALUES (?, ?)", COLUMN_SOURCE, COLUMN_TARGET, tableName);
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(0, source);
            ps.setString(1, target);
            ps.executeUpdate();
            return null;
         });

         // wait for statement to complete
         try
         {
            result.get(10, TimeUnit.SECONDS);
         }
         catch (InterruptedException | ExecutionException | TimeoutException e)
         {
            throw new TrcServiceException("Unable to insert new 'see also' record.", e);
         }
      }

      return new LinkImpl(source, target);
   }

   @Override
   public boolean isRelated(String source, String target)
   {
      CompletableFuture<Boolean> result = sqlExecutor.submit(conn -> {
         String sql = format("SELECT COUNT(*) FROM {2} WHERE {0} = ? AND {1} = ?", COLUMN_SOURCE, COLUMN_TARGET, tableName);
         PreparedStatement ps = conn.prepareStatement(sql);
         ps.setString(0, source);
         ps.setString(1, target);
         ResultSet resultSet = ps.executeQuery();
         int count = resultSet.getInt(0);

         if (count > 1)
         {
            logger.log(Level.SEVERE, format("more than one record for link ({0}, {1}).", source, target));
         }

         return Boolean.valueOf(count == 1);
      });

      try
      {
         Boolean boolResult = result.get();
         return Objects.requireNonNull(boolResult, "SQL execution failed to return a result").booleanValue();
      }
      catch (InterruptedException | ExecutionException e)
      {
         throw new TrcServiceException(format("unable to determine whether record ({0}, {1}) exists", source, target), e);
      }
   }

   @Override
   public Collection<Link> getFor(String id)
   {
      CompletableFuture<Collection<Link>> result = sqlExecutor.submit(conn -> {
         String sql = format("SELECT {0}, {1} FROM {2} WHERE {0} = ? OR {1} = ?", COLUMN_SOURCE, COLUMN_TARGET, tableName);
         PreparedStatement ps = conn.prepareStatement(sql);
         ps.setString(0, id);
         ps.setString(1, id);
         ResultSet resultSet = ps.executeQuery();
         return hydrateModels(resultSet);
      });

      try
      {
         return Objects.requireNonNull(result.get(), "SQL execution failed to return a result");
      }
      catch (InterruptedException | ExecutionException e)
      {
         throw new TrcServiceException(format("unable to find records ({0}, *) || (*, {0})", id), e);
      }
   }

   @Override
   public Collection<Link> getFrom(String source)
   {
      CompletableFuture<Collection<Link>> result = sqlExecutor.submit(conn -> {
         String sql = format("SELECT {0}, {1} FROM {2} WHERE {0} = ?", COLUMN_SOURCE, COLUMN_TARGET, tableName);
         PreparedStatement ps = conn.prepareStatement(sql);
         ps.setString(0, source);
         ResultSet resultSet = ps.executeQuery();
         return hydrateModels(resultSet);
      });

      try
      {
         return Objects.requireNonNull(result.get(), "SQL execution failed to return a result");
      }
      catch (InterruptedException | ExecutionException e)
      {
         throw new TrcServiceException(format("unable to find records ({0}, *)", source), e);
      }
   }

   @Override
   public Collection<Link> getTo(String target)
   {
      CompletableFuture<Collection<Link>> result = sqlExecutor.submit(conn -> {
         String sql = format("SELECT {0}, {1} FROM {2} WHERE {1} = ?", COLUMN_SOURCE, COLUMN_TARGET, tableName);
         PreparedStatement ps = conn.prepareStatement(sql);
         ps.setString(0, target);
         ResultSet resultSet = ps.executeQuery();
         return hydrateModels(resultSet);
      });

      try
      {
         return Objects.requireNonNull(result.get(), "SQL execution failed to return a result");
      }
      catch (InterruptedException | ExecutionException e)
      {
         throw new TrcServiceException(format("unable to find records (*, {0})", target), e);
      }
   }

   @Override
   public boolean delete(String source, String target)
   {
      CompletableFuture<Boolean> result = sqlExecutor.submit(conn -> {
         String sql = format("DELETE FROM {2} WHERE {0} = ? AND {1} = ?", COLUMN_SOURCE, COLUMN_TARGET, tableName);
         PreparedStatement ps = conn.prepareStatement(sql);
         ps.setString(0, source);
         ps.setString(1, target);
         int updated = ps.executeUpdate();
         return Boolean.valueOf(updated > 0);
      });

      try
      {
         return Objects.requireNonNull(result.get(), "SQL execution failed to return a result").booleanValue();
      }
      catch (InterruptedException | ExecutionException e)
      {
         throw new TrcServiceException(format("unable to delete record ({0}, {1})", source, target), e);
      }
   }

   @Override
   public boolean delete(String id)
   {
      CompletableFuture<Boolean> result = sqlExecutor.submit(conn -> {
         String sql = format("DELETE FROM {2} WHERE {0} = ? OR {1} = ?", COLUMN_SOURCE, COLUMN_TARGET, tableName);
         PreparedStatement ps = conn.prepareStatement(sql);
         ps.setString(0, id);
         ps.setString(1, id);
         int updated = ps.executeUpdate();
         return Boolean.valueOf(updated > 0);
      });

      try
      {
         return Objects.requireNonNull(result.get(), "SQL execution failed to return a result").booleanValue();
      }
      catch (InterruptedException | ExecutionException e)
      {
         throw new TrcServiceException(format("unable to delete records ({0}, *) || (*, {0})", id), e);
      }
   }

   /**
    * @param resultSet
    * @return
    * @throws SQLException
    * @throws InterruptedException
    */
   private static Collection<Link> hydrateModels(ResultSet resultSet) throws SQLException, InterruptedException
   {
      Collection<Link> links = new ArrayList<>();

      while (resultSet.next())
      {
         if (Thread.interrupted())
            throw new InterruptedException();

         String source = resultSet.getString(COLUMN_SOURCE);
         String target = resultSet.getString(COLUMN_TARGET);
         links.add(new LinkImpl(source, target));
      }

      return links;
   }

   public static TableDefinition getTableDefinition(String tableName)
   {
      TableDefinition.Builder tableDefinitionBuilder = new TableDefinition.Builder();

      tableDefinitionBuilder.setName(tableName);
      tableDefinitionBuilder.addColumn(makeStringColumn(COLUMN_SOURCE));
      tableDefinitionBuilder.addColumn(makeStringColumn(COLUMN_TARGET));

      return tableDefinitionBuilder.build();
   }

   private static ColumnDefinition makeStringColumn(String columnName)
   {
      ColumnDefinition.Builder columnDefinitionBuilder = new ColumnDefinition.Builder();

      columnDefinitionBuilder.setName(columnName);
      columnDefinitionBuilder.setType(ColumnType.varchar);
      columnDefinitionBuilder.setSize(255);
      columnDefinitionBuilder.notNull();

      return columnDefinitionBuilder.build();
   }
}
