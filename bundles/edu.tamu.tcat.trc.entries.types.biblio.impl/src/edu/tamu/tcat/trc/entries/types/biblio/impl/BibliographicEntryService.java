package edu.tamu.tcat.trc.entries.types.biblio.impl;

import static java.text.MessageFormat.format;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.tamu.tcat.account.Account;
import edu.tamu.tcat.trc.entries.core.repo.BasicRepoDelegate;
import edu.tamu.tcat.trc.entries.core.repo.EntryRepository;
import edu.tamu.tcat.trc.entries.core.repo.EntryRepositoryRegistrar;
import edu.tamu.tcat.trc.entries.types.biblio.BiblioEntryUtils;
import edu.tamu.tcat.trc.entries.types.biblio.BibliographicEntry;
import edu.tamu.tcat.trc.entries.types.biblio.Title;
import edu.tamu.tcat.trc.entries.types.biblio.dto.WorkDTO;
import edu.tamu.tcat.trc.entries.types.biblio.impl.repo.BiblioRepoImpl;
import edu.tamu.tcat.trc.entries.types.biblio.impl.repo.EditWorkCommandFactory;
import edu.tamu.tcat.trc.entries.types.biblio.impl.repo.ModelAdapter;
import edu.tamu.tcat.trc.entries.types.biblio.impl.search.BibliographicSearchStrategy;
import edu.tamu.tcat.trc.entries.types.biblio.repo.BibliographicEntryRepository;
import edu.tamu.tcat.trc.entries.types.biblio.repo.EditBibliographicEntryCommand;
import edu.tamu.tcat.trc.impl.psql.entries.SolrSearchSupport;
import edu.tamu.tcat.trc.repo.DocRepoBuilder;
import edu.tamu.tcat.trc.repo.DocumentRepository;
import edu.tamu.tcat.trc.resolver.EntryId;
import edu.tamu.tcat.trc.resolver.EntryResolverBase;
import edu.tamu.tcat.trc.resolver.EntryResolverRegistrar;
import edu.tamu.tcat.trc.resolver.InvalidReferenceException;
import edu.tamu.tcat.trc.search.solr.IndexService;
import edu.tamu.tcat.trc.search.solr.SearchServiceManager;

public class BibliographicEntryService
{
   private static final Logger logger = Logger.getLogger(BibliographicEntryService.class.getName());

   public static final String ID_CONTEXT_WORKS = "works";
   public static final String ID_CONTEXT_EDITIONS = "editions";
   public static final String ID_CONTEXT_VOLUMES = "volumes";
   public static final String ID_CONTEXT_COPIES = "copies";

   private static final String TABLE_NAME = "works";
   private static final String SCHEMA_DATA_FIELD = "work";


   private EntryRepositoryRegistrar context;
   private SearchServiceManager indexSvcMgr;

   private DocumentRepository<BibliographicEntry, WorkDTO, EditBibliographicEntryCommand> docRepo;
   private BasicRepoDelegate<BibliographicEntry, WorkDTO, EditBibliographicEntryCommand> delegate;

   private EntryResolverRegistrar.Registration resolverReg;
   private EntryRepositoryRegistrar.Registration repoReg;
   private EntryRepository.ObserverRegistration searchReg;

   public void setRepoContext(EntryRepositoryRegistrar context)
   {
      logger.fine(format("[{0}] setting repository context", getClass().getName()));
      this.context = context;
   }

   public void setSearchSvcMgr(SearchServiceManager indexSvcFactory)
   {
      logger.fine(format("[{0}] setting search service manager", getClass().getName()));
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
      catch (Exception ex)
      {
         logger.log(Level.SEVERE, "Failed to activate bibliographic entry repository service.", ex);
         throw ex;
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

      this.resolverReg = context.registerResolver(new BibliographicEntryResolver());
      this.repoReg = context.registerRepository(BibliographicEntryRepository.class, account -> new BiblioRepoImpl(delegate, account));
   }

   private void initDocRepo()
   {
      DocRepoBuilder<BibliographicEntry, WorkDTO, EditBibliographicEntryCommand> builder = context.getDocRepoBuilder();
      builder.setTableName(TABLE_NAME);
      builder.setDataColumn(SCHEMA_DATA_FIELD);
      builder.setEditCommandFactory(new EditWorkCommandFactory(context::getIdFactory));
      builder.setDataAdapter(ModelAdapter::adapt);
      builder.setStorageType(WorkDTO.class);

      docRepo = builder.build();
   }

   private void initDelegate()
   {
      BasicRepoDelegate.Builder<BibliographicEntry, WorkDTO, EditBibliographicEntryCommand> delegateBuilder =
            new BasicRepoDelegate.Builder<>();

      delegateBuilder.setEntryName("bibliographic");
      delegateBuilder.setIdFactory(context.getIdFactory(ID_CONTEXT_WORKS));
      delegateBuilder.setEntryResolvers(context.getResolverRegistry());
      delegateBuilder.setAdapter(ModelAdapter::adapt);
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
      BibliographicSearchStrategy indexCfg = new BibliographicSearchStrategy();
      IndexService<BibliographicEntry> indexSvc = indexSvcMgr.configure(indexCfg);

      BiblioRepoImpl repo = new BiblioRepoImpl(delegate, null);     // TODO USE SEARCH ACCT
      SolrSearchSupport<BibliographicEntry> mediator = new SolrSearchSupport<>(indexSvc, indexCfg);
      searchReg = repo.onUpdate(mediator::handleUpdate);
   }

   private class BibliographicEntryResolver extends EntryResolverBase<BibliographicEntry>
   {

      public BibliographicEntryResolver()
      {
         super(BibliographicEntry.class,
               context.getConfig(),
               BibliographicEntryRepository.ENTRY_URI_BASE,
               BibliographicEntryRepository.ENTRY_TYPE_ID);
      }

      @Override
      public Optional<BibliographicEntry> resolve(Account account, EntryId reference) throws InvalidReferenceException
      {
         if (!accepts(reference))
            throw new InvalidReferenceException(reference, "Unsupported reference type.");

         return delegate.getOptionally(account, reference.getId());
      }

      @Override
      protected String getId(BibliographicEntry relationship)
      {
         return relationship.getId();
      }

      /**
       * @param instance the bibliographic entry to construct a label for.
       * @return A label in the following format <pre>Author (YYYY): Title</pre>. The title
       *       will be constructed using the 'short' or 'canonical' title if present, or
       *       by selecting a title at random, if not.
       */
      @Override
      public String getLabel(BibliographicEntry instance)
      {
         String title = instance.getTitle(Title.SHORT, Title.CANONICAL)
               .map(Title::getFullTitle)
               .orElse("Title Unavailable");
         String name = BiblioEntryUtils.parseAuthorName(instance);
         String pubDate = BiblioEntryUtils.parsePublicationDate(instance);

         StringBuilder sb = new StringBuilder();
         if (name != null)
            sb.append(name).append(pubDate == null ? ", " : " ");

         if (pubDate != null)
            sb.append("(").append(pubDate).append("): ");

         sb.append(title);
         return sb.toString();
      }

      /**
       * Constructs an HTML formatted label for a bibliographic entry using the following
       * template. Missing fields will be omitted as appropriate.
       *
       * <pre>
       * &lt;span class='bibentry-label'&gt;
       *   &lt;span class='bibentry-author'&gt;Smith, John&lt;/span&gt;
       *   &lt;span class='bibentry-pubdate'&gt;(year)&lt;/span&gt;:
       *   &lt;span class='bibentry-title'&gt;title&lt;/span&gt;
       * &lt;/span&gt;
       * </pre>
       *
       * @param instance The bibliography entry for which a label is to be returned.
       * @return The HTML formatted label
       */
      @Override
      public String getHtmlLabel(BibliographicEntry instance)
      {
         String title = instance.getTitle(Title.SHORT, Title.CANONICAL)
               .map(Title::getFullTitle)
               .orElse("Title Unavailable");
         String name = BiblioEntryUtils.parseAuthorName(instance);
         String pubDate = BiblioEntryUtils.parsePublicationDate(instance);

         StringBuilder sb = new StringBuilder();
         if (name != null)
            sb.append(format("<span class='bibentry-author'>{0}</span>", name)).append(pubDate == null ? ", " : " ");

         if (pubDate != null)
            sb.append(format("<span class='bibentry-pubdate'>({0})</span>: ", pubDate));

         sb.append(format("<span class='bibentry-title'>{0}</span>", title));

         return format("<span class='bibentry-label'>{0}</span>", sb.toString());
      }

      @Override
      public CompletableFuture<Boolean> remove(Account account, EntryId reference)
      {
         return delegate.remove(account, reference.getId());
      }
   }

}
