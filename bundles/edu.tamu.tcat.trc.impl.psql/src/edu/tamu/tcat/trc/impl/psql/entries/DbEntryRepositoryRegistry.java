package edu.tamu.tcat.trc.impl.psql.entries;

import java.net.URI;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import edu.tamu.tcat.account.Account;
import edu.tamu.tcat.db.exec.sql.SqlExecutor;
import edu.tamu.tcat.osgi.config.ConfigurationProperties;
import edu.tamu.tcat.trc.ConfigParams;
import edu.tamu.tcat.trc.entries.core.repo.EntryRepositoryRegistrar;
import edu.tamu.tcat.trc.entries.core.repo.EntryRepositoryRegistry;
import edu.tamu.tcat.trc.repo.id.IdFactory;
import edu.tamu.tcat.trc.repo.id.IdFactoryProvider;
import edu.tamu.tcat.trc.repo.postgres.PsqlJacksonRepoBuilder;
import edu.tamu.tcat.trc.resolver.BasicResolverRegistry;
import edu.tamu.tcat.trc.resolver.EntryResolver;
import edu.tamu.tcat.trc.resolver.EntryResolverRegistrar;
import edu.tamu.tcat.trc.resolver.EntryResolverRegistry;

/**
 *  Provides a unified service for accessing the service dependencies that are
 *  common across repositories.
 *
 */
public class DbEntryRepositoryRegistry implements EntryRepositoryRegistry, EntryRepositoryRegistrar
{
//   NOTE This is really a PsqlJacksonRepoRegistry - may provide different flavors of repo or (perhaps better) allow
//        internal mechanisms to support different DB technologies.
//   NOTE that this introduces a dependency on this shared class (notably for registration of repos) that will
//        will be difficult to untangle. For the immediate future, this is acceptable in order to simplify
//        implementation, but the end result is that this class, effectively, becomes part of the API for the
//        repo framework and makes it difficult to provide alternate implementatoins. Mostly likely, we need to
//        split the API of this class to that they can be interchanged.

   private final BasicResolverRegistry resolverRegistry = new BasicResolverRegistry();
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
   public <T> EntryResolverRegistrar.Registration registerResolver(EntryResolver<T> resolver)
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
      return config.getPropertyValue(ConfigParams.API_ENDPOINT_PARAM, URI.class, URI.create(""));
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
   public <T, DTO, CMD> PsqlJacksonRepoBuilder<T, DTO, CMD> getDocRepoBuilder()
   {
      PsqlJacksonRepoBuilder<T, DTO, CMD> repoBuilder = new PsqlJacksonRepoBuilder<>();

      repoBuilder.setDbExecutor(sqlExecutor);
      return repoBuilder;
   }

   @Override
   public <Repo> EntryRepositoryRegistrar.Registration registerRepository(Class<Repo> type, Function<Account, Repo> factory)
   {
      if (repositories.containsKey(type))
            throw new IllegalArgumentException("A repository has already been registered for " + type);

      repositories.put(type, factory);

      return () -> {
         repositories.remove(type);
      };
   }
}
