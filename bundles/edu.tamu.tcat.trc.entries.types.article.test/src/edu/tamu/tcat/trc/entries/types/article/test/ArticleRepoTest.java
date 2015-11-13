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
package edu.tamu.tcat.trc.entries.types.article.test;

import java.net.URI;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.tamu.tcat.db.core.DataSourceException;
import edu.tamu.tcat.db.exec.sql.SqlExecutor;
import edu.tamu.tcat.osgi.config.ConfigurationProperties;
import edu.tamu.tcat.osgi.config.file.SimpleFileConfigurationProperties;
import edu.tamu.tcat.trc.entries.repo.NoSuchCatalogRecordException;
import edu.tamu.tcat.trc.entries.types.article.Article;
import edu.tamu.tcat.trc.entries.types.article.ArticleAuthor;
import edu.tamu.tcat.trc.entries.types.article.dto.ArticleAuthorDTO;
import edu.tamu.tcat.trc.entries.types.article.dto.ArticleDTO;
import edu.tamu.tcat.trc.entries.types.article.postgres.PsqlArticleRepo;
import edu.tamu.tcat.trc.entries.types.article.repo.EditArticleCommand;
import edu.tamu.tcat.trc.test.TestUtils;

public class ArticleRepoTest
{

   private SqlExecutor exec;
   private ConfigurationProperties config;
   private PsqlArticleRepo repo;

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
      config = TestUtils.loadConfigFile();
      exec = TestUtils.initPostgreSqlExecutor(config);

      repo = new PsqlArticleRepo();
      repo.setDatabaseExecutor(exec);
      repo.activate();
   }

   @After
   public void tearDownTest() throws Exception
   {
      String sql = "DELETE FROM articles WHERE article->>'associatedEntity' LIKE 'articles/%'";
      Future<Void> future = exec.submit((conn) -> {
         try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.executeUpdate();
            return null;
         }
      });

      future.get();
      repo.dispose();

      exec.close();

      if (config instanceof SimpleFileConfigurationProperties)
         ((SimpleFileConfigurationProperties)config).dispose();
   }

   @Test
   public void createArticle() throws InterruptedException, ExecutionException, NoSuchCatalogRecordException
   {
      ArticleDTO article = createArticleDTO();
      EditArticleCommand command = repo.create();
      command.setAll(article);
      article.id = command.execute().get();


      Article article2 = repo.get(article.id);
      ArticleDTO articleDTO = ArticleDTO.create(article2);

      Assert.assertEquals("Articles do not match", article.id, articleDTO.id);
      Assert.assertEquals("Articles do not match", article.title, articleDTO.title);
      Assert.assertEquals("Articles do not match", article.associatedEntity, articleDTO.associatedEntity);
      Assert.assertEquals("Articles do not match", article.authorId, articleDTO.authorId);
      Assert.assertEquals("Articles do not match", article.mimeType, articleDTO.mimeType);
      Assert.assertEquals("Articles do not match", article.content, articleDTO.content);
   }

   @Test
   public void updateArticle() throws InterruptedException, ExecutionException, NoSuchCatalogRecordException
   {
      ArticleDTO article = createArticleDTO();
      EditArticleCommand command = repo.create();
      command.setAll(article);
      article.id = command.execute().get();
      article.lastModified = new Date();
      article.title = "The New & Everlasting Title";
      article.content = "<H1>The New and Everlasting Title<H1> <p>As time passes so do many articles. In this" +
                  "particular case, this article will not be passed on. It will forever be made available" +
                  "through this testing process. </p> " +
                  " <p> To change the article, we need to provide some type of update to it.</p>";

      EditArticleCommand updateCommand = repo.edit(article.id);
      updateCommand.setAll(article);
      updateCommand.execute();

      Article article2 = repo.get(article.id);
      ArticleDTO articleDTO = ArticleDTO.create(article2);

      Assert.assertEquals("Articles do not match", article.id, articleDTO.id);
      Assert.assertEquals("Articles do not match", article.title, articleDTO.title);
      Assert.assertEquals("Articles do not match", article.associatedEntity, articleDTO.associatedEntity);
      Assert.assertEquals("Articles do not match", article.authorId, articleDTO.authorId);
      Assert.assertEquals("Articles do not match", article.mimeType, articleDTO.mimeType);
      Assert.assertEquals("Articles do not match", article.content, articleDTO.content);

   }

   @Test
   public void deleteArticle() throws InterruptedException, ExecutionException
   {
      ArticleDTO article = createArticleDTO();
      EditArticleCommand command = repo.create();
      command.setAll(article);
      article.id = command.execute().get();

      Boolean removed = repo.remove(article.id).get();
      Assert.assertEquals("Article was not removed", Boolean.TRUE, removed);
      try
      {
         repo.get(article.id);
         Assert.fail();
      }
      catch(NoSuchCatalogRecordException e)
      {
         Assert.assertTrue("Article has been removed", true);
      }

   }

   private ArticleDTO createArticleDTO()
   {
      ArticleDTO article = new ArticleDTO();
      List<ArticleAuthorDTO> authors = new ArrayList<ArticleAuthorDTO>();
      
      ArticleAuthorDTO author1 = new ArticleAuthorDTO();
      author1.id = "n_audenaert";
      author1.label = "Neal Audenaert";
      authors.add(author1);
      
      ArticleAuthorDTO author2 = new ArticleAuthorDTO();
      author1.id = "j_mitchell";
      author1.label = "Jesse Mitchell";
      authors.add(author2);
      
      
      article.title = "The New and Everlasting Title";
      article.associatedEntity = URI.create("articles/1");
      article.authors = authors;
      article.articleAbstract = "The abstract of this article.";
      article.publication = new Date();
      article.authorId = "d25d7b89-6634-4895-89c1-7024fc3d5396";
      article.mimeType = "HTML";
      article.content = "<H1>The New and Everlasting Title<H1> <p>As time passes so do many articles. In this" +
                        "particular case, this article will not be passed on. It will forever be made available" +
                        "through this testing process. </p>";

      return article;
   }

}
