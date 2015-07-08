package edu.tamu.tcat.trc.notes.search.solr;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.common.SolrInputDocument;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.tamu.tcat.osgi.config.ConfigurationProperties;
import edu.tamu.tcat.trc.entries.notification.EntryUpdateHelper;
import edu.tamu.tcat.trc.entries.notification.UpdateListener;
import edu.tamu.tcat.trc.notes.Note;
import edu.tamu.tcat.trc.notes.repo.NoteChangeEvent;
import edu.tamu.tcat.trc.notes.repo.NotesRepository;

public class NotesIndexManagerService
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

   private SolrServer solr;
   private ConfigurationProperties config;

   private AutoCloseable register;
   private EntryUpdateHelper<NoteChangeEvent> listener;

   public void setNotesRepo(NotesRepository repo)
   {
      this.repo = repo;
   }

   public void setConfiguration(ConfigurationProperties config)
   {
      this.config = config;
   }

   public void activate()
   {
      listener = new EntryUpdateHelper<>();
      listener.register(new NotesUpdateListener());
      register = repo.register(new NotesUpdateListener());
      // construct Solr core
      URI solrBaseUri = config.getPropertyValue(SOLR_API_ENDPOINT, URI.class);
      String solrCore = config.getPropertyValue(SOLR_CORE, String.class);

      URI coreUri = solrBaseUri.resolve(solrCore);
      logger.info("Connecting to Solr Service [" + coreUri + "]");

      solr = new HttpSolrServer(coreUri.toString());
   }

   public void dispose()
   {
      listener = null;
      register = null;
      solr.shutdown();
   }

   private void onEvtChange(NoteChangeEvent evt)
   {
      try
      {
         switch (evt.getUpdateAction())
         {
            case CREATE:
               onCreate(evt.getNotes());
               break;
            case UPDATE:
               onUpdate(evt.getNotes());
               break;
            case DELETE:
               onDelete(evt.getEntityId());
               break;
            default:
               logger.log(Level.INFO, "Unexpected notes change event " + evt);
         }
      }
      catch (Exception ex)
      {
         logger.log(Level.WARNING, "Failed to update search indices following a change to notes: " + evt, ex);
      }
   }

   private void onCreate(Note note)
   {
      try
      {
         NoteDocument proxy = NoteDocument.create(note);
         postDocument(proxy);
      }
      catch (SolrServerException | IOException e)
      {
         logger.log(Level.SEVERE, "Failed to adapt Notes to indexable data transfer objects for note id: [" + note.getId() + "]", e);
         return;
      }
      catch (Exception e)
      {
         logger.log(Level.SEVERE, "Failed to adapt Notes to indexable data transfer objects for note id: [" + note.getId() + "]", e);
         return;
      }
   }

   private void onUpdate(Note note)
   {
      try
      {
         NoteDocument proxy = NoteDocument.update(note);
         postDocument(proxy);
      }
      catch (SolrServerException | IOException e)
      {
         logger.log(Level.SEVERE, "Failed to adapt Notes to indexable data transfer objects for note id: [" + note.getId() + "]", e);
         return;
      }
      catch (Exception e)
      {
         logger.log(Level.SEVERE, "Failed to adapt Notes to indexable data transfer objects for note id: [" + note.getId() + "]", e);
         return;
      }
   }

   private void postDocument(NoteDocument doc) throws SolrServerException, IOException
   {
      Collection<SolrInputDocument> solrDocs = new ArrayList<>();
      solrDocs.add(doc.getDocument());
      solr.add(solrDocs);
      solr.commit();
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
         logger.log(Level.SEVERE, "Failed to commit the note id: [" + id + "] to the SOLR server. " + e);
      }
   }

   private class NotesUpdateListener implements UpdateListener<NoteChangeEvent>
   {
      @Override
      public void handle(NoteChangeEvent evt)
      {
         onEvtChange(evt);
      }
   }
}
