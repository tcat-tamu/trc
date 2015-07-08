package edu.tamu.tcat.trc.notes.test;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
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
import edu.tamu.tcat.sda.catalog.psql.provider.PsqlDataSourceProvider;
import edu.tamu.tcat.trc.notes.Note;
import edu.tamu.tcat.trc.notes.dto.NoteDTO;
import edu.tamu.tcat.trc.notes.postgres.PsqlNotesRepo;
import edu.tamu.tcat.trc.notes.repo.EditNoteCommand;
import edu.tamu.tcat.trc.notes.search.solr.NotesIndexManagerService;

public class NotesRepoTest
{

   private PostgreSqlExecutor exec;
   private SimpleFileConfigurationProperties config;
   private PsqlDataSourceProvider dsp;
   private PsqlNotesRepo repo;
   private NotesIndexManagerService noteService;

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

      dsp = new PsqlDataSourceProvider();
      dsp.bind(config);
      dsp.activate();

      exec = new PostgreSqlExecutor();
      exec.init(dsp);

      repo = new PsqlNotesRepo();
      repo.setDatabaseExecutor(exec);
      repo.activate();

      noteService = new NotesIndexManagerService();
      noteService.setNotesRepo(repo);
      noteService.setConfiguration(config);
      noteService.activate();
   }

   @After
   public void tearDownTest() throws InterruptedException, ExecutionException
   {


   }

   @Test
   public void testNotesCreation() throws InterruptedException, ExecutionException
   {
      EditNoteCommand create = repo.create();
      NoteDTO updateDTO = new NoteDTO();
      updateDTO.authorId = UUID.randomUUID().toString();
      updateDTO.associatedEntity = URI.create("notes/1");
      updateDTO.content = "The contents of the note";
      updateDTO.mimeType = "Text";
      create.update(updateDTO );

      Future<Note> execute = create.execute();
      Note notes = execute.get();

      Assert.assertEquals(updateDTO.authorId, notes.getAuthorId());
   }
}
