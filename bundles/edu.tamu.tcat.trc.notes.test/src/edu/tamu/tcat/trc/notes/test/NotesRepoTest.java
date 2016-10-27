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
package edu.tamu.tcat.trc.notes.test;


import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.tamu.tcat.account.Account;
import edu.tamu.tcat.db.core.DataSourceException;
import edu.tamu.tcat.db.postgresql.exec.PostgreSqlExecutor;
import edu.tamu.tcat.osgi.config.file.SimpleFileConfigurationProperties;
import edu.tamu.tcat.trc.entries.types.biblio.repo.BibliographicEntryRepository;
import edu.tamu.tcat.trc.impl.psql.entries.DbEntryRepositoryRegistry;
import edu.tamu.tcat.trc.impl.psql.services.notes.NotesServiceFactory;
import edu.tamu.tcat.trc.impl.psql.services.notes.NotesServiceFactory.NotesRepoImpl;
import edu.tamu.tcat.trc.repo.postgres.PostgresDataSourceProvider;
import edu.tamu.tcat.trc.resolver.EntryId;
import edu.tamu.tcat.trc.services.notes.EditNoteCommand;
import edu.tamu.tcat.trc.services.notes.Note;
import edu.tamu.tcat.trc.services.notes.NotesService;

public class NotesRepoTest
{

   private PostgreSqlExecutor exec;
   private SimpleFileConfigurationProperties config;
   private PostgresDataSourceProvider dsp;

   private NotesServiceFactory factory;
   private NotesRepoImpl notesSvc;


   @BeforeClass
   public static void setUp()
   {
      // TODO spin up DB, etc
   }

   @AfterClass
   public static void tearDown()
   {

   }

   @Before
   public void setupTest() throws DataSourceException
   {
      Map<String, Object> params = new HashMap<>();
      params.put(SimpleFileConfigurationProperties.PROP_FILE, "config.path");
      config = new SimpleFileConfigurationProperties();
      config.activate(params);

      dsp = new PostgresDataSourceProvider();
      dsp.bind(config);
      dsp.activate();

      exec = new PostgreSqlExecutor();
      exec.init(dsp);

      DbEntryRepositoryRegistry repoRegistry = new DbEntryRepositoryRegistry();
      repoRegistry.setConfiguration(config);
      repoRegistry.setIdFactory(ctx -> () -> UUID.randomUUID().toString());
      repoRegistry.setSqlExecutor(exec);
      repoRegistry.activate();

      factory = new NotesServiceFactory(repoRegistry, null);
      notesSvc = factory.getService(NotesService.makeContext(null, "test"));
   }

   @After
   public void tearDownTest() throws InterruptedException, ExecutionException
   {
      String sql = "DELETE FROM notes WHERE note->>'associatedEntity' LIKE 'notes/%'";
      Future<Void> future = exec.submit((conn) -> {
         try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.executeUpdate();
            return null;
         }
      });

      future.get();
      factory.shutdown();
      exec.close();
      dsp.dispose();
      config.dispose();
   }

   @Test
   public void createNote() throws Exception
   {
      EditNoteCommand command = notesSvc.create();
      applyStandardFields(command);
      String noteId = command.execute().get();


      Note note = notesSvc.get(noteId).orElseThrow(() -> new IllegalStateException());;

      Assert.assertEquals("Notes do not match", noteId, note.getId());
      Assert.assertEquals("Notes do not match", "1", note.getAssociatedEntry().id);
      Assert.assertEquals("Notes do not match", BibliographicEntryRepository.ENTRY_TYPE_ID, note.getAssociatedEntry().type);
      Assert.assertEquals("Notes do not match", UUID.fromString("d25d7b89-6634-4895-89c1-7024fc3d5396"), note.getAuthor().getId());
      Assert.assertEquals("Notes do not match", "Test User", note.getAuthor().getDisplayName());
      Assert.assertEquals("Notes do not match", "text/plain", note.getMimeType());
      Assert.assertEquals("Notes do not match", "I'm not sure that I agree with the information that is contained within this work.", note.getContent());
   }

   @Test
   public void updateNote() throws Exception
   {
      EditNoteCommand command = notesSvc.create();
      applyStandardFields(command);
      String noteId = command.execute().get();

      Note note = notesSvc.get(noteId).orElseThrow(() -> new IllegalStateException());

      String content = "<H1>The New and Everlasting Title<H1> <p>As time passes so do many articles. In this" +
                  "particular case, this article will not be passed on. It will forever be made available" +
                  "through this testing process. </p> " +
                  " <p> To change the article, we need to provide some type of update to it.</p>";

      EditNoteCommand updateCommand = notesSvc.edit(noteId);
      updateCommand.setContent(content);
      updateCommand.execute();

      Note note2 = notesSvc.get(noteId).orElseThrow(() -> new IllegalStateException());

      Assert.assertEquals("Notes do not match", noteId, note2.getId());
      Assert.assertEquals("Notes do not match", "1", note2.getAssociatedEntry().id);
      Assert.assertEquals("Notes do not match", BibliographicEntryRepository.ENTRY_TYPE_ID, note2.getAssociatedEntry().type);
      Assert.assertEquals("Notes do not match", UUID.fromString("d25d7b89-6634-4895-89c1-7024fc3d5396"), note2.getAuthor().getId());
      Assert.assertEquals("Notes do not match", "Test User", note2.getAuthor().getDisplayName());
      Assert.assertEquals("Notes do not match", "text/plain", note2.getMimeType());
      Assert.assertEquals("Notes do not match", content, note2.getContent());

   }

   @Test
   public void deleteNote() throws InterruptedException, ExecutionException
   {
      EditNoteCommand command = notesSvc.create();
      applyStandardFields(command);

      String noteId = command.execute().get();

      Boolean removed = notesSvc.remove(noteId).get();
      Assert.assertEquals("Note was not removed", Boolean.TRUE, removed);

      Optional<Note> noteRef = notesSvc.get(noteId);
      Assert.assertTrue("Article has not been removed", !noteRef.isPresent());
   }


   private void applyStandardFields(EditNoteCommand command)
   {
      String authorId = "d25d7b89-6634-4895-89c1-7024fc3d5396";
      TestAccount acct = new TestAccount(UUID.fromString(authorId), "Test User");

      EntryId ref = new EntryId();
      ref.type = BibliographicEntryRepository.ENTRY_TYPE_ID;
      ref.id = "1";

      command.setAssociatedEntry(ref);
      command.setAuthor(acct);
      command.setContent("I'm not sure that I agree with the information that is contained within this work.");
      command.setMimeType("text/plain");
   }

   private static class TestAccount implements Account
   {
      private final UUID id;
      private final String displayName;

      public TestAccount(UUID id, String displayName)
      {
         this.id = id;
         this.displayName = displayName;
      }

      @Override
      public UUID getId()
      {
         return id;
      }

      @Override
      public String getDisplayName()
      {
         return displayName;
      }

      @Override
      public boolean isActive()
      {
         return true;
      }

   }

}
