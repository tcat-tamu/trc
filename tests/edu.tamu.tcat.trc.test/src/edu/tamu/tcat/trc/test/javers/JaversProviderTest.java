package edu.tamu.tcat.trc.test.javers;

import static java.text.MessageFormat.format;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Future;

import org.javers.common.collections.Optional;
import org.javers.core.Javers;
import org.javers.core.diff.Diff;
import org.javers.core.metamodel.object.CdoSnapshot;
import org.javers.repository.jql.QueryBuilder;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.tamu.tcat.db.core.DataSourceException;
import edu.tamu.tcat.db.exec.sql.SqlExecutor;
import edu.tamu.tcat.trc.TrcApplication;
import edu.tamu.tcat.trc.entries.types.article.impl.ArticleEntryService;
import edu.tamu.tcat.trc.entries.types.article.impl.repo.DataModelV1;
import edu.tamu.tcat.trc.entries.types.article.impl.repo.DataModelV1.Article;
import edu.tamu.tcat.trc.entries.types.article.impl.repo.DataModelV1.ArticleAuthor;
import edu.tamu.tcat.trc.entries.types.article.impl.repo.DataModelV1.Footnote;
import edu.tamu.tcat.trc.repo.postgres.JaversProvider;
import edu.tamu.tcat.trc.test.support.TrcTestContext;

public class JaversProviderTest
{
   private static final String ARTICLE_TYPE = "article";
   private static final String CONTENT_TYPE = "text/plain";
   private static final String TITLE = "Article Title";
   private static final String SLUG = "article_title";
   private static final String ABSTRACT = "This is the abstract for an article";
   private static final String BODY = "This is the body text of an article";
   
   private static final String JV_COMMIT_PROPERTY = "jv_commit_property";
   private static final String JV_SNAPSHOT = "jv_snapshot";
   private static final String JV_COMMIT = "jv_commit";
   private static final String JV_GLOBAL_ID = "jv_global_id";

   private static TrcTestContext trcTestContext;
   private static JaversProvider jvsp;

   @BeforeClass
   public static void beforeClass() throws DataSourceException
   {
      trcTestContext = new TrcTestContext();
      jvsp = trcTestContext.getJaversProvider();
   }

   @AfterClass
   public static void afterClass() throws Exception
   {
      trcTestContext.close();
      jvsp.dispose(); //Not that it does anything... :-)
   }

   @Before
   public void setup() throws DataSourceException
   {
   }

   @After
   public void tearDown() throws Exception
   {
      deleteContents(JV_COMMIT_PROPERTY);
      deleteContents(JV_SNAPSHOT);
      deleteContents(JV_COMMIT);
      deleteContents(JV_GLOBAL_ID);
   }
   
   public void deleteContents(String tableName) throws Exception
   {
      String sql = format("TRUNCATE {0} CASCADE", tableName);
      SqlExecutor exec = trcTestContext.getSqlExecutor();
      Future<Void> future = exec.submit((conn) -> {
         try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.executeUpdate();
            return null;
         }
      });

      future.get();
   }

   private DataModelV1.Article createStandardArticle()
   {
      DataModelV1.Article article = new DataModelV1.Article();

      article.id = UUID.randomUUID().toString();
      article.title = TITLE;
      article.body = BODY;
      article.articleType = ARTICLE_TYPE;
      article.contentType = CONTENT_TYPE;
      article.slug = SLUG;
      article.articleAbstract = ABSTRACT;
     
      return article;
   }
   
   @Test
   public void javersCommit()
   {
      Article articleDTO = createStandardArticle();
      
      org.javers.core.Javers jvs = jvsp.getJavers();
      
      jvs.commit("Johnny", articleDTO);
      
      jvs.commit("Johnny", changeTitle(articleDTO));
      jvs.commit("Johnny", changeAbstract(articleDTO));
      jvs.commit("Johnny", changeBody(articleDTO));
      
      
      articleDTO.authors.add(addAuthor("James Cordon", "James", "Cordon"));
      jvs.commit("Johnny", articleDTO);
      
      articleDTO.authors.add(addAuthor("Mary Steelhammer", "Mary", "Steelhammer"));
      jvs.commit("Johnny", articleDTO);
      
      
      Footnote note = new Footnote();
      note.id = UUID.randomUUID().toString();
      note.backlinkId = "/Vehicles/Electric/Tesla";
      note.content = "The information on this is not correct and out of date.";
      
      articleDTO.footnotes.put(note.id, note);
      jvs.commit("Johnny", articleDTO);
      
      List<CdoSnapshot> snapShots = jvs.findSnapshots(QueryBuilder.byInstanceId(articleDTO.id, Article.class).build());
      
      Assert.assertTrue(snapShots.size() == 7);
      checkDiff(articleDTO.authors);
      
   }
   
   private void checkDiff(List<ArticleAuthor> authors)
   {
      Javers javers = jvsp.getJavers();
      
      if (authors.size() > 1)
      {
         Diff diff = javers.compare(authors.get(0), authors.get(1));
         String changesSummary = diff.changesSummary();
         
         System.out.println(diff);
         System.out.println(changesSummary);
      }
   }
   
   private ArticleAuthor addAuthor(String fullName, String first, String last)
   {
      ArticleAuthor author = new ArticleAuthor();
      author.id = UUID.randomUUID().toString();
      author.name = fullName;
      author.first = first;
      author.last = last;
      
      return author;
   }

   private Article changeTitle(Article article)
   {
      article.title = "Tesla Model 3 Review.";
      return article;
   }
   
   private Article changeAbstract(Article article)
   {
      article.articleAbstract = "Tesla Model 3 is a revolutionary car. Production of the car is going now with car being released to customers at the end of 2017.";
      return article;
   }
   
   private Article changeBody(Article article)
   {
      article.body = "In early 2016 Tesla announce a new electric car that would be around $35K. When this announcement was made, may Tesla enthusiasts where exited about being able to own a vehicle that was able to be within thier budget.";
      return article;
   }

}
