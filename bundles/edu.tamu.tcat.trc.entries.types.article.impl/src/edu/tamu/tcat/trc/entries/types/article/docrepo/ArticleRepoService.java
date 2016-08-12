package edu.tamu.tcat.trc.entries.types.article.docrepo;

import static java.text.MessageFormat.format;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.tamu.tcat.account.Account;
import edu.tamu.tcat.trc.entries.core.repo.db.DbEntryRepositoryContext;
import edu.tamu.tcat.trc.entries.core.resolver.EntryResolverRegistry;
import edu.tamu.tcat.trc.entries.types.article.Article;
import edu.tamu.tcat.trc.entries.types.article.ArticleRepoFacade;
import edu.tamu.tcat.trc.entries.types.article.repo.ArticleRepository;
import edu.tamu.tcat.trc.entries.types.article.repo.EditArticleCommand;
import edu.tamu.tcat.trc.entries.types.article.search.ArticleSearchService;
import edu.tamu.tcat.trc.entries.types.article.search.solr.ArticleDocument;
import edu.tamu.tcat.trc.entries.types.article.search.solr.ArticleIndexManagerService;
import edu.tamu.tcat.trc.repo.DocumentRepository;
import edu.tamu.tcat.trc.repo.IdFactory;
import edu.tamu.tcat.trc.repo.NoSuchEntryException;
import edu.tamu.tcat.trc.repo.RepositoryException;
import edu.tamu.tcat.trc.repo.UpdateContext;
import edu.tamu.tcat.trc.search.solr.impl.TrcDocument;

public class ArticleRepoService implements ArticleRepoFacade
{
   private final static Logger logger = Logger.getLogger(ArticleRepoService.class.getName());

   public static final String PARAM_TABLE_NAME = "trc.entries.articles.tablename";
   public static final String PARAM_ID_CTX = "trc.entries.articles.id_context";

   private static final String ID_CONTEXT_ARTICLES = "trc.articles";
   private static final String TABLE_NAME = "articles";

   private ArticleIndexManagerService indexSvc;
   private DbEntryRepositoryContext context;

   private IdFactory idFactory;
   private DocumentRepository<Article, DataModelV1.Article, EditArticleCommand> articleBackend;


   public void setRepoContext(DbEntryRepositoryContext context)
   {
      this.context = context;
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
         doActivation(properties);
      }
      catch (Exception e)
      {
         logger.log(Level.SEVERE, "Failed to construct articles repository instance.", e);
         throw e;
      }
   }

   private void doActivation(Map<String, Object> properties)
   {
      Objects.requireNonNull(context, "EntryRepositoryContext is not abailable.");
      String tablename = (String)properties.getOrDefault(PARAM_TABLE_NAME, TABLE_NAME);
      articleBackend = context.buildDocumentRepo(
            tablename,
            new EditArticleCommandFactory(),
            dto -> new ArticleImpl(dto),
            DataModelV1.Article.class);
      configureIndexing(articleBackend);
      configureVersioning(articleBackend);

      String idContext = (String)properties.getOrDefault(PARAM_ID_CTX, ID_CONTEXT_ARTICLES);
      idFactory = context.getIdFactory(idContext);
      EntryResolverRegistry registry = context.getResolverRegistry();
      if (registry != null)
      {
         registry.register(new ArticleResolver(this, context.getConfig()));
      }
   }

   private void configureIndexing(DocumentRepository<Article, DataModelV1.Article, EditArticleCommand> repo)
   {
      if (indexSvc == null)
         logger.warning("No search index has been configured for articles.");

      repo.afterUpdate(this::index);
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
         TrcDocument doc;
         switch(ctx.getActionType())
         {
            case CREATE:
               doc = ArticleDocument.adapt(ctx.getModified());
               indexSvc.postDocument(doc);
               break;
            case EDIT:
               doc = ArticleDocument.adapt(ctx.getModified());
               indexSvc.postDocument(doc);
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
   }

   /**
    * @param account The account to be used with this repository.
    * @return Obtains an {@code ArticleRepository} scoped to a particular user account.
    */
   @Override
   public ArticleRepository getArticleRepo(Account account)
   {
      return new ArticleRepoImpl(account);
   }

   /**
    * @return The article search service associated with this repository.
    *       Note that this may be {@code null} if no search service has been configured.
    */
   @Override
   public ArticleSearchService getSearchService()
   {
      return indexSvc;
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
      public CompletableFuture<Boolean> remove(String articleId)
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
