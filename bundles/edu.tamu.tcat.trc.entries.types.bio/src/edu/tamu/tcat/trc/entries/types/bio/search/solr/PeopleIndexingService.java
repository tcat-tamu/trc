package edu.tamu.tcat.trc.entries.types.bio.search.solr;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.common.SolrInputDocument;

import edu.tamu.tcat.osgi.config.ConfigurationProperties;
import edu.tamu.tcat.trc.entries.search.SearchException;
import edu.tamu.tcat.trc.entries.search.solr.impl.TrcQueryBuilder;
import edu.tamu.tcat.trc.entries.types.bio.Person;
import edu.tamu.tcat.trc.entries.types.bio.repo.PeopleRepository;
import edu.tamu.tcat.trc.entries.types.bio.repo.PersonChangeEvent;
import edu.tamu.tcat.trc.entries.types.bio.search.PeopleIndexServiceManager;
import edu.tamu.tcat.trc.entries.types.bio.search.PeopleQueryCommand;
import edu.tamu.tcat.trc.entries.types.bio.search.PeopleSearchService;

public class PeopleIndexingService implements PeopleIndexServiceManager, PeopleSearchService
{
   private final static Logger logger = Logger.getLogger(PeopleIndexingService.class.getName());

   /** Configuration property key that defines the URI for the Solr server. */
   public static final String SOLR_API_ENDPOINT = "solr.api.endpoint";

   /** Configuration property key that defines Solr core to be used for relationships. */
   public static final String SOLR_CORE = "catalogentries.authors.solr.core";

   private PeopleRepository repo;
   private ConfigurationProperties config;
   private SolrServer solr;
   private AutoCloseable registration;

   public void setRepo(PeopleRepository repo)
   {
      this.repo = repo;
   }

   public void setConfiguration(ConfigurationProperties cp)
   {
      this.config = cp;
   }

   public void activate()
   {
      logger.fine("Activating PeopleIndexingService");
      Objects.requireNonNull(repo, "No people repository supplied.");
      registration = repo.addUpdateListener(this::onUpdate);

      // construct Solr core
      URI solrBaseUri = config.getPropertyValue(SOLR_API_ENDPOINT, URI.class);
      String solrCore = config.getPropertyValue(SOLR_CORE, String.class);

      Objects.requireNonNull(solrBaseUri, "Failed to initialize PeopleIndexing Service. Solr API endpoint is not configured.");
      Objects.requireNonNull(solrCore, "Failed to initialize PeopleIndexing Service. Solr core is not configured.");

      URI coreUri = solrBaseUri.resolve(solrCore);
      logger.info("Connecting to Solr Service [" + coreUri + "]");

      solr = new HttpSolrServer(coreUri.toString());
   }

   public void deactivate()
   {
      logger.info("Deactivating PeopleIndexingService");

      unregisterRepoListener();
      releaseSolrConnection();
   }

   @Override
   public PeopleQueryCommand createQueryCommand() throws SearchException
   {
      return new PeopleSolrQueryCommand(solr, new TrcQueryBuilder(solr, new BioSolrConfig()));
   }

   private void unregisterRepoListener()
   {
      if (registration != null)
      {
         try
         {
            registration.close();
         }
         catch (Exception e)
         {
            logger.log(Level.WARNING, "Failed to unregister update listener on people repository.", e);
         }
         finally {
            registration = null;
         }
      }
   }

   private void releaseSolrConnection()
   {
      logger.fine("Releasing connection to Solr server");
      if (solr != null)
      {
         try
         {
            solr.shutdown();
         }
         catch (Exception e)
         {
            logger.log(Level.WARNING, "Failed to cleanly shut down connection to Solr server.", e);
         }
      }
   }

   private void onUpdate(PersonChangeEvent evt)
   {
      try
      {
         switch(evt.getUpdateAction())
         {
            case CREATE:
               onCreate(evt.getPerson());
               break;
            case UPDATE:
               onUpdate(evt.getPerson());
               break;
            case DELETE:
               onDelete(evt.getPerson());
               break;
            default:
               logger.log(Level.INFO, "Unexpected change event " + evt);
         }
      }
      catch(Exception e)
      {
         logger.log(Level.WARNING, "Failed to update search indices following a change " + evt, e);
      }
   }

   private void onCreate(Person person)
   {
      Collection<SolrInputDocument> solrDocs = new ArrayList<>();
      try
      {
         BioDocument doc = BioDocument.create(person);
         solrDocs.add(doc.getDocument());
      }
      catch (Exception e)
      {
         logger.log(Level.SEVERE, "Failed to adapt Person to indexable data transfer objects for person id: [" + person.getId() + "]", e);
         return;
      }

      try
      {
         solr.add(solrDocs);
         solr.commit();
      }
      catch (SolrServerException | IOException e)
      {
         logger.log(Level.SEVERE, "Failed to commit the person id: [" + person.getId() + "] to the SOLR server. " + e);
      }
   }

   private void onUpdate(Person person)
   {
      Collection<SolrInputDocument> solrDocs = new ArrayList<>();
      try
      {
         BioDocument doc = BioDocument.create(person);
         solrDocs.add(doc.getDocument());
      }
      catch (Exception e)
      {
         logger.log(Level.SEVERE, "Failed to adapt Person to indexable data transfer objects for person id: [" + person.getId() + "]", e);
         return;
      }

      try
      {
         solr.add(solrDocs);
         solr.commit();
      }
      catch (SolrServerException | IOException e)
      {
         logger.log(Level.SEVERE, "Failed to commit the person id: [" + person.getId() + "] to the SOLR server. " + e);
      }
   }

   private void onDelete(Person person)
   {
      String id = person.getId();
      try
      {
         solr.deleteById(id);
         solr.commit();
      }
      catch(SolrServerException | IOException e)
      {
         logger.log(Level.SEVERE, "Failed to delete the person id: [" + id + "] from the SOLR server. " + e);
      }
   }
}
