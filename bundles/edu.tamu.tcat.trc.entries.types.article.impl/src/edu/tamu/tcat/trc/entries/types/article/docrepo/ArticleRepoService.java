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

import edu.tamu.tcat.account.Account;
import edu.tamu.tcat.osgi.config.ConfigurationProperties;
import edu.tamu.tcat.trc.entries.core.EntryReference;
import edu.tamu.tcat.trc.entries.core.EntryResolver;
import edu.tamu.tcat.trc.entries.core.EntryResolverRegistry;
import edu.tamu.tcat.trc.entries.core.InvalidReferenceException;
import edu.tamu.tcat.trc.entries.core.db.DbEntryRepositoryContext;
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
      catch (Exception e)
      {
         logger.log(Level.SEVERE, "Failed to construct articles repository instance.", e);
         throw e;
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

   private static class ArticleResolver implements EntryResolver<Article>
   {
      private final ArticleRepoService articleSvc;
      private URI apiEndpoint;

      public ArticleResolver(ArticleRepoService articleSvc, ConfigurationProperties config)
      {
         this.articleSvc = articleSvc;
         this.apiEndpoint = config.getPropertyValue("trc.api.endpoint", URI.class, URI.create(""));
      }

      @Override
      public Article resolve(Account account, EntryReference reference) throws InvalidReferenceException
      {
         if (!accepts(reference.type))
            throw new InvalidReferenceException(reference, "Unsupported reference type.");

         ArticleRepository repo = articleSvc.getArticleRepo(account);
         return repo.get(reference.id);
      }

      @Override
      public URI toUri(EntryReference reference) throws InvalidReferenceException
      {
         if (!accepts(reference.type))
            throw new InvalidReferenceException(reference, "Unsupported reference type.");

         // format: <api_endpoint>/entries/articles/{articleId}
         return apiEndpoint.resolve(ArticleRepository.ENTRY_URI_BASE).resolve(reference.id);
      }

      @Override
      public EntryReference makeReference(Article instance) throws InvalidReferenceException
      {
         EntryReference ref = new EntryReference();
         ref.id = instance.getId();
         ref.type = ArticleRepository.ENTRY_TYPE_ID;

         return ref;
      }

      @Override
      public EntryReference makeReference(URI uri) throws InvalidReferenceException
      {
         URI articleId = uri.relativize(apiEndpoint.resolve(ArticleRepository.ENTRY_URI_BASE));
         if (articleId.equals(uri))
            throw new InvalidReferenceException(uri, "The supplied URI does not reference an article.");

         String path = articleId.getPath();
         if (path.contains("/"))
            throw new InvalidReferenceException(uri, "The supplied URI represents a sub-resource of an article.");

         EntryReference ref = new EntryReference();
         ref.id = path;
         ref.type = ArticleRepository.ENTRY_TYPE_ID;

         return ref;
      }

      @Override
      public boolean accepts(Object obj)
      {
         return (ArticleImpl.class.isInstance(obj));
      }

      @Override
      public boolean accepts(EntryReference ref)
      {
         return ArticleRepository.ENTRY_TYPE_ID.equals(ref.type);
      }

      @Override
      public boolean accepts(URI uri)
      {
         URI articleId = uri.relativize(apiEndpoint.resolve(ArticleRepository.ENTRY_URI_BASE));
//         The supplied URI does not reference an article
         if (articleId.equals(uri))
            return false;

         String path = articleId.getPath();
//         The supplied URI represents a sub-resource of an article.
         if (path.contains("/"))
            return false;

         return true;
      }
   }
}
