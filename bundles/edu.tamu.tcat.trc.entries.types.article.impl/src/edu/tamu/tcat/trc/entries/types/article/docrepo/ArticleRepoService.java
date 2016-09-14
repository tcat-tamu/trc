package edu.tamu.tcat.trc.entries.types.article.docrepo;

import static java.text.MessageFormat.format;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.tamu.tcat.account.Account;
import edu.tamu.tcat.osgi.config.ConfigurationProperties;
import edu.tamu.tcat.trc.entries.core.InvalidReferenceException;
import edu.tamu.tcat.trc.entries.core.repo.BasicRepoDelegate;
import edu.tamu.tcat.trc.entries.core.repo.UnauthorziedException;
import edu.tamu.tcat.trc.entries.core.repo.db.DbEntryRepositoryRegistry;
import edu.tamu.tcat.trc.entries.core.resolver.EntryReference;
import edu.tamu.tcat.trc.entries.core.resolver.EntryResolverBase;
import edu.tamu.tcat.trc.entries.types.article.Article;
import edu.tamu.tcat.trc.entries.types.article.repo.ArticleRepository;
import edu.tamu.tcat.trc.entries.types.article.repo.EditArticleCommand;
import edu.tamu.tcat.trc.repo.DocRepoBuilder;
import edu.tamu.tcat.trc.repo.DocumentRepository;
import edu.tamu.tcat.trc.repo.IdFactory;

public class ArticleRepoService
{
   private final static Logger logger = Logger.getLogger(ArticleRepoService.class.getName());

   public static final String PARAM_TABLE_NAME = "trc.entries.articles.tablename";
   public static final String PARAM_ID_CTX = "trc.entries.articles.id_context";

   private static final String ID_CONTEXT_ARTICLES = "trc.articles";
   private static final String TABLE_NAME = "articles";
   private static final String SCHEMA_DATA_FIELD = "data";

   private DbEntryRepositoryRegistry context;
   private ConfigurationProperties config;

   private IdFactory articleIds;

   private DocumentRepository<Article, DataModelV1.Article, EditArticleCommand> repoBackend;
   private BasicRepoDelegate<Article, DataModelV1.Article, EditArticleCommand> delegate;

   /**
    * Note that this requires the implementation-specific
    * @param context
    */
   public void setRepoContext(DbEntryRepositoryRegistry context)
   {
      logger.fine(format("[{0}] setting repository context", getClass().getName()));
      this.context = context;
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
      logger.info("Activating " + getClass().getSimpleName());
      articleIds = context.getIdFactory(ID_CONTEXT_ARTICLES);
      config = context.getConfig();

      initDocRepo();
      initDelegate();

      context.registerRepository(ArticleRepository.class,
            account -> new ArticleRepoImpl(delegate, account));
      context.registerResolver(new ArticleResolver());

      logger.fine("Activated " + getClass().getSimpleName());
   }


   private void initDocRepo()
   {
      DocRepoBuilder<Article, DataModelV1.Article, EditArticleCommand> builder = context.getDocRepoBuilder();
      repoBackend = builder.setTableName(TABLE_NAME)
             .setDataColumn(SCHEMA_DATA_FIELD)
             .setEditCommandFactory(new EditArticleCommandFactory())
             .setDataAdapter(ArticleImpl::new)
             .setStorageType(DataModelV1.Article.class)
             .build();
   }

   private void initDelegate()
   {
      BasicRepoDelegate.Builder<Article, DataModelV1.Article, EditArticleCommand> delegateBuilder =
            new BasicRepoDelegate.Builder<>();

      delegateBuilder.setEntryName("bibliographic");
      delegateBuilder.setIdFactory(articleIds);
      delegateBuilder.setEntryResolvers(context.getResolverRegistry());
      delegateBuilder.setAdapter(ArticleImpl::new);
      delegateBuilder.setDocumentRepo(repoBackend);

      delegate = delegateBuilder.build();
   }

   /**
    * Lifecycle management method (usually called by framework service layer)
    * Called when this service is no longer required.
    */
   public void dispose()
   {
      delegate.dispose();
      repoBackend.dispose();
   }

   private class ArticleResolver extends EntryResolverBase<Article>
   {
      public ArticleResolver()
      {
         super(Article.class,
               config,
               ArticleRepository.ENTRY_URI_BASE,
               ArticleRepository.ENTRY_TYPE_ID);
      }

      @Override
      public Article resolve(Account account, EntryReference reference) throws InvalidReferenceException
      {
         if (!accepts(reference))
            throw new InvalidReferenceException(reference, "Unsupported reference type.");

         return delegate.get(account, reference.id);
      }

      @Override
      protected String getId(Article article)
      {
         return article.getId();
      }

      @Override
      public CompletableFuture<Boolean> remove(Account account, EntryReference reference) throws InvalidReferenceException, UnauthorziedException, UnsupportedOperationException
      {
         return delegate.remove(account, reference.id);
      }
   }
}
