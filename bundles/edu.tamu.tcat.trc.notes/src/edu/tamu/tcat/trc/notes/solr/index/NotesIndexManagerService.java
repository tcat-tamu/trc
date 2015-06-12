package edu.tamu.tcat.trc.notes.solr.index;

import java.util.logging.Logger;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.tamu.tcat.trc.entries.notification.EntryUpdateHelper;
import edu.tamu.tcat.trc.entries.notification.UpdateEvent;
import edu.tamu.tcat.trc.entries.notification.UpdateListener;
import edu.tamu.tcat.trc.notes.Notes;
import edu.tamu.tcat.trc.notes.repo.NotesRepository;

public class NotesIndexManagerService implements NotesIndexManager
{
   private final static Logger logger = Logger.getLogger(NotesIndexManagerService.class.getName());

   /** Configuration property key that defines the URI for the Solr server. */
   public static final String SOLR_API_ENDPOINT = "solr.api.endpoint";

   public static final String SOLR_CORE = "trc.notes.solr.core";

   static final ObjectMapper mapper;
   static
   {
      mapper = new ObjectMapper();
      mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
   }

   private NotesRepository repo;

//   private SolrServer solr;
//   private ConfigurationProperties config;

//   private UpdateListener<Notes> ears;
   private AutoCloseable register;
   private EntryUpdateHelper<Notes> listener;

   public void setNotesRepo(NotesRepository repo)
   {
      this.repo = repo;
   }

   public void activate()
   {
      listener = new EntryUpdateHelper<Notes>();
      listener.register(new NotesUpdateListener());
      register = repo.register(new NotesUpdateListener());
      // construct Solr core
//      URI solrBaseUri = config.getPropertyValue(SOLR_API_ENDPOINT, URI.class);
//      String solrCore = config.getPropertyValue(SOLR_CORE, String.class);
//
//      URI coreUri = solrBaseUri.resolve(solrCore);
//      logger.info("Connecting to Solr Service [" + coreUri + "]");
//
//      solr = new HttpSolrServer(coreUri.toString());
   }

   private void onEvtChange(UpdateEvent<Notes> evt)
   {
      switch (evt.getAction())
      {
         case CREATE:
            onCreate(evt.getOriginal());
            break;
         case UPDATE:
            onUpdate(evt.getOriginal());
            break;
         case DELETE:
            onDelete(evt.getEntityId());
            break;
         default:
      }
   }

   private void onCreate(Notes note)
   {
      logger.info("A note has been created, and notified NotesIndexManager");
   }

   private void onUpdate(Notes note)
   {
      logger.info("A note has been updated, and notified NotesIndexManager");
   }

   private void onDelete(String id)
   {
      logger.info("A note has been deleted, and notified NotesIndexManager");
   }

   private class NotesUpdateListener implements UpdateListener<Notes>
   {

      private UpdateEvent<Notes> evt;

      @Override
      public boolean beforeUpdate(UpdateEvent<Notes> evt)
      {
         return true;
      }

      @Override
      public void afterUpdate(UpdateEvent<Notes> evt)
      {
         onEvtChange(evt);
      }

   }
}
