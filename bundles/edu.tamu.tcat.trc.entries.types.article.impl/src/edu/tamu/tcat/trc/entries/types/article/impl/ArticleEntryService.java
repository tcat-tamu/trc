package edu.tamu.tcat.trc.entries.types.article.impl;

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.tamu.tcat.trc.entries.core.repo.BasicRepoDelegate;
import edu.tamu.tcat.trc.entries.core.repo.EntryRepository;
import edu.tamu.tcat.trc.entries.core.repo.RepositoryContext;
import edu.tamu.tcat.trc.entries.core.resolver.EntryResolverRegistry;
import edu.tamu.tcat.trc.entries.core.search.SolrSearchMediator;
import edu.tamu.tcat.trc.entries.types.article.Article;
import edu.tamu.tcat.trc.entries.types.article.impl.model.ArticleImpl;
import edu.tamu.tcat.trc.entries.types.article.impl.repo.ArticleRepoImpl;
import edu.tamu.tcat.trc.entries.types.article.impl.repo.ArticleResolver;
import edu.tamu.tcat.trc.entries.types.article.impl.repo.DataModelV1;
import edu.tamu.tcat.trc.entries.types.article.impl.repo.EditArticleCommandFactory;
import edu.tamu.tcat.trc.entries.types.article.impl.search.ArticleSearchStrategy;
import edu.tamu.tcat.trc.entries.types.article.repo.ArticleRepository;
import edu.tamu.tcat.trc.entries.types.article.repo.EditArticleCommand;
import edu.tamu.tcat.trc.repo.DocRepoBuilder;
import edu.tamu.tcat.trc.repo.DocumentRepository;
import edu.tamu.tcat.trc.search.solr.IndexService;
import edu.tamu.tcat.trc.search.solr.SearchServiceManager;

public class ArticleEntryService
{
   private final static Logger logger = Logger.getLogger(ArticleEntryService.class.getName());

   public static final String PARAM_TABLE_NAME = "trc.entries.articles.tablename";
   public static final String PARAM_ID_CTX = "trc.entries.articles.id_context";

   private static final String ID_CONTEXT_ARTICLES = "trc.articles";
   private static final String TABLE_NAME = "articles";
   private static final String SCHEMA_DATA_FIELD = "data";

   private RepositoryContext context;
   private SearchServiceManager indexSvcMgr;

   private RepositoryContext.Registration repoReg;
   private EntryResolverRegistry.Registration resolverReg;
   private EntryRepository.ObserverRegistration searchReg;

   private DocumentRepository<Article, DataModelV1.Article, EditArticleCommand> docRepo;
   private BasicRepoDelegate<Article, DataModelV1.Article, EditArticleCommand> delegate;

   public void setRepoContext(RepositoryContext ctx)
   {
      this.context = ctx;
   }


   public void setSearchSvcMgr(SearchServiceManager indexSvcFactory)
   {
      this.indexSvcMgr = indexSvcFactory;
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
      builder.setTableName(TABLE_NAME);
      builder.setDataColumn(SCHEMA_DATA_FIELD);
      builder.setEditCommandFactory(new EditArticleCommandFactory());
      builder.setDataAdapter(ArticleImpl::new);
      builder.setStorageType(DataModelV1.Article.class);

      docRepo = builder.build();
   }

   private void initDelegate()
   {
      BasicRepoDelegate.Builder<Article, DataModelV1.Article, EditArticleCommand> delegateBuilder =
            new BasicRepoDelegate.Builder<>();

      delegateBuilder.setEntryName("bibliographic");
      delegateBuilder.setIdFactory(context.getIdFactory(ID_CONTEXT_ARTICLES));
      delegateBuilder.setEntryResolvers(context.getResolverRegistry());
      delegateBuilder.setAdapter(ArticleImpl::new);
      delegateBuilder.setDocumentRepo(docRepo);

      delegate = delegateBuilder.build();
   }

   private void initSearch()
   {
      if (indexSvcMgr == null)
      {
         logger.log(Level.WARNING, "Index support has not been configured for " + getClass().getSimpleName());
         return;
      }

      ArticleSearchStrategy strategy = new ArticleSearchStrategy();
      IndexService<Article> indexSvc = indexSvcMgr.configure(strategy);

      ArticleRepoImpl repo = new ArticleRepoImpl(delegate, null);     // USE SEARCH ACCT
      searchReg = repo.onUpdate(ctx -> SolrSearchMediator.index(indexSvc, ctx));
   }
}
