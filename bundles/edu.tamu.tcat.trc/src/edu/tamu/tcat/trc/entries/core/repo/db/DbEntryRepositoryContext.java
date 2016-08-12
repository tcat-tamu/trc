package edu.tamu.tcat.trc.entries.core.repo.db;

import java.util.Objects;
import java.util.function.Function;

import edu.tamu.tcat.account.Account;
import edu.tamu.tcat.db.exec.sql.SqlExecutor;
import edu.tamu.tcat.osgi.config.ConfigurationProperties;
import edu.tamu.tcat.trc.entries.core.repo.EntryRepositoryContext;
import edu.tamu.tcat.trc.entries.core.repo.EntryRepositoryRegistry;
import edu.tamu.tcat.trc.entries.core.resolver.EntryResolverRegistry;
import edu.tamu.tcat.trc.repo.BasicSchemaBuilder;
import edu.tamu.tcat.trc.repo.DocumentRepository;
import edu.tamu.tcat.trc.repo.EditCommandFactory;
import edu.tamu.tcat.trc.repo.IdFactory;
import edu.tamu.tcat.trc.repo.IdFactoryProvider;
import edu.tamu.tcat.trc.repo.postgres.PsqlJacksonRepoBuilder;

/**
 *  Provides a unified service for accessing the service dependencies that are
 *  common across repositories.
 */
public class DbEntryRepositoryContext implements EntryRepositoryContext
{

   private EntryResolverRegistry resolverRegistry;

   private SqlExecutor sqlExecutor;
   private IdFactoryProvider idFactoryProvider;
   private ConfigurationProperties config;

   // TODO add version history
   //      start up other service?

   /**
    * Bind method for SQL executor service dependency (usually called by dependency injection layer)
    *
    * @param sqlExecutor
    */
   public void setSqlExecutor(SqlExecutor sqlExecutor)
   {
      this.sqlExecutor = sqlExecutor;
   }

   /**
    * Bind method for ID factory provider service dependency (usually called by dependency injection layer)
    *
    * @param idFactory
    */
   public void setIdFactory(IdFactoryProvider idFactoryProvider)
   {
      this.idFactoryProvider = idFactoryProvider;
   }

   public void setResolverRegistry(EntryResolverRegistry reg)
   {
      this.resolverRegistry = reg;
   }

   public void setConfiguration(ConfigurationProperties config)
   {
      this.config = config;
   }

   public void activate()
   {
      Objects.requireNonNull(idFactoryProvider);
   }

   public void dispose()
   {

   }

   @Override
   public IdFactory getIdFactory(String context)
   {
      return idFactoryProvider.getIdFactory(context);
   }

   @Override
   public SqlExecutor getSqlExecutor()
   {
      return sqlExecutor;
   }

   @Override
   public ConfigurationProperties getConfig()
   {
      return config;
   }

   @Override
   public EntryResolverRegistry getResolverRegistry()
   {
      return resolverRegistry;
   }

   /**
    *
    * @param tablename
    * @param factory
    * @param adapter
    * @param type
    *
    * @return
    */
   @Override
   public <T, DTO, CMD> DocumentRepository<T, DTO, CMD>
   buildDocumentRepo(String tablename, EditCommandFactory<DTO, CMD> factory, Function<DTO, T> adapter, Class<DTO> type)
   {
      PsqlJacksonRepoBuilder<T, DTO, CMD> repoBuilder = new PsqlJacksonRepoBuilder<>();

      repoBuilder.setDbExecutor(sqlExecutor);
      repoBuilder.setTableName(tablename);
      repoBuilder.setEditCommandFactory(factory);
      repoBuilder.setDataAdapter(adapter);
      repoBuilder.setSchema(BasicSchemaBuilder.buildDefaultSchema());
      repoBuilder.setStorageType(type);
      repoBuilder.setEnableCreation(true);

      return repoBuilder.build();
   }

   @Override
   public EntryRepositoryRegistry getRepositoryRegistry()
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public <Repo> void register(Class<Repo> type, Function<Account, Repo> factory)
   {
      // TODO Auto-generated method stub

   }
}
