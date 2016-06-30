package edu.tamu.tcat.trc.entries.types.article.docrepo;

import static java.text.MessageFormat.format;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;

import edu.tamu.tcat.account.Account;
import edu.tamu.tcat.db.exec.sql.SqlExecutor;
import edu.tamu.tcat.trc.entries.types.article.Article;
import edu.tamu.tcat.trc.entries.types.article.repo.ArticleAuthorRepository;
import edu.tamu.tcat.trc.entries.types.article.repo.ArticleRepository;
import edu.tamu.tcat.trc.entries.types.article.repo.EditArticleCommand;
import edu.tamu.tcat.trc.entries.types.article.search.solr.ArticleDocument;
import edu.tamu.tcat.trc.entries.types.article.search.solr.ArticleIndexManagerService;
import edu.tamu.tcat.trc.repo.BasicSchemaBuilder;
import edu.tamu.tcat.trc.repo.DocumentRepository;
import edu.tamu.tcat.trc.repo.IdFactory;
import edu.tamu.tcat.trc.repo.IdFactoryProvider;
import edu.tamu.tcat.trc.repo.NoSuchEntryException;
import edu.tamu.tcat.trc.repo.RepositoryException;
import edu.tamu.tcat.trc.repo.UpdateContext;
import edu.tamu.tcat.trc.repo.postgres.PsqlJacksonRepoBuilder;
import edu.tamu.tcat.trc.search.SearchException;

public class ArticleRepoService
{
   private final static Logger logger = Logger.getLogger(ArticleRepoService.class.getName());

   public static final String PARAM_TABLE_NAME = "trc.entries.articles.tablename";
   public static final String PARAM_ID_CTX = "trc.entries.articles.id_context";

   private static final String ID_CONTEXT_ARTICLES = "trc.articles";
   private static final String TABLE_NAME = "articles";

   private SqlExecutor sqlExecutor;
   private IdFactoryProvider idFactoryProvider;
   private ArticleIndexManagerService indexSvc;


   private IdFactory idFactory;
   private DocumentRepository<Article, DataModelV1.Article, EditArticleCommand> articleBackend;

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

   public void setIndexService(ArticleIndexManagerService indexSvc)
   {
      this.indexSvc = indexSvc;
   }

   /**
    * Lifecycle management method (usually called by framework service layer)
    * Called when all dependencies have been provided and the service is ready to run.
    */
   public void activate(Map<String, Object> properties)
   {
      try
      {
         Objects.requireNonNull(sqlExecutor, "No SQL Executor provided.");

         String tablename = (String)properties.getOrDefault(PARAM_TABLE_NAME, TABLE_NAME);
         articleBackend = buildDocumentRepository(tablename);
         configureIndexing(articleBackend);
         configureVersioning(articleBackend);

         String idContext = (String)properties.getOrDefault(PARAM_ID_CTX, ID_CONTEXT_ARTICLES);
         idFactory = idFactoryProvider.getIdFactory(idContext);
      }
      catch (Exception e)
      {
         logger.log(Level.SEVERE, "Failed to construct articles repository instance.", e);
         throw e;
      }
   }

   /**
    * @return A new document repository instance for persisting and retrieving works
    */
   private DocumentRepository<Article, DataModelV1.Article, EditArticleCommand> buildDocumentRepository(String tablename)
   {
      PsqlJacksonRepoBuilder<Article, DataModelV1.Article, EditArticleCommand> repoBuilder = new PsqlJacksonRepoBuilder<>();

      repoBuilder.setDbExecutor(sqlExecutor);
      repoBuilder.setTableName(tablename);
      repoBuilder.setEditCommandFactory(new EditArticleCommandFactory());
      repoBuilder.setDataAdapter(dto -> new ArticleImpl(dto));
      repoBuilder.setSchema(BasicSchemaBuilder.buildDefaultSchema());
      repoBuilder.setStorageType(DataModelV1.Article.class);
      repoBuilder.setEnableCreation(true);

      return repoBuilder.build();
   }

   private void configureIndexing(DocumentRepository<Article, DataModelV1.Article, EditArticleCommand> repo)
   {
      if (indexSvc == null)
         logger.warning("No search index has been configured for articles.");

      repo.afterUpdate(this::index);
      // TODO Auto-generated method stub

   }

   private void index(UpdateContext<DataModelV1.Article> ctx)
   {
      if (indexSvc == null)
      {
         logger.info(() -> format("Not indexing article {0}. No search index is available.", ctx.getId()));
         return;
      }

      try
      {
         // TODO should be able to generalize this.
         switch(ctx.getActionType())
         {
            case CREATE:
               indexSvc.postDocument(getSolrDoc(ctx));
               break;
            case EDIT:
               indexSvc.postDocument(getSolrDoc(ctx));
               break;
            case REMOVE:
               indexSvc.remove(ctx.getId());
               break;
         }
      }
      catch (Exception ex)
      {
         logger.log(Level.SEVERE, ex, () -> "Failed to index article {0}: " + ex.getMessage());
      }
   }

   private ArticleDocument getSolrDoc(UpdateContext<DataModelV1.Article> ctx) throws JsonProcessingException, SearchException
   {
      DataModelV1.Article dto = ctx.getModified();
      return ArticleDocument.create(new ArticleImpl(dto));
   }

   private void configureVersioning(DocumentRepository<Article, DataModelV1.Article, EditArticleCommand> repo)
   {
      // TODO Auto-generated method stub
   }


   /**
    * Lifecycle management method (usually called by framework service layer)
    * Called when this service is no longer required.
    */
   public void dispose()
   {
      sqlExecutor = null;
   }

   public ArticleRepository getArticleRepo(Account account)
   {
      return new ArticleRepoImpl(account);
   }

   public ArticleAuthorRepository getAuthorRepo(Account account)
   {
      return new ArticleAuthorRepoImpl();
   }

   public class ArticleAuthorRepoImpl implements ArticleAuthorRepository
   {

   }
   public class ArticleRepoImpl implements ArticleRepository
   {

      @SuppressWarnings("unused")      // placeholder to be used once account creation has been integrated
      private Account account;

      public ArticleRepoImpl(Account account)
      {
         this.account = account;
      }

      @Override
      public Article get(String articleId)
      {
         return articleBackend.get(articleId);
      }

      @Override
      public List<Article> getArticles(URI entityURI) throws NoSuchEntryException
      {
         // This seems like a query rather than part of the article repo impl.
         throw new UnsupportedOperationException();
      }

      @Override
      public EditArticleCommand create()
      {
         String id = idFactory.get();
         return create(id);
      }

      @Override
      public EditArticleCommand create(String id)
      {
         return articleBackend.create(id);
      }

      @Override
      public EditArticleCommand edit(String articleId) throws NoSuchEntryException
      {
         try
         {
            return articleBackend.edit(articleId);
         }
         catch (RepositoryException e)
         {
            throw new IllegalArgumentException("Unable to find article with id {" + articleId + "}.", e);
         }
      }

      @Override
      public Future<Boolean> remove(String articleId)
      {
         CompletableFuture<Boolean> result = articleBackend.delete(articleId);

         result.thenRun(() -> {
               if (indexSvc != null)
                  indexSvc.remove(articleId);
            });

         return result;
      }

      @Override
      public Runnable register(Consumer<Article> ears)
      {
         return articleBackend.afterUpdate((dto) -> {
            // TODO adapt dto to Article
            ears.accept(null);
         });
      }

   }
}
