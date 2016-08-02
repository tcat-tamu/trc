package edu.tamu.tcat.trc.categorization.impl;

import static java.text.MessageFormat.format;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.tamu.tcat.account.Account;
import edu.tamu.tcat.db.exec.sql.SqlExecutor;
import edu.tamu.tcat.trc.categorization.CategorizationRepo;
import edu.tamu.tcat.trc.categorization.CategorizationRepoFactory;
import edu.tamu.tcat.trc.categorization.CategorizationScheme;
import edu.tamu.tcat.trc.categorization.CategorizationScheme.Strategy;
import edu.tamu.tcat.trc.categorization.CategorizationScope;
import edu.tamu.tcat.trc.categorization.EditCategorizationCommand;
import edu.tamu.tcat.trc.categorization.strategies.tree.EditTreeCategorizationCommand;
import edu.tamu.tcat.trc.categorization.strategies.tree.TreeCategorization;
import edu.tamu.tcat.trc.entries.core.EntryResolverRegistry;
import edu.tamu.tcat.trc.repo.BasicSchemaBuilder;
import edu.tamu.tcat.trc.repo.DocumentRepository;
import edu.tamu.tcat.trc.repo.IdFactory;
import edu.tamu.tcat.trc.repo.IdFactoryProvider;
import edu.tamu.tcat.trc.repo.RepositorySchema;
import edu.tamu.tcat.trc.repo.SchemaBuilder;
import edu.tamu.tcat.trc.repo.postgres.PsqlJacksonRepoBuilder;

/**
 *
 *
 */
public class CategorizationSchemeService implements CategorizationRepoFactory
{
   private final static Logger logger = Logger.getLogger(CategorizationSchemeService.class.getName());

   public static final String ID_CONTEXT_SCHEMES = "trc.services.categorization.schemes.ids";
   public static final String ID_CONTEXT_NODES = "trc.services.categorization.nodes.ids";

   private static final String TABLE_NAME = "categorizations";
   private static final String SCHEMA_ID = "taxonomy";
   private static final String SCHEMA_DATA_FIELD = "hierarchy";

   /**
    * The configuration properties key uses to supply an id factory context
    * for generating categorization scheme IDs.
    */
   public static final String PARAM_ID_CTX = "scheme_id_ctx";

   /**
    * The configuration properties key uses to supply an id factory context
    * for generating categorization node IDs.
    */
   public static final String PARAM_NODE_CTX = "node_id_ctx";

   /**
    * The configuration properties key uses to supply an database table name
    */
   public static final String PARAM_TABLE_NAME = "table_name";


   private DocumentRepository<TreeCategorization, PersistenceModelV1.TreeCategorizationStrategy, EditTreeCategorizationCommand> treeRepo;

   private SqlExecutor sqlExecutor;

   private IdFactoryProvider idFactoryProvider;

   private IdFactory nodeIds;
   private IdFactory schemeIds;

   private EntryResolverRegistry registry;


   public void bindSqlExecutor(SqlExecutor sqlExecutor)
   {
      this.sqlExecutor = sqlExecutor;
   }

   public void bindIdProvider(IdFactoryProvider idProvider)
   {
      this.idFactoryProvider = idProvider;
   }

   public void bindEntityResolver(EntryResolverRegistry registry)
   {
      this.registry = registry;
   }

   public void activate(Map<String, Object> props)
   {
      try
      {
         logger.info("Activating CategorizationSchemeService");
         Objects.requireNonNull(sqlExecutor);
         Objects.requireNonNull(idFactoryProvider);
         Objects.requireNonNull(sqlExecutor);

         String schemeIdsCtx = (String)props.getOrDefault(PARAM_ID_CTX, ID_CONTEXT_SCHEMES);
         logger.fine(() -> format("Categorization scheme id context {0}", schemeIdsCtx));
         schemeIds = this.idFactoryProvider.getIdFactory(schemeIdsCtx);

         String nodeIdsCtx = (String)props.getOrDefault(PARAM_NODE_CTX, ID_CONTEXT_NODES);
         logger.fine(() -> format("Categorization nodes id context {0}", nodeIdsCtx));
         nodeIds = this.idFactoryProvider.getIdFactory(nodeIdsCtx);

         // TODO could add a mapping repo to track metadata about entries by id/scope/key/type, etc.

         String tableName = (String)props.getOrDefault(PARAM_TABLE_NAME, TABLE_NAME);
         buildTreeDocRepo(tableName);
//         buildListDocRepo(tableName);
//         buildSetDocRepo(tableName);
      }
      catch (Exception ex)
      {
         logger.log(Level.SEVERE, "Failed to activate CategorizationSchemeService", ex);
         throw ex;
      }
   }

   public void deactivate()
   {
      sqlExecutor = null;
   }

   private void buildTreeDocRepo(String tableName)
   {
      PsqlJacksonRepoBuilder<TreeCategorization,
                             PersistenceModelV1.TreeCategorizationStrategy,
                             EditTreeCategorizationCommand>
      repoBuilder = new PsqlJacksonRepoBuilder<>();


      repoBuilder.setDbExecutor(sqlExecutor);
      repoBuilder.setTableName(tableName);
      repoBuilder.setSchema(buildSchema());
      repoBuilder.setEnableCreation(true);

      repoBuilder.setEditCommandFactory(new EditHeirarchyCommandFactory(nodeIds));
      repoBuilder.setDataAdapter(dto -> PersistenceModelV1Adapter.toDomainModel(registry, dto));
      repoBuilder.setStorageType(PersistenceModelV1.TreeCategorizationStrategy.class);

      treeRepo = repoBuilder.build();
   }

   /**
    * @return The repository schema
    */
   private RepositorySchema buildSchema()
   {
      SchemaBuilder schemaBuilder = new BasicSchemaBuilder();
      schemaBuilder.setId(SCHEMA_ID);
      schemaBuilder.setDataField(SCHEMA_DATA_FIELD);
      return schemaBuilder.build();
   }

   @Override
   public CategorizationScope createScope(Account account, String scopeId)
   {
      return new BasicScope(account, scopeId);
   }

   @Override
   public CategorizationRepo getRepository(CategorizationScope scope)
   {
      return new CategorizationRepoImpl(scope);
   }

   private static class BasicScope implements CategorizationScope
   {
      private final Account account;
      private final String scopeId;

      public BasicScope(Account account, String scopeId)
      {
         this.account = account;
         this.scopeId = scopeId;
      }

      @Override
      public String getScopeId()
      {
         return scopeId;
      }

      @Override
      public Account getAccount()
      {
         return account;
      }

      @Override
      public int hashCode()
      {
         int result = 17;
         result = 31 * result + scopeId.hashCode();
         result = 31 * result +
                  ((account != null) ? account.getId().hashCode() : 0);

         return result;
      }

      @Override
      public boolean equals(Object obj)
      {
         if (!CategorizationScope.class.isInstance(obj))
            return false;

         CategorizationScope scope = (CategorizationScope)obj;
         String id = scope.getScopeId();

         if (!Objects.equals(this.scopeId, id))
            return false;

         Account account = scope.getAccount();
         if (account != null)
         {
            UUID accountId = account.getId();
            return this.account != null ? this.account.getId().equals(accountId) : false;
         }
         else
         {
            return this.account == null;
         }
      }


      @Override
      public String toString()
      {
         String template = "Categorization scope: {0} (Held by account {1})";
         UUID accountId = account != null ? account.getId() : null;
         return MessageFormat.format(template, scopeId, accountId);
      }
   }

   private class CategorizationRepoImpl implements CategorizationRepo
   {
      private final CategorizationScope scope;

      public CategorizationRepoImpl(CategorizationScope scope)
      {
         this.scope = scope;
      }

      @Override
      public CategorizationScope getScope()
      {
         return scope;
      }

      @Override
      public CategorizationScheme get(String key) throws IllegalArgumentException
      {
         // TODO figure out what type it is.
         // TODO Auto-generated method stub
         return null;
      }

      @Override
      public CategorizationScheme getById(String id) throws IllegalArgumentException
      {
         // TODO Auto-generated method stub
         return null;
      }

      private CategorizationScheme.Strategy getStrategyForPrefix(char prefix)
      {
         switch (prefix) {
         case 't': return CategorizationScheme.Strategy.TREE;
         case 'l': return CategorizationScheme.Strategy.LIST;
         case 's': return CategorizationScheme.Strategy.SET;
         default:
            throw new IllegalArgumentException("Unsupported categorization strategy prefix: " + prefix);
         }
      }

      private String getIdPrefix(CategorizationScheme.Strategy strategy)
      {
         switch (strategy)
         {
            case TREE: return "t";
            case SET: return "s";
            case LIST: return "l";
            default:
               throw new IllegalArgumentException("Unsupported categorization strategy: " + strategy);

         }
      }

      @Override
      public EditCategorizationCommand create(CategorizationScheme.Strategy strategy, String key)
      {
         // HACK: add prefix to simplify lookup of strategies.
         Map<CategorizationScheme.Strategy, String> prefixes = new HashMap<>();
         prefixes.put(CategorizationScheme.Strategy.TREE, "t");
         prefixes.get(strategy);

         String id = getIdPrefix(strategy) + "_" + schemeIds.get();
         switch (strategy)
         {
            case TREE:
               return treeRepo.create(id);
            case SET:
               throw new UnsupportedOperationException("Set categorizations are not yet supported");
            case LIST:
               throw new UnsupportedOperationException("List categorizations are not yet supported");
            default:
               throw new IllegalArgumentException("Unsupported categorization strategy: " + strategy);

         }
      }

      @Override
      public EditCategorizationCommand edit(String id)
      {
         Strategy strategy = getStrategyForPrefix(id.charAt(0));
         switch (strategy)
         {
            case TREE:
               return treeRepo.edit(id);
            case SET:
               throw new UnsupportedOperationException("Set categorizations are not yet supported");
            case LIST:
               throw new UnsupportedOperationException("List categorizations are not yet supported");
            default:
               throw new IllegalArgumentException("Unsupported categorization strategy: " + strategy);
         }
      }

      @Override
      public CompletableFuture<Boolean> remove(String id)
      {
         // TODO Auto-generated method stub
         return null;
      }
   }
}