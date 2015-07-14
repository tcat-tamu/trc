package edu.tamu.tcat.trc.entries.types.article.test;

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
import edu.tamu.tcat.sda.catalog.psql.provider.PsqlDataSourceProvider;
import edu.tamu.tcat.trc.entries.repo.NoSuchCatalogRecordException;
import edu.tamu.tcat.trc.entries.types.article.Article;
import edu.tamu.tcat.trc.entries.types.article.dto.ArticleDTO;
import edu.tamu.tcat.trc.entries.types.article.postgres.PsqlArticleRepo;
import edu.tamu.tcat.trc.entries.types.article.repo.EditArticleCommand;

public class ArticleRepoTest
{

   private PostgreSqlExecutor exec;
   private SimpleFileConfigurationProperties config;
   private PsqlDataSourceProvider dsp;
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
      Map<String, Object> params = new HashMap<>();
      params.put(SimpleFileConfigurationProperties.PROP_FILE, "config.path");
      config = new SimpleFileConfigurationProperties();
      config.activate(params);

      dsp = new PsqlDataSourceProvider();
      dsp.bind(config);
      dsp.activate();

      exec = new PostgreSqlExecutor();
      exec.init(dsp);

      repo = new PsqlArticleRepo();
      repo.setDatabaseExecutor(exec);
      repo.activate();
   }

   @After
   public void tearDownTest() throws InterruptedException, ExecutionException
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
      dsp.dispose();
      config.dispose();
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
      article.title = "The New and Everlasting Title";
      article.associatedEntity = URI.create("articles/1");
      article.authorId = "d25d7b89-6634-4895-89c1-7024fc3d5396";
      article.mimeType = "HTML";
      article.content = "<H1>The New and Everlasting Title<H1> <p>As time passes so do many articles. In this" +
                        "particular case, this article will not be passed on. It will forever be made available" +
                        "through this testing process. </p>";

      return article;
   }

}
