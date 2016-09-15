package edu.tamu.tcat.trc.search.solr;

import static java.text.MessageFormat.format;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrInputDocument;

import edu.tamu.tcat.osgi.config.ConfigurationProperties;

/**
 * General properties will be defined as:
 *
 * <dl>
 *   <dt>trc.search.solr.url</dt>
 *   <dd></dd>
 *
 *   <dt>trc.search.solr.enabled{=true}</dt>
 *   <dd></dd>
 *
 *   <dt>trc.search.solr.username</dt>
 *   <dd></dd>
 *
 *   <dt>trc.search.solr.password</dt>
 *   <dd></dd>
 * </dl>
 *
 * <p>Solr organizes it's search indices into different cores. The TRC {@link IndexService}
 * identifies the Solr core to be used based on the logical identifier supplied by
 *
 * Configuration properties for individual solr core's are as follows:
 *
 * <dl>
 *   <dt>trc.search.solr.cores.{coreId}.id={coreId}</dt>
 *   <dd></dd>
 *
 *   <dt>trc.search.solr.cores.{coreId}.enabled={true}</dt>
 *   <dd></dd>
 * </dl>
 * <p>The core key will be used to lookup configuration properties.
 *
 */
public class BasicIndexServiceFactory implements IndexServiceFactory
{
   private final static Logger logger = Logger.getLogger(BasicIndexService.class.getName());

   /** Configuration property key that defines the URI for the Solr server. */
   public static final String SOLR_API_ENDPOINT = "trc.search.solr.url";
   public static final String SOLR_ENABLED = "trc.search.solr.enabled";
   public static final String SOLR_USERNAME = "trc.search.solr.username";
   public static final String SOLR_PASSWORD = "trc.search.solr.password";

   public static final String SOLR_CORE_ID = "trc.search.solr.cores.{0}.id";
   public static final String SOLR_CORE_ENABLED = "trc.search.solr.cores.{0}.enabled";

   private ConfigurationProperties config;

   private URI solrBaseUri;
   private boolean solrEnabled;
   private String username;
   private String password;

   @SuppressWarnings("rawtypes")    // type consistency is enforced by controlled creation/access
   private final ConcurrentHashMap<Class, BasicIndexService> cache =
            new ConcurrentHashMap<>();

   public BasicIndexServiceFactory()
   {

   }

   public void setConfigurationProperties(ConfigurationProperties config)
   {
      this.config = config;
      this.solrBaseUri = config.getPropertyValue(SOLR_API_ENDPOINT, URI.class);
      this.solrEnabled = config.getPropertyValue(SOLR_ENABLED, Boolean.class, true);
      this.username = config.getPropertyValue(SOLR_USERNAME, String.class, null);
      this.password = config.getPropertyValue(SOLR_PASSWORD, String.class, null);
   }

   public void activate()
   {

   }

   public void close()
   {
      // shut down all active services
      cache.values().forEach(svc -> {
         if (svc.isEnabled())
            return;

         svc.shutdown();
      });

      cache.clear();
   }

   @Override
   public boolean isEnabled()
   {
      return solrEnabled;
   }

   @Override
   @SuppressWarnings("unchecked")  // type safety maintained by controlled insertion
   public <Entry, QueryCmd> IndexService<Entry, QueryCmd> getIndexService(IndexServiceStrategy<Entry, QueryCmd> indexCfg)
   {
      return cache.computeIfAbsent(indexCfg.getClass(),
            type -> new BasicIndexService<>(indexCfg));
   }

   // TODO really a mediator
   public class BasicIndexService<Entry, QueryCmd> implements IndexService<Entry, QueryCmd>
   {
      private final HttpSolrClient solr;
      private final IndexServiceStrategy<Entry, QueryCmd> indexCfg;

      private final AtomicBoolean enabled = new AtomicBoolean(true);

      public BasicIndexService(IndexServiceStrategy<Entry, QueryCmd> indexCfg)
      {
         this.indexCfg = indexCfg;

         // retrieve core specific properties
         String coreId = indexCfg.getCoreId();
         String core = config.getPropertyValue(format(SOLR_CORE_ID, coreId), String.class, coreId);
         boolean coreEnabled = config.getPropertyValue(format(SOLR_CORE_ENABLED, coreId), Boolean.class, true);

         URI coreUri = solrBaseUri.resolve(core);
         this.solr = (solrEnabled && coreEnabled) ? new HttpSolrClient(coreUri.toString()) : null;
         if (this.solr == null)
            enabled.set(false);
      }

      public void shutdown()
      {
         // TODO should we require that clients close these? That would imply that we need to do ref tracking, etc.

         if (!enabled.compareAndSet(true, false))
            return;

         try
         {
            solr.close();
         }
         catch (Exception e)
         {
            logger.log(Level.SEVERE, "Problems shutting down index service", e);
         }
      }

      @Override
      public boolean isEnabled()
      {
         return enabled.get();
      }

      @Override
      public SolrClient getSolrClient()
      {
         checkEnabled();
         return solr;
      }

      @Override
      public boolean isIndexed(Entry instance)
      {
         checkEnabled();

         String id = indexCfg.getEntryId(instance);
         return isIndexed(id);
      }

      @Override
      public void index(Entry instance)
      {
         checkEnabled();

         SolrInputDocument document = indexCfg.getDocument(instance);
         this.index(document);
      }

      @Override
      public void remove(Entry instance)
      {
         checkEnabled();

         String id = indexCfg.getEntryId(instance);
         this.remove(id);
      }

      @Override
      public void remove(String... ids)
      {
         checkEnabled();

         addIndexTask(() -> {
            try {
               solr.deleteById(Arrays.asList(ids));
            } catch (Exception e) {
               String template = "Failed to delete document [{0}] from Solr core {1} [host: {2}].";
               String msg = format(template, String.join(", ", ids), indexCfg.getCoreId(), solrBaseUri);
               logger.log(Level.SEVERE, msg, e);
            }
         });
      }

      @Override
      public QueryCmd createQuery()
      {
         checkEnabled();

         return indexCfg.createQuery(solr);
      }

      private void checkEnabled()
      {
         String msg = "The index service for Solr core {1} [host: {2}] has been stopped.";
         if (!enabled.get())
            throw new IllegalStateException(format(msg, indexCfg.getCoreId(), solrBaseUri));
      }

      private boolean isIndexed(String id)
      {
         checkEnabled();

         SolrQuery query = new SolrQuery();
         query.setQuery("id:" + id);
         try
         {
            QueryResponse response = solr.query(query);
            return  !response.getResults().isEmpty();
         }
         catch (IOException | SolrServerException e)
         {
            logger.log(Level.SEVERE, "Failed to query the work id: [" + id + "] from the SOLR server. " + e);
            return false;
         }
      }

      private void index(SolrInputDocument... docs)
      {
         if (enabled.get())
            throw new IllegalStateException("The index service has been stopped.");

         addIndexTask(() -> {
            try
            {
               solr.add(Arrays.asList(docs));
            }
            catch (SolrServerException | IOException ex)
            {
               List<String> ids = Arrays.stream(docs).map(doc -> {
                  return (String)doc.getField("id").getValue();
               }).collect(Collectors.toList());

               String template = "Failed to update document(s) [{0}] from Solr core {1} [host: {2}].";
               String msg = format(template, String.join(", ", ids), indexCfg.getCoreId(), solrBaseUri);
               logger.log(Level.SEVERE, msg, ex);
            }
         });
      }

      private void addIndexTask(Runnable task)
      {
         // TODO HACK execute immediately, commit every so often
         task.run();
         try {
            solr.commit();
         } catch (Exception ex) {
            logger.log(Level.SEVERE, "Failed to commit changes to Solr", ex);
         }
      }
   }
}