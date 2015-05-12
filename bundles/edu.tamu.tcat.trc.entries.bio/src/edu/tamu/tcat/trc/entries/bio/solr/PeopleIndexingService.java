package edu.tamu.tcat.trc.entries.bio.solr;

import java.io.IOException;
import java.net.URI;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.tamu.tcat.osgi.config.ConfigurationProperties;
import edu.tamu.tcat.trc.entries.bio.PeopleChangeEvent;
import edu.tamu.tcat.trc.entries.bio.PeopleIndexServiceManager;
import edu.tamu.tcat.trc.entries.bio.PeopleQueryCommand;
import edu.tamu.tcat.trc.entries.bio.PeopleRepository;
import edu.tamu.tcat.trc.entries.bio.PeopleSearchService;
import edu.tamu.tcat.trc.entries.bio.Person;

public class PeopleIndexingService implements PeopleIndexServiceManager, PeopleSearchService
{


   private final static Logger logger = Logger.getLogger(PeopleIndexingService.class.getName());

   /** Configuration property key that defines the URI for the Solr server. */
   public static final String SOLR_API_ENDPOINT = "solr.api.endpoint";

   /** Configuration property key that defines Solr core to be used for relationships. */
   public static final String SOLR_CORE = "catalogentries.authors.solr.core";

   // configured here for use by other classes in this package - these classes are effectively
   // delegates of this service's responsibilities
   static final ObjectMapper mapper;

   private PeopleRepository repo;
   private ConfigurationProperties config;
   private SolrServer solr;
   private AutoCloseable registration;

   static {
      mapper = new ObjectMapper();
      mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
   }

   public PeopleIndexingService()
   {
      // TODO Auto-generated constructor stub
   }

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


   private void onUpdate(PeopleChangeEvent evt)
   {
      try
      {
         switch(evt.getChangeType())
         {
            case CREATED:
               onCreate(evt.getPerson());
               break;
            case MODIFIED:
               onUpdate(evt.getPerson());
               break;
            case DELETED:
               onDelete(evt.getPerson());
               break;
            default:
               logger.log(Level.INFO, "Unexpected work change event [" + evt.getPersonId() +"]: " + evt.getChangeType());
         }

      }
      catch(Exception e)
      {
         logger.log(Level.WARNING, "Failed to update search indices following a change to work [" + evt.getPersonId() +"]: " + evt, e);
      }
   }

   private void onCreate(Person person)
   {
      PeopleSolrProxy proxy = PeopleSolrProxy.create(person);
      try
      {
         solr.add(proxy.getDocument());
         solr.commit();
      }
      catch (SolrServerException | IOException e)
      {
         logger.log(Level.SEVERE, "Failed to commit the person id: [" + person.getId() + "] to the SOLR server. " + e);
      }
   }

   private void onUpdate(Person person)
   {
      PeopleSolrProxy proxy = PeopleSolrProxy.create(person);
      try
      {
         solr.add(proxy.getDocument());
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

   @Override
   public PeopleQueryCommand createQueryCommand()
   {
      return new PeopleSolrQueryCommand(solr);
   }

}
