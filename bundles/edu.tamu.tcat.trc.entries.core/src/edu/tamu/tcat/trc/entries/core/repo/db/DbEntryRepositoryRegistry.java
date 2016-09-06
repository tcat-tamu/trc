package edu.tamu.tcat.trc.entries.core.repo.db;

import java.net.URI;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import edu.tamu.tcat.account.Account;
import edu.tamu.tcat.db.exec.sql.SqlExecutor;
import edu.tamu.tcat.osgi.config.ConfigurationProperties;
import edu.tamu.tcat.trc.entries.core.repo.EntryRepositoryRegistry;
import edu.tamu.tcat.trc.entries.core.repo.RepositoryContext;
import edu.tamu.tcat.trc.entries.core.resolver.BasicResolverRegistry;
import edu.tamu.tcat.trc.entries.core.resolver.EntryResolver;
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
 *
 */
public class DbEntryRepositoryRegistry implements EntryRepositoryRegistry, RepositoryContext
{
//   NOTE This is really a PsqlJacksonRepoRegistry - may provide different flavors of repo or (perhaps better) allow
//        internal mechanisms to support different DB technologies.
//   NOTE that this introduces a dependency on this shared class (notably for registration of repos) that will
//        will be difficult to untangle. For the immediate future, this is acceptable in order to simplify
//        implementation, but the end result is that this class, effectively, becomes part of the API for the
//        repo framework and makes it difficult to provide alternate implementatoins. Mostly likely, we need to
//        split the API of this class to that they can be interchanged.

   private final EntryResolverRegistry resolverRegistry = new BasicResolverRegistry();
   private final ConcurrentHashMap<Class<? extends Object>, Function<Account, ?>> repositories = new ConcurrentHashMap<>();

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

   public void setConfiguration(ConfigurationProperties config)
   {
      this.config = config;
   }

   public void activate()
   {
      // TODO make the idFactoryProvider option and/or configure internally?
      Objects.requireNonNull(sqlExecutor);
      Objects.requireNonNull(idFactoryProvider);
      Objects.requireNonNull(config);
   }

   public void dispose()
   {

   }

   @Override
   public EntryResolverRegistry getResolverRegistry()
   {
      return resolverRegistry;
   }

   @Override
   public <T> EntryResolverRegistry.Registration registerResolver(EntryResolver<T> resolver)
   {
      return resolverRegistry.register(resolver);
   }

   @Override
   public <Repo> boolean isRepositoryAvailable(Class<Repo> type)
   {
      return repositories.containsKey(type);
   }

   @Override
   @SuppressWarnings("unchecked") // type safety maintained by registration process
   public <Repo> Repo getRepository(Account account, Class<Repo> type)
   {
      if (!isRepositoryAvailable(type))
         throw new IllegalArgumentException("No resolver has been registered for " + type);

      Function<Account, ?> factory = repositories.get(type);
      return (Repo)factory.apply(account);
   }

   @Override
   public ConfigurationProperties getConfig()
   {
      return config;
   }

   @Override
   public IdFactory getIdFactory(String context)
   {
      return idFactoryProvider.getIdFactory(context);
   }

   public SqlExecutor getSqlExecutor()
   {
      return sqlExecutor;
   }

   /**
    * @return The URI of the endpoint that will be used for URI based identification
    *       of entries. This should correspond to the REST endpoint on which the
    *       application is hosted.
    */
   @Override
   public URI getApiEndpoint()
   {
      return config.getPropertyValue(API_ENDPOINT_PARAM, URI.class, URI.create(""));
   }

   /**
    * Builds a basic document repository using the supplied configuration utilities.
    *
    * @param tablename The name of the database table (or other storage specific mechanism)
    *       for storing recording these entries. Note that if the underlying table does not
    *       exist, it will be created.
    * @param factory A factory for generating edit commands.
    * @param adapter An adapter to convert stored data representations (type DTO) to
    *       instances of the associated entries Java type (an Interface).
    * @param type The Java interface that defines this entry.
    *
    * @return A document repository with the supplied configuration.
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
   public <Repo> void registerRepository(Class<Repo> type, Function<Account, Repo> factory)
   {
      if (repositories.containsKey(type))
            throw new IllegalArgumentException("A repository has already been registered for " + type);

      repositories.put(type, factory);
   }

   @Override
   public <Repo> void unregister(Class<Repo> type)
   {
      repositories.remove(type);
   }
}
