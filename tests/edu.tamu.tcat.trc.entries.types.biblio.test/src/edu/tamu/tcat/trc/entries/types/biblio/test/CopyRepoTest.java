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
package edu.tamu.tcat.trc.entries.types.biblio.test;

import java.net.URI;
import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.List;
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
import edu.tamu.tcat.sda.catalog.psql.provider.PsqlDataSourceProvider;
import edu.tamu.tcat.trc.entries.repo.NoSuchCatalogRecordException;
import edu.tamu.tcat.trc.entries.types.biblio.copies.CopyReference;
import edu.tamu.tcat.trc.entries.types.biblio.copies.UpdateCanceledException;
import edu.tamu.tcat.trc.entries.types.biblio.postgres.copies.PsqlDigitalCopyLinkRepo;
import edu.tamu.tcat.trc.entries.types.biblio.repo.copies.EditCopyReferenceCommand;

public class CopyRepoTest
{

   private PostgreSqlExecutor exec;
   private SimpleFileConfigurationProperties config;
   private PsqlDataSourceProvider dsp;
   private PsqlDigitalCopyLinkRepo repo;

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

      repo = new PsqlDigitalCopyLinkRepo();
      repo.setDatabaseExecutor(exec);
      repo.activate();
   }

   @After
   public void tearDownTest() throws InterruptedException, ExecutionException
   {
      String sql = "DELETE FROM copy_references WHERE reference->>'associatedEntry' LIKE 'test%'";
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
   public void testReferenceCreation() throws UpdateCanceledException, InterruptedException, ExecutionException
   {
      EditCopyReferenceCommand editor = repo.create();
      editor.setAssociatedEntry(URI.create("test/works/1"));
      String id = "htid:000000000#ark+=13960=t00z72x8w";
      editor.setCopyId(id);
      editor.setTitle("Copy from my hard drive");
      editor.setSummary("A copy reference example.");
      editor.setRights("full view");

      Future<CopyReference> future = editor.execute();
      CopyReference ref = future.get();
   }

   @Test
   public void testReferenceRetrevial() throws UpdateCanceledException, InterruptedException, ExecutionException, NoSuchCatalogRecordException
   {
      URI workOneUri = URI.create("test/works/1");
      CopyReference ref1 = createReference("test/works/1", "htid:001383874#mdp.39015003847145");
      createReference("test/works/1/edition/2", "htid:001383874#mdp.39015003847145");
      createReference("test/works/1/edition/3", "htid:001383874#mdp.39015003847145");
      createReference("test/works/1/edition/3/volume/1", "htid:001383874#mdp.39015003847145");

      CopyReference retrieved = repo.get(ref1.getId());
      List<CopyReference> copies = repo.getCopies(workOneUri);

      Assert.assertEquals(4, copies.size());

      EditCopyReferenceCommand edit = repo.edit(retrieved.getId());
      edit.setSummary("A copy reference example that further illistrates the need for updating information.");
      Future<CopyReference> future = edit.execute();
      CopyReference updatedRef = future.get();

      Assert.assertNotEquals(updatedRef.getSummary(), retrieved.getSummary());

   }

   private CopyReference createReference(String workUri, String htid) throws UpdateCanceledException, InterruptedException, ExecutionException
   {
      EditCopyReferenceCommand editor = repo.create();

      editor.setAssociatedEntry(URI.create(workUri));
      editor.setCopyId(htid);
      editor.setTitle("Copy from my harddrive");
      editor.setSummary("A copy reference example.");
      editor.setRights("full view");

      Future<CopyReference> future = editor.execute();
      return future.get();
   }

   // TODO make sure we can get one copy back.
   // TODO make sure we can get all copies for a work back
   //       -- insert for test/works/1, test/works/1/edition/2, test/works/1/edition/3, test/works/1/edition/2/volume/1
   //       -- make sure all come back
   // TODO edit ref, save, make sure results come back OK
}
