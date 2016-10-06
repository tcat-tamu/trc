package edu.tamu.tcat.trc.impl.psql.services.categorization;

import static java.text.MessageFormat.format;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.tamu.tcat.account.Account;
import edu.tamu.tcat.db.exec.sql.SqlExecutor;
import edu.tamu.tcat.trc.ResourceNotFoundException;
import edu.tamu.tcat.trc.impl.psql.entries.DbEntryRepositoryRegistry;
import edu.tamu.tcat.trc.impl.psql.services.ServiceFactory;
import edu.tamu.tcat.trc.impl.psql.services.categorization.model.TreeCategorizationImpl;
import edu.tamu.tcat.trc.impl.psql.services.categorization.repo.BaseEditCommand;
import edu.tamu.tcat.trc.impl.psql.services.categorization.repo.EditHeirarchyCommandFactory;
import edu.tamu.tcat.trc.impl.psql.services.categorization.repo.PersistenceModelV1;
import edu.tamu.tcat.trc.repo.BasicSchemaBuilder;
import edu.tamu.tcat.trc.repo.DocumentRepository;
import edu.tamu.tcat.trc.repo.RepositorySchema;
import edu.tamu.tcat.trc.repo.SchemaBuilder;
import edu.tamu.tcat.trc.repo.id.IdFactory;
import edu.tamu.tcat.trc.repo.postgres.PsqlJacksonRepo;
import edu.tamu.tcat.trc.repo.postgres.PsqlJacksonRepoBuilder;
import edu.tamu.tcat.trc.resolver.EntryReference;
import edu.tamu.tcat.trc.resolver.EntryResolverRegistry;
import edu.tamu.tcat.trc.services.ServiceContext;
import edu.tamu.tcat.trc.services.categorization.CategorizationScheme;
import edu.tamu.tcat.trc.services.categorization.CategorizationService;
import edu.tamu.tcat.trc.services.categorization.EditCategorizationCommand;
import edu.tamu.tcat.trc.services.categorization.strategies.tree.EditTreeCategorizationCommand;
import edu.tamu.tcat.trc.services.categorization.strategies.tree.TreeCategorization;

/**
 *
 */
public class CategorizationServiceFactory implements ServiceFactory<CategorizationService>
{
   private final static Logger logger = Logger.getLogger(CategorizationServiceFactory.class.getName());

   public static final String ID_CONTEXT_SCHEMES = "trc.services.categorization.schemes.ids";
   public static final String ID_CONTEXT_NODES = "trc.services.categorization.nodes.ids";

   private static final String TABLE_NAME = "categorizations";
   private static final String SCHEMA_ID = "taxonomy";
   private static final String SCHEMA_DATA_FIELD = "doc";

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

   private PsqlJacksonRepo<TreeCategorization, PersistenceModelV1.TreeCategorizationStrategy, EditTreeCategorizationCommand> treeRepo;

   private IdFactory nodeIds;
   private IdFactory schemeIds;

   private String tableName;

   private final DbEntryRepositoryRegistry repoRegistry;

   public CategorizationServiceFactory(DbEntryRepositoryRegistry repoRegistry)
   {
      this.repoRegistry = repoRegistry;
      this.activate(new HashMap<>());     // HACK: need to setup props.

   }

   private void activate(Map<String, Object> props)
   {
      // FIXME get from config properties following standard model
      try
      {
         logger.info("Activating CategorizationSchemeService");

         String schemeIdsCtx = (String)props.getOrDefault(PARAM_ID_CTX, ID_CONTEXT_SCHEMES);
         logger.fine(() -> format("Categorization scheme id context {0}", schemeIdsCtx));
         schemeIds = this.repoRegistry.getIdFactory(schemeIdsCtx);

         String nodeIdsCtx = (String)props.getOrDefault(PARAM_NODE_CTX, ID_CONTEXT_NODES);
         logger.fine(() -> format("Categorization nodes id context {0}", nodeIdsCtx));
         nodeIds = this.repoRegistry.getIdFactory(nodeIdsCtx);

         // TODO could add a mapping repo to track metadata about entries by id/scope/key/type, etc.

         tableName = (String)props.getOrDefault(PARAM_TABLE_NAME, TABLE_NAME);
         buildTreeDocRepo();
//         buildListDocRepo(tableName);
//         buildSetDocRepo(tableName);
      }
      catch (Exception ex)
      {
         logger.log(Level.SEVERE, "Failed to activate CategorizationSchemeService", ex);
         throw ex;
      }
   }

   @Override
   public Class<CategorizationService> getType()
   {
      return CategorizationService.class;
   }

   @Override
   public void shutdown()
   {
      treeRepo.dispose();
   }

   /**
    * Note that the returned {@link TreeCategorizationImpl} MUST have the associated
    * categorization set prior to access.
    *
    * @param registry
    * @param dto
    * @return
    */
   private static TreeCategorizationImpl toDomainModel(EntryResolverRegistry registry,
                                                      PersistenceModelV1.TreeCategorizationStrategy dto)
   {
      return new TreeCategorizationImpl(dto, registry);
   }

   public static EntryReference copy(EntryReference ref)
   {
      if (ref == null)
         return null;

      EntryReference dto = new EntryReference();
      dto.id = ref.id;
      dto.type = ref.type;

      return dto;
   }

   private void buildTreeDocRepo()
   {
      PsqlJacksonRepoBuilder<TreeCategorization, PersistenceModelV1.TreeCategorizationStrategy, EditTreeCategorizationCommand> repoBuilder = repoRegistry.getDocRepoBuilder();
      repoBuilder.setTableName(tableName);
      repoBuilder.setSchema(buildSchema());
      repoBuilder.setEnableCreation(true);

      EntryResolverRegistry resolvers = repoRegistry.getResolverRegistry();
      repoBuilder.setEditCommandFactory(new EditHeirarchyCommandFactory(resolvers, nodeIds));
      repoBuilder.setDataAdapter(dto -> toDomainModel(resolvers, dto));
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
   public CategorizationService getService(ServiceContext<CategorizationService> ctx)
   {
      return new CategorizationRepoImpl(ctx);
   }

   // TODO this needs to be worked so that categorization implementations can be registered
   //      and we can avoid the myriad switch statements
   private class CategorizationRepoImpl implements CategorizationService
   {
      private final ServiceContext<CategorizationService> svcContext;
      private final String scopeId;
      private final Optional<Account> account;

      public CategorizationRepoImpl(ServiceContext<CategorizationService> scope)
      {
         this.svcContext = scope;
         this.scopeId = (String)scope.getProperty(CTX_SCOPE_ID);
         this.account = scope.getAccount();
      }

      @Override
      public ServiceContext<CategorizationService> getContext()
      {
         return svcContext;
      }

      @Override
      public String getScopeId()
      {
         return scopeId;
      }

      @Override
      public Optional<? extends CategorizationScheme> get(String key)
      {
         // TODO provide API on DocRepo to support more robust WHERE queries and projections
         // paired single quotes ('') are required due to escaping performed by MessageFormat
         String sqlTemplate = "SELECT {0} AS json, {0}->>''strategy'' AS strategy"
                             + " FROM {1} "
                             + "WHERE {0}->>''key'' = ? "
                               + "AND {0}->>''scopeId'' = ? "
                               + treeRepo.buildNotRemovedClause();

         SqlExecutor sqlExecutor = repoRegistry.getSqlExecutor();
         String sql = format(sqlTemplate, SCHEMA_DATA_FIELD, tableName);
         Future<Optional<CategorizationScheme>> future = sqlExecutor.submit(
               (conn) -> doGetByKey(key, sql, conn));

         String internalErr = "Failed to retrieve categorization scheme for key {0} within scope {1}";

         return DocumentRepository.unwrap(future, () -> format(internalErr, key, scopeId));
      }

      private Optional<CategorizationScheme> doGetByKey(String key, String sql, Connection conn) throws SQLException
      {
         try (PreparedStatement ps = conn.prepareStatement(sql))
         {
            ps.setString(1, key);
            ps.setString(2, scopeId);

            ResultSet rs = ps.executeQuery();
            if (!rs.next())
               return Optional.empty();

            String json = rs.getString("json");
            String s = rs.getString("strategy");

            CategorizationScheme scheme = parseCategorizationScheme(json, s);
            return Optional.of(scheme);
         }
      }

      private CategorizationScheme parseCategorizationScheme(String json, String s)
      {
         try
         {
            CategorizationScheme.Strategy strategy = CategorizationScheme.Strategy.valueOf(s);
            switch (strategy)
            {
               case TREE:
                  return parseTreeCategorization(json);
               case SET:
                  // TODO add support for sets
                  throw new UnsupportedOperationException("Set categorizations are not yet supported");
               case LIST:
                  // TODO add support for lists
                  throw new UnsupportedOperationException("List categorizations are not yet supported");
               default:
                  throw new IllegalArgumentException("Unsupported categorization strategy: " + strategy);
            }
         }
         catch (Exception ex)
         {
            throw new IllegalStateException("Failed to parse stored JSON data for categorition scheme: \n" + json , ex);
         }
      }

      private CategorizationScheme parseTreeCategorization(String json) throws IOException
      {
         ObjectMapper mapper = new ObjectMapper();
         EntryResolverRegistry resolvers = repoRegistry.getResolverRegistry();

         PersistenceModelV1.TreeCategorizationStrategy dto = mapper.readValue(json, PersistenceModelV1.TreeCategorizationStrategy.class);
         TreeCategorizationImpl impl = toDomainModel(resolvers, dto);
         impl.setContext(svcContext);
         return impl;
      }

      @Override
      public Optional<? extends CategorizationScheme> getById(String id)
      {
         Optional<TreeCategorizationImpl> optional = treeRepo.get(id)
               .filter(TreeCategorizationImpl.class::isInstance)
               .map(TreeCategorizationImpl.class::cast);

         optional.ifPresent(scheme -> {
            scheme.setContext(svcContext);
            validateScheme(scheme, id);
         });

         return optional.map(CategorizationScheme.class::cast);
      }

      private void validateScheme(TreeCategorizationImpl scheme, String id)
      {
         String notFoundErr = "The requested categorization scheme [{0}] is not accessible within from {1}";

         if (!scheme.getScopeId().equals(scopeId))
            throw new ResourceNotFoundException(format(notFoundErr, id, svcContext));

         // TODO verify that this is within the correct scope and that the
         //      user is authorized to access it
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

         EditCategorizationCommand cmd = null;
         String id = getIdPrefix(strategy) + "_" + schemeIds.get();
         switch (strategy)
         {
            case TREE:
               cmd = treeRepo.create(svcContext.getAccount().orElse(null), id);
               break;
            case SET:
               // TODO add support for sets
               throw new UnsupportedOperationException("Set categorizations are not yet supported");
            case LIST:
               // TODO add support for lists
               throw new UnsupportedOperationException("List categorizations are not yet supported");
            default:
               throw new IllegalArgumentException("Unsupported categorization strategy: " + strategy);

         }

         ((BaseEditCommand<?>)cmd).setContext(svcContext);
         cmd.setKey(key);
         return cmd;
      }

      @Override
      public EditCategorizationCommand edit(String id)
      {
         EditCategorizationCommand cmd = null;
         CategorizationScheme.Strategy strategy = getStrategyForPrefix(id.charAt(0));
         switch (strategy)
         {
            case TREE:
               cmd = treeRepo.edit(svcContext.getAccount().orElse(null), id);
               break;
            case SET:
               // TODO add support for sets
               throw new UnsupportedOperationException("Set categorizations are not yet supported");
            case LIST:
               // TODO add support for lists
               throw new UnsupportedOperationException("List categorizations are not yet supported");
            default:
               throw new IllegalArgumentException("Unsupported categorization strategy: " + strategy);
         }

         ((BaseEditCommand<?>)cmd).setContext(svcContext);
         return cmd;
      }

      @Override
      public CompletableFuture<Boolean> remove(String id)
      {
         CategorizationScheme.Strategy strategy = getStrategyForPrefix(id.charAt(0));
         switch (strategy)
         {
            case TREE:
               return treeRepo.delete(svcContext.getAccount().orElse(null), id);
            case SET:
               // TODO add support for sets
               throw new UnsupportedOperationException("Set categorizations are not yet supported");
            case LIST:
               // TODO add support for lists
               throw new UnsupportedOperationException("List categorizations are not yet supported");
            default:
               throw new IllegalArgumentException("Unsupported categorization strategy: " + strategy);
         }
      }
   }
}
