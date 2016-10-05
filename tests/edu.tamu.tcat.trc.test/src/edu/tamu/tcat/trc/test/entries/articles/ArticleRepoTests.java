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
package edu.tamu.tcat.trc.test.entries.articles;

import static java.text.MessageFormat.format;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.sql.PreparedStatement;
import java.util.concurrent.Future;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.tamu.tcat.db.core.DataSourceException;
import edu.tamu.tcat.osgi.config.ConfigurationProperties;
import edu.tamu.tcat.osgi.config.file.SimpleFileConfigurationProperties;
import edu.tamu.tcat.trc.entries.types.article.Article;
import edu.tamu.tcat.trc.entries.types.article.impl.ArticleEntryService;
import edu.tamu.tcat.trc.entries.types.article.repo.ArticleRepository;
import edu.tamu.tcat.trc.entries.types.article.repo.EditArticleCommand;
import edu.tamu.tcat.trc.impl.psql.entries.DbEntryRepositoryRegistry;
import edu.tamu.tcat.trc.repo.IdFactoryProvider;
import edu.tamu.tcat.trc.resolver.EntryReference;
import edu.tamu.tcat.trc.resolver.EntryResolver;
import edu.tamu.tcat.trc.resolver.EntryResolverRegistry;
import edu.tamu.tcat.trc.repo.DocumentNotFoundException;
import edu.tamu.tcat.trc.test.ClosableSqlExecutor;
import edu.tamu.tcat.trc.test.TestUtils;

public class ArticleRepoTests
{

   private static final String TBL_NAME = "test_articles";

   private static final String ARTICLE_TYPE = "article";
   private static final String CONTENT_TYPE = "text/plain";
   private static final String TITLE = "Article Title";
   private static final String SLUG = "article_title";
   private static final String ABSTRACT = "This is the abstract for an article";
   private static final String BODY = "This is the body text of an article";

   private static ClosableSqlExecutor exec;
   private static ConfigurationProperties config;

   private static DbEntryRepositoryRegistry repoCtx;
   private static ArticleEntryService svc;
   private static EntryResolverRegistry resolvers;

   @BeforeClass
   public static void setUp() throws DataSourceException
   {
      // TODO spin up DB, etc
      config = TestUtils.loadConfigFile();
      exec = TestUtils.initPostgreSqlExecutor(config);

      IdFactoryProvider idProvider = TestUtils.makeIdFactoryProvider();

      repoCtx = new DbEntryRepositoryRegistry();
      repoCtx.setConfiguration(config);
      repoCtx.setIdFactory(idProvider);
      repoCtx.setSqlExecutor(exec);
      resolvers = repoCtx.getResolverRegistry();
      repoCtx.activate();

      svc = new ArticleEntryService();
      svc.setRepoContext(repoCtx);
      svc.activate();
   }

   @AfterClass
   public static void tearDown() throws Exception
   {
      svc.dispose();
      exec.close();

      if (config instanceof SimpleFileConfigurationProperties)
         ((SimpleFileConfigurationProperties)config).dispose();
   }

   @Before
   public void setupTest() throws DataSourceException
   {
   }

   @After
   public void tearDownTest() throws Exception
   {
      String sql = format("TRUNCATE {0}", TBL_NAME);
      Future<Void> future = exec.submit((conn) -> {
         try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.executeUpdate();
            return null;
         }
      });

      future.get();
   }

   private EditArticleCommand createStandardArticle(ArticleRepository repo)
   {
      EditArticleCommand cmd = repo.create();

      cmd.setArticleType(ARTICLE_TYPE);
      cmd.setContentType(CONTENT_TYPE);
      cmd.setTitle(TITLE);
      cmd.setSlug(SLUG);
      cmd.setAbstract(ABSTRACT);
      cmd.setBody(BODY);
      return cmd;
   }



// TODO these should be tested
//      cmd.setAuthors(new ArrayList<>());
//      cmd.setPublicationInfo(null);
//      cmd.setFootnotes(null);
//      cmd.setCitations(null);
//      cmd.setBibliography(null);
//      cmd.setLinks(links);

   @Test
   public void createArticle() throws Exception
   {
      ArticleRepository repo = repoCtx.getRepository(null, ArticleRepository.class);
      EditArticleCommand cmd = createStandardArticle(repo);

      Future<String> result = cmd.execute();

      String articleId = result.get();
      Article article = repo.get(articleId);

      assertNotNull(article);
      assertEquals(ARTICLE_TYPE, article.getArticleType());
      assertEquals(CONTENT_TYPE, article.getContentType());
      assertEquals(TITLE, article.getTitle());
      assertEquals(SLUG, article.getSlug());
      assertEquals(ABSTRACT, article.getAbstract());
      assertEquals(BODY, article.getBody());
   }

   @Test
   public void updateArticle() throws Exception
   {
      String articleType = "play";
      String contentType = "text/html";
      String title = "Another Article Title";
      String slug = "another_article_title";
      String articleAbstract = "This is the another abstract for the same article";
      String body = "This is the changed body text of an article";

      ArticleRepository repo = repoCtx.getRepository(null, ArticleRepository.class);
      EditArticleCommand cmd = createStandardArticle(repo);

      String articleId = cmd.execute().get();
//      Article original = repo.get(articleId);

      cmd = repo.edit(articleId);
      cmd.setArticleType(articleType);
      cmd.setContentType(contentType);
      cmd.setTitle(title);
      cmd.setSlug(slug);
      cmd.setAbstract(articleAbstract);
      cmd.setBody(body);

      articleId = cmd.execute().get();
      Article revised = repo.get(articleId);

      assertNotNull(revised);
      assertEquals(articleType, revised.getArticleType());
      assertEquals(contentType, revised.getContentType());
      assertEquals(title, revised.getTitle());
      assertEquals(slug, revised.getSlug());
      assertEquals(articleAbstract, revised.getAbstract());
      assertEquals(body, revised.getBody());
   }

   @Test
   public void deleteArticle() throws Exception
   {
      ArticleRepository repo = repoCtx.getRepository(null, ArticleRepository.class);
      EditArticleCommand cmd = createStandardArticle(repo);

      String articleId = cmd.execute().get();
      assertNotNull(repo.get(articleId));

      repo.remove(articleId).get();

      try {
         repo.get(articleId);
         assertFalse("Was able to retrieve removed entry", true);
      } catch (DocumentNotFoundException ex) {
         // this is the expected behavior
      }
   }

   @Test
   public void testArticleResolver() throws Exception
   {
      ArticleRepository repo = repoCtx.getRepository(null, ArticleRepository.class);
      EditArticleCommand cmd = createStandardArticle(repo);

      String articleId = cmd.execute().get();
      Article article = repo.get(articleId);

      EntryResolver<Article> resolver = resolvers.getResolver(article);
      assertNotNull(resolver);
      assertTrue(resolver.accepts(article));

      EntryReference articleRef = resolver.makeReference(article);
      assertNotNull(articleRef);
      assertTrue(resolver.accepts(articleRef));
      // Note - there is no obligation that the reolver id be the same as the article id,
      // so we won't test for that

      Article resolved = resolver.resolve(null, articleRef);
      assertNotNull(resolved);
      assertEquals(article.getId(), resolved.getId());
      assertEquals(article.getArticleType(), resolved.getArticleType());
      assertEquals(article.getContentType(), resolved.getContentType());
      assertEquals(article.getSlug(), resolved.getSlug());
      assertEquals(article.getTitle(), resolved.getTitle());
      assertEquals(article.getAbstract(), resolved.getAbstract());
      assertEquals(article.getBody(), resolved.getBody());
   }
}
