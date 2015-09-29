/*
 * Copyright 2015 Texas A&M Engineering Experiment Station
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.tamu.tcat.trc.notes.search.solr;

import java.io.IOException;
import java.net.URI;
import java.text.MessageFormat;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.tamu.tcat.osgi.config.ConfigurationProperties;
import edu.tamu.tcat.trc.entries.notification.UpdateListener;
import edu.tamu.tcat.trc.entries.repo.NoSuchCatalogRecordException;
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
      try
      {
         if (register != null)
            register.close();
      }
      catch (Exception ex)
      {
         logger.log(Level.WARNING, "Failed to unregisters notes repository listener.", ex);
      }

      register = null;

      try
      {
         solr.shutdown();
      }
      catch (Exception ex)
      {
         logger.log(Level.WARNING, "Failed to shutdown solr server client for notes index manager", ex);
      }
   }

   private void onEvtChange(NoteChangeEvent evt)
   {
      try
      {
         String id = evt.getEntityId();
         switch (evt.getUpdateAction())
         {
            case CREATE:
               index(id, NoteDocument::create);
               break;
            case UPDATE:
               index(id, NoteDocument::update);
               break;
            case DELETE:
               solr.deleteById(id);
               break;
            default:
               logger.log(Level.INFO, "Unexpected notes change event " + evt);
         }

         solr.commit();
      }
      catch (Exception ex)
      {
         logger.log(Level.WARNING, "Failed to update search indices following a change to notes: " + evt, ex);
      }
   }

   private void index(String id, Function<Note, NoteDocument> adapter)
         throws SolrServerException, IOException, NoSuchCatalogRecordException
   {
      UUID uuid = (id != null && !id.trim().isEmpty()) ? UUID.fromString(id) : null;
      Note note = repo.get(uuid);
      Objects.requireNonNull(note,
            MessageFormat.format("Failed to retrieve note with id {0}", id));

      NoteDocument proxy = adapter.apply(note);
      solr.add(proxy.getDocument());
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
