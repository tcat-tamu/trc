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


import java.net.URI;
import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.tamu.tcat.db.core.DataSourceException;
import edu.tamu.tcat.db.postgresql.exec.PostgreSqlExecutor;
import edu.tamu.tcat.osgi.config.file.SimpleFileConfigurationProperties;
import edu.tamu.tcat.trc.entries.repo.NoSuchCatalogRecordException;
import edu.tamu.tcat.trc.notes.Note;
import edu.tamu.tcat.trc.notes.dto.NoteDTO;
import edu.tamu.tcat.trc.notes.postgres.PsqlNotesRepo;
import edu.tamu.tcat.trc.notes.repo.EditNoteCommand;
import edu.tamu.tcat.trc.repo.postgres.PostgresDataSourceProvider;

public class NotesRepoTest
{

   private PostgreSqlExecutor exec;
   private SimpleFileConfigurationProperties config;
   private PostgresDataSourceProvider dsp;
   private PsqlNotesRepo repo;


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

      repo = new PsqlNotesRepo();
      repo.setDatabaseExecutor(exec);
      repo.activate();
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
      repo.dispose();
      exec.close();
      dsp.dispose();
      config.dispose();
   }

   @Test
   public void createNote() throws InterruptedException, ExecutionException, NoSuchCatalogRecordException
   {
      NoteDTO note = createNoteDTO();
      EditNoteCommand command = repo.create();
      command.setAll(note);
      note.id = command.execute().get();


      Note note2 = repo.get(note.id);
      NoteDTO noteDTO = NoteDTO.create(note2);

      Assert.assertEquals("Notes do not match", note.id, noteDTO.id);
      Assert.assertEquals("Notes do not match", note.associatedEntity, noteDTO.associatedEntity);
      Assert.assertEquals("Notes do not match", note.authorId, noteDTO.authorId);
      Assert.assertEquals("Notes do not match", note.mimeType, noteDTO.mimeType);
      Assert.assertEquals("Notes do not match", note.content, noteDTO.content);
   }

   @Test
   public void updateNote() throws InterruptedException, ExecutionException, NoSuchCatalogRecordException
   {
      NoteDTO note = createNoteDTO();
      EditNoteCommand command = repo.create();
      command.setAll(note);
      note.id = command.execute().get();

      note.content = "<H1>The New and Everlasting Title<H1> <p>As time passes so do many articles. In this" +
                  "particular case, this article will not be passed on. It will forever be made available" +
                  "through this testing process. </p> " +
                  " <p> To change the article, we need to provide some type of update to it.</p>";

      EditNoteCommand updateCommand = repo.edit(note.id);
      updateCommand.setAll(note);
      updateCommand.execute();

      Note note2 = repo.get(note.id);
      NoteDTO noteDTO = NoteDTO.create(note2);

      Assert.assertEquals("Notes do not match", note.id, noteDTO.id);
      Assert.assertEquals("Notes do not match", note.associatedEntity, noteDTO.associatedEntity);
      Assert.assertEquals("Notes do not match", note.authorId, noteDTO.authorId);
      Assert.assertEquals("Notes do not match", note.mimeType, noteDTO.mimeType);
      Assert.assertEquals("Notes do not match", note.content, noteDTO.content);

   }

   @Test
   public void deleteNote() throws InterruptedException, ExecutionException
   {
      NoteDTO note = createNoteDTO();
      EditNoteCommand command = repo.create();
      command.setAll(note);
      note.id = command.execute().get();

      Boolean removed = repo.remove(note.id).get();
      Assert.assertEquals("Note was not removed", Boolean.TRUE, removed);
      try
      {
         repo.get(note.id);
         Assert.fail();
      }
      catch(NoSuchCatalogRecordException e)
      {
         Assert.assertTrue("Article has been removed", true);
      }

   }

   private NoteDTO createNoteDTO()
   {
      NoteDTO article = new NoteDTO();
      article.associatedEntity = URI.create("notes/1");
      article.authorId = "d25d7b89-6634-4895-89c1-7024fc3d5396";
      article.mimeType = "Text";
      article.content = "I'm not sure that I agree with the information that is contained within this work.";

      return article;
   }

}
