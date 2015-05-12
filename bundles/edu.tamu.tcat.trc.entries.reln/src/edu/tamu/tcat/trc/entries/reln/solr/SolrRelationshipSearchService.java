package edu.tamu.tcat.trc.entries.reln.solr;

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
import edu.tamu.tcat.trc.entries.reln.Relationship;
import edu.tamu.tcat.trc.entries.reln.RelationshipChangeEvent;
import edu.tamu.tcat.trc.entries.reln.RelationshipQueryCommand;
import edu.tamu.tcat.trc.entries.reln.RelationshipRepository;
import edu.tamu.tcat.trc.entries.reln.RelationshipSearchIndexManager;
import edu.tamu.tcat.trc.entries.reln.RelationshipSearchService;
import edu.tamu.tcat.trc.entries.reln.RelationshipTypeRegistry;

/**
 *  TODO include documentation about expected fields and format of the solr core.
 *
 */
public class SolrRelationshipSearchService implements RelationshipSearchIndexManager, RelationshipSearchService
{
   private final static Logger logger = Logger.getLogger(SolrRelationshipSearchService.class.getName());

   /** Configuration property key that defines the URI for the Solr server. */
   public static final String SOLR_API_ENDPOINT = "solr.api.endpoint";

   /** Configuration property key that defines Solr core to be used for relationships. */
   public static final String SOLR_CORE = "catalogentries.relationships.solr.core";

   // configured here for use by other classes in this package - these classes are effectively
   // delegates of this service's responsibilities
   static final ObjectMapper mapper;
   static {
      mapper = new ObjectMapper();
      mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
   }


   private RelationshipRepository repo;
   private AutoCloseable registration;

   private SolrServer solr;
   private ConfigurationProperties config;
   private RelationshipTypeRegistry typeReg;

   public SolrRelationshipSearchService()
   {
   }

   // HACK: Relationships are set to the db in an asynchronous matter. It can not be quarenteed that db operations will be
   //       completed before the next operation starts, causing an error. The create/update/delete process "should" not have
   //       that many requests to see this occur.
   public void setRelationshipRepo(RelationshipRepository repo)
   {
      this.repo = repo;
   }

   public void setConfiguration(ConfigurationProperties config)
   {
      this.config = config;
   }

   public void setTypeRegistry(RelationshipTypeRegistry typeReg)
   {
      this.typeReg = typeReg;
   }

   public void activate()
   {
      logger.fine("Activating SolrRelationshipSearchService");
      Objects.requireNonNull(repo, "No relationship repository supplied.");
      registration = repo.addUpdateListener(this::onUpdate);

      // construct Solr core
      URI solrBaseUri = config.getPropertyValue(SOLR_API_ENDPOINT, URI.class);
      String solrCore = config.getPropertyValue(SOLR_CORE, String.class);

      URI coreUri = solrBaseUri.resolve(solrCore);
      logger.info("Connecting to Solr Service [" + coreUri + "]");

      solr = new HttpSolrServer(coreUri.toString());
   }

   public void deactivate()
   {
      logger.info("Deactivating SolrRelationshipSearchService");

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
            logger.log(Level.WARNING, "Failed to unregister update listener on relationship repository.", e);
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

   private void onUpdate(RelationshipChangeEvent evt)
   {
      // NOTE: since this is an event listener, it serves as a fault barrier
      try
      {
         switch (evt.getChangeType())
         {
            case CREATED:
               onCreate(evt.getRelationship());
               break;
            case MODIFIED:
               onChange(evt.getRelationship());
               break;
            case DELETED:
               onDelete(evt.getRelationshipId());
               break;
            default:
               logger.log(Level.INFO, "Unexpected relationship change event [" + evt.getRelationshipId() +"]: " + evt.getChangeType());
         }
      }
      catch (Exception ex)
      {
         logger.log(Level.WARNING, "Failed to update search indices following a change to relationship [" + evt.getRelationshipId() +"]: " + evt, ex);
      }
   }

   private void onCreate(Relationship reln)
   {
      try
      {
         RelnSolrProxy proxy = RelnSolrProxy.create(reln);
         solr.add(proxy.getDocument());
         solr.commit();
      }
      catch (SolrServerException | IOException e)
      {
         logger.log(Level.SEVERE, "Failed to commit new relationship id: [" + reln.getId() + "] to the SOLR server. " + e);
      }
   }

   private void onChange(Relationship reln)
   {
      try
      {
         RelnSolrProxy proxy = RelnSolrProxy.update(reln);
         solr.add(proxy.getDocument());
         solr.commit();
      }
      catch (SolrServerException | IOException e)
      {
         logger.log(Level.SEVERE, "Failed to commit the updated relationship id: [" + reln.getId() + "] to the SOLR server. " + e);
      }
   }

   private void onDelete(String id)
   {
      try
      {
         solr.deleteById(id);
         solr.commit();
      }
      catch (SolrServerException | IOException e)
      {
         logger.log(Level.SEVERE, "Failed to delete relationship id: [" + id + "] to the SOLR server. " + e);
      }
   }

   @Override
   public Iterable<Relationship> findRelationshipsFor(URI entry)
   {
      return createQueryCommand().forEntity(entry).getResults();
   }

   @Override
   public Iterable<Relationship> findRelationshipsBy(URI creator)
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public RelationshipQueryCommand createQueryCommand()
   {
      return new RelationshipSolrQueryCommand(solr, typeReg);
   }

}

//Here is an example of how to do a partial update via Solr’s Java client, SolrJ:
//
//// create the SolrJ client
//HttpSolrServer client = new HttpSolrServer("http://localhost:8983/solr");
//
//// create the document
//SolrInputDocument sdoc = new SolrInputDocument();
//sdoc.addField("id","book1");
//Map<String,Object> fieldModifier = new HashMap<>(1);
//fieldModifier.put("set","Cyberpunk"); set or replace a value, remove value if null is specified as the new value
//fieldModifier.put("add","Cyberpunk"); adds a value to a list
//fieldModifier.put("remove","Cyberpunk"); remove or list of values from a list
//fieldModifier.put("inc",1); increment a number
//sdoc.addField("cat", fieldModifier);  // add the map as the field value
//
//client.add( sdoc );  // send it to the solr server
//
//client.shutdown();
