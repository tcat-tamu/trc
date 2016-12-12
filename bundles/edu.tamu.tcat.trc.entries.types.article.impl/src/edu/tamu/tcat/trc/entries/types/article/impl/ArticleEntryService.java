package edu.tamu.tcat.trc.entries.types.article.impl;

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.tamu.tcat.trc.TrcApplication;
import edu.tamu.tcat.trc.entries.core.repo.BasicRepoDelegate;
import edu.tamu.tcat.trc.entries.core.repo.EntryRepository;
import edu.tamu.tcat.trc.entries.core.repo.EntryRepositoryRegistrar;
import edu.tamu.tcat.trc.entries.types.article.Article;
import edu.tamu.tcat.trc.entries.types.article.impl.model.ArticleImpl;
import edu.tamu.tcat.trc.entries.types.article.impl.repo.ArticleRepoImpl;
import edu.tamu.tcat.trc.entries.types.article.impl.repo.ArticleResolver;
import edu.tamu.tcat.trc.entries.types.article.impl.repo.DataModelV1;
import edu.tamu.tcat.trc.entries.types.article.impl.repo.EditArticleCommandFactory;
import edu.tamu.tcat.trc.entries.types.article.impl.search.ArticleSearchStrategy;
import edu.tamu.tcat.trc.entries.types.article.repo.ArticleRepository;
import edu.tamu.tcat.trc.entries.types.article.repo.EditArticleCommand;
import edu.tamu.tcat.trc.impl.psql.entries.SolrSearchSupport;
import edu.tamu.tcat.trc.repo.DocRepoBuilder;
import edu.tamu.tcat.trc.repo.DocumentRepository;
import edu.tamu.tcat.trc.resolver.EntryResolverRegistrar;
import edu.tamu.tcat.trc.search.solr.IndexService;
import edu.tamu.tcat.trc.search.solr.SearchServiceManager;

public class ArticleEntryService
{
   private final static Logger logger = Logger.getLogger(ArticleEntryService.class.getName());

   public static final String PARAM_TABLE_NAME = "trc.entries.articles.tablename";
   public static final String PARAM_ID_CTX = "trc.entries.articles.id_context";

   private static final String ID_CONTEXT_ARTICLES = "trc.articles";
   private static final String TABLE_NAME = "articles";

   private EntryRepositoryRegistrar context;
//   private SearchServiceManager indexSvcMgr;

   private EntryRepositoryRegistrar.Registration repoReg;
   private EntryResolverRegistrar.Registration resolverReg;
   private EntryRepository.ObserverRegistration searchReg;

   private DocumentRepository<Article, EditArticleCommand> docRepo;
   private BasicRepoDelegate<Article, DataModelV1.Article, EditArticleCommand> delegate;

   private TrcApplication trcCtx;

   public void setTrcContext(TrcApplication trcCtx)
   {
      this.trcCtx = trcCtx;
   }

   public void setRepoContext(EntryRepositoryRegistrar ctx)
   {
      this.context = ctx;
   }

   /**
    * Lifecycle management method (usually called by framework service layer)
    * Called when all dependencies have been provided and the service is ready to run.
    */
   public void activate()
   {
      try
      {
         logger.info("Activating " + getClass().getSimpleName());

         initRepo();

         // make sure these are all set up and fail fast if not.
         Objects.requireNonNull(delegate);
         Objects.requireNonNull(docRepo);
         Objects.requireNonNull(resolverReg);
         Objects.requireNonNull(repoReg);

         initSearch();

         logger.fine("Activated " + getClass().getSimpleName());
      }
      catch (Exception e)
      {
         logger.log(Level.SEVERE, "Failed to construct articles repository instance.", e);
         throw e;
      }
   }

   /**
    * Lifecycle management method (usually called by framework service layer)
    * Called when this service is no longer required.
    */
   public void dispose()
   {
      try
      {
         logger.info("Stopping " + getClass().getSimpleName());

         resolverReg.unregister();
         repoReg.unregister();
         docRepo.dispose();
         delegate.dispose();

         if (searchReg != null)
            searchReg.close();

         logger.fine("Stopped " + getClass().getSimpleName());

      }
      catch (Exception ex)
      {
         logger.log(Level.SEVERE, "Failed to stop" + getClass().getSimpleName(), ex);
         throw ex;
      }
   }

   private String getTableName()
   {
      return trcCtx.getConfig().getPropertyValue(PARAM_TABLE_NAME, String.class, TABLE_NAME);
   }


   private void initRepo()
   {
      initDocRepo();
      initDelegate();

      resolverReg = context.registerResolver(new ArticleResolver(context.getConfig(), delegate));
      repoReg = context.registerRepository(ArticleRepository.class, account -> new ArticleRepoImpl(delegate, account));
   }


   private void initDocRepo()
   {
      DocRepoBuilder<Article, DataModelV1.Article, EditArticleCommand> builder = context.getDocRepoBuilder();
      builder.setPersistenceId(getTableName());
      builder.setEditCommandFactory(new EditArticleCommandFactory());
      builder.setDataAdapter(ArticleImpl::new);
      builder.setStorageType(DataModelV1.Article.class);
      builder.setEnableCreation(true);

      docRepo = builder.build();
   }

   private void initDelegate()
   {
      BasicRepoDelegate.Builder<Article, DataModelV1.Article, EditArticleCommand> delegateBuilder =
            new BasicRepoDelegate.Builder<>();

      delegateBuilder.setEntryName("bibliographic");
      delegateBuilder.setIdFactory(context.getIdFactory(ID_CONTEXT_ARTICLES));
      delegateBuilder.setEntryResolvers(context.getResolverRegistry());
      delegateBuilder.setDocumentRepo(docRepo);

      delegate = delegateBuilder.build();
   }

   private void initSearch()
   {
      SearchServiceManager indexSvcMgr = trcCtx.getSearchManager();
      if (indexSvcMgr == null)
      {
         logger.log(Level.WARNING, "Index support has not been configured for " + getClass().getSimpleName());
         return;
      }

      ArticleSearchStrategy strategy = new ArticleSearchStrategy(context.getResolverRegistry());
      IndexService<Article> indexSvc = indexSvcMgr.configure(strategy);

      ArticleRepoImpl repo = new ArticleRepoImpl(delegate, null);     // USE SEARCH ACCT
      SolrSearchSupport<Article> mediator = new SolrSearchSupport<>(indexSvc, strategy);
      searchReg = repo.onUpdate(mediator::handleUpdate);
   }
}
