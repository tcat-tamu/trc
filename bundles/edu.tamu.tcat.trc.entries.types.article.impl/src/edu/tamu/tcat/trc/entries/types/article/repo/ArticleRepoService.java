package edu.tamu.tcat.trc.entries.types.article.repo;

import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.tamu.tcat.account.Account;
import edu.tamu.tcat.db.exec.sql.SqlExecutor;
import edu.tamu.tcat.trc.entries.repo.NoSuchCatalogRecordException;
import edu.tamu.tcat.trc.entries.types.article.Article;
import edu.tamu.tcat.trc.entries.types.article.rest.v1.ModelAdapter;
import edu.tamu.tcat.trc.entries.types.article.search.solr.ArticleIndexManagerService;
import edu.tamu.tcat.trc.repo.BasicSchemaBuilder;
import edu.tamu.tcat.trc.repo.DocumentRepository;
import edu.tamu.tcat.trc.repo.IdFactory;
import edu.tamu.tcat.trc.repo.IdFactoryProvider;
import edu.tamu.tcat.trc.repo.RepositoryException;
import edu.tamu.tcat.trc.repo.postgres.PsqlJacksonRepoBuilder;

public class ArticleRepoService
{
   private final static Logger logger = Logger.getLogger(ArticleRepoService.class.getName());
   private static final String ID_CONTEXT_ARTICLES = "trc.articles";

   private DocumentRepository<Article, DataModelV1.Article, EditArticleCommand> repoBackend;

   private SqlExecutor sqlExecutor;
   private IdFactoryProvider idFactoryProvider;

   private static final String TABLE_NAME = "articles";

   private IdFactory idFactory;

   private ArticleIndexManagerService indexSvc;

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
   public void activate()
   {
      Objects.requireNonNull(sqlExecutor, "No SQL Executor provided.");

      repoBackend = buildDocumentRepository();
      idFactory = idFactoryProvider.getIdFactory(ID_CONTEXT_ARTICLES);
   }

   /**
    * @return A new document repository instance for persisting and retrieving works
    */
   private DocumentRepository<Article, DataModelV1.Article, EditArticleCommand> buildDocumentRepository()
   {
      PsqlJacksonRepoBuilder<Article, DataModelV1.Article, EditArticleCommand> repoBuilder = new PsqlJacksonRepoBuilder<>();

      repoBuilder.setDbExecutor(sqlExecutor);
      repoBuilder.setTableName(TABLE_NAME);
      // TODO register service as post commit hook
      repoBuilder.setEditCommandFactory(new EditArticleCommandFactory(indexSvc));
      repoBuilder.setDataAdapter(ModelAdapter::adapt);
      repoBuilder.setSchema(BasicSchemaBuilder.buildDefaultSchema());
      repoBuilder.setStorageType(DataModelV1.Article.class);
      repoBuilder.setEnableCreation(true);

      try
      {
         return repoBuilder.build();
      }
      catch (RepositoryException e)
      {
         logger.log(Level.SEVERE, "Failed to construct articles repository instance.", e);
      }
      return null;
   }

   /**
    * Lifecycle management method (usually called by framework service layer)
    * Called when this service is no longer required.
    */
   public void dispose()
   {
      sqlExecutor = null;
   }

   public ArticleRepository getRepository(Account account)
   {
      return new ArticleRepoImpl(account);
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
         return repoBackend.get(articleId);
      }

      @Override
      public List<Article> getArticles(URI entityURI) throws NoSuchCatalogRecordException
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
         return repoBackend.create(id);
      }

      @Override
      public EditArticleCommand edit(String articleId) throws NoSuchCatalogRecordException
      {
         try
         {
            return repoBackend.edit(articleId);
         }
         catch (RepositoryException e)
         {
            throw new IllegalArgumentException("Unable to find article with id {" + articleId + "}.", e);
         }
      }

      @Override
      public Future<Boolean> remove(String articleId)
      {
         CompletableFuture<Boolean> result = repoBackend.delete(articleId);
         
         result.thenRun(() -> {
               if (indexSvc != null)
                  indexSvc.remove(articleId);
            });
         
         return result;
      }

      @Override
      public Runnable register(Consumer<Article> ears)
      {
         return repoBackend.afterUpdate((dto) -> {
            // TODO adapt dto to Article
            ears.accept(null);
         });
      }

   }
}
