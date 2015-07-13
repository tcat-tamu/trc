package edu.tamu.tcat.trc.entries.types.article.postgres;

import java.io.IOException;
import java.net.URI;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.postgresql.util.PGobject;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.tamu.tcat.db.exec.sql.SqlExecutor;
import edu.tamu.tcat.trc.entries.notification.BaseUpdateEvent;
import edu.tamu.tcat.trc.entries.notification.DataUpdateObserverAdapter;
import edu.tamu.tcat.trc.entries.notification.EntryUpdateHelper;
import edu.tamu.tcat.trc.entries.notification.ObservableTaskWrapper;
import edu.tamu.tcat.trc.entries.notification.UpdateEvent;
import edu.tamu.tcat.trc.entries.notification.UpdateListener;
import edu.tamu.tcat.trc.entries.repo.CatalogRepoException;
import edu.tamu.tcat.trc.entries.repo.NoSuchCatalogRecordException;
import edu.tamu.tcat.trc.entries.types.article.Article;
import edu.tamu.tcat.trc.entries.types.article.dto.ArticleDTO;
import edu.tamu.tcat.trc.entries.types.article.repo.ArticleChangeEvent;
import edu.tamu.tcat.trc.entries.types.article.repo.ArticleRepository;
import edu.tamu.tcat.trc.entries.types.article.repo.EditArticleCommand;

public class PsqlArticleRepo implements ArticleRepository
{
   private static final Logger logger = Logger.getLogger(PsqlArticleRepo.class.getName());

   private static final String SQL_GET_ALL =
         "SELECT article "
        +  "FROM articles "
        + "WHERE reference->>'associatedEntry' LIKE ? AND active = true "
        + "ORDER BY reference->>'associatedEntry'";

   private static final String SQL_GET =
         "SELECT article "
               +  "FROM articles "
               + "WHERE article_id = ? AND active = true";

   private static final String SQL_GET_ALL_BY_ID =
         "SELECT article "
        +  "FROM articles "
        + "WHERE article_id = ?";

   private static String CREATE_SQL =
         "INSERT INTO articles (article, article_id) VALUES(?, ?)";

   private static String UPDATE_SQL =
         "UPDATE articles "
         + " SET article = ?, "
         +     " modified = now() "
         +"WHERE article_id = ?";

   private static final String SQL_REMOVE =
         "UPDATE articles "
         + " SET active = FALSE, "
         +     " modified = now() "
         +"WHERE article_id = ?";

   //HACK: doesn't really matter for now, but once authz is in place, this will be the user's id
   private static final UUID ACCOUNT_ID_REPO = UUID.randomUUID();

   private EntryUpdateHelper<ArticleChangeEvent> listeners;
   private SqlExecutor exec;
   private ObjectMapper mapper;

   public PsqlArticleRepo()
   {
   }

   public void setDatabaseExecutor(SqlExecutor exec)
   {
      this.exec = exec;
   }

   public void activate()
   {
      listeners = new EntryUpdateHelper<>();

      mapper = new ObjectMapper();
      mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
   }

   public void dispose()
   {
      try
      {
         if (listeners != null)
            listeners.close();
      }
      catch (Exception ex)
      {
         logger.log(Level.WARNING, "Failed to shut down event notification helper.", ex);
      }

      // managed by supplier. no need to shut down
      this.exec = null;
      listeners = null;
      mapper = null;
   }

   @Override
   public Article get(UUID articleId) throws NoSuchCatalogRecordException
   {
      return adapt(getArticleDTO(SQL_GET, articleId));
   }

   @Override
   public List<Article> getArticles(URI entityURI) throws NoSuchCatalogRecordException
   {
      Future<List<Article>> results = exec.submit(conn -> {
         try (PreparedStatement ps = conn.prepareStatement(SQL_GET_ALL))
         {
            ps.setString(1, entityURI.toString() + "%");
            try (ResultSet rs = ps.executeQuery())
            {
               List<Article> articles = new ArrayList<>();
               while (rs.next())
               {
                  PGobject pgo = (PGobject)rs.getObject("note");
                  ArticleDTO article = parseCopyRefJson(pgo.toString());
                  Article n = adapt(article);
                  articles.add(n);
               }

               return articles;
            }
         }
         catch(SQLException e)
         {
            throw new IllegalStateException("Failed to retrive copy reference [" + entityURI + "]. ", e);
         }
      });

      try
      {
         return unwrapGetResults(results, entityURI.toString());
      }
      catch (NoSuchCatalogRecordException e)
      {
         throw new IllegalStateException("Unexpected internal error", e);
      }
   }

   private static Article adapt(ArticleDTO article)
   {
      return new PsqlArticle(article.id, article.title, article.associatedEntity, article.authorId, article.mimeType, article.content);
   }

   @Override
   public EditArticleCommand create()
   {
      ArticleDTO article = new ArticleDTO();
      article.id = UUID.randomUUID();

      PostgresEditArticleCmd cmd = new PostgresEditArticleCmd(article);
      cmd.setCommitHook((n) -> {
         ArticleChangeNotifier notifier = new ArticleChangeNotifier(UpdateEvent.UpdateAction.CREATE);
         return exec.submit(new ObservableTaskWrapper<UUID>(makeSaveTask(n, CREATE_SQL), notifier));
      });

      return cmd;
   }

   @Override
   public EditArticleCommand edit(UUID articleId) throws NoSuchCatalogRecordException
   {
      ArticleDTO article = getArticleDTO(SQL_GET, articleId);

      PostgresEditArticleCmd cmd = new PostgresEditArticleCmd(article);
      cmd.setCommitHook((n) -> {
         ArticleChangeNotifier notifier = new ArticleChangeNotifier(UpdateEvent.UpdateAction.UPDATE);
         return exec.submit(new ObservableTaskWrapper<UUID>(makeSaveTask(n, UPDATE_SQL), notifier));
      });

      return cmd;
   }

   @Override
   public Future<Boolean> remove(UUID articleId)
   {
      ArticleChangeEvent evt = new ArticleChangeEventImpl(articleId, UpdateEvent.UpdateAction.UPDATE);
      return exec.submit(new ObservableTaskWrapper<Boolean>(
            makeRemoveTask(articleId),
            new DataUpdateObserverAdapter<Boolean>()
            {
               @Override
               protected void onFinish(Boolean result) {
                  if (result.booleanValue())
                     listeners.after(evt);
               }
            }));
   }

   @Override
   public AutoCloseable register(UpdateListener<ArticleChangeEvent> ears)
   {
      Objects.requireNonNull(listeners, "Registration for updates is not available.");
      return listeners.register(ears);
   }

   private SqlExecutor.ExecutorTask<Boolean> makeRemoveTask(UUID id)
   {
      return (conn) -> {
         try (PreparedStatement ps = conn.prepareStatement(SQL_REMOVE))
         {
            ps.setString(1, id.toString());
            int ct = ps.executeUpdate();
            if (ct == 0)
            {
               logger.log(Level.WARNING, "Failed to remove article  [" + id + "]. Reference may not exist.", id);
               return Boolean.valueOf(false);
            }

            return Boolean.valueOf(true);
         }
         catch(SQLException e)
         {
            throw new IllegalStateException("Failed to remove article [" + id + "]. ", e);
         }
      };
   }

   private SqlExecutor.ExecutorTask<UUID> makeSaveTask(ArticleDTO dto, String sql)
   {
      ObjectMapper mapper = new ObjectMapper();
      mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

      return (conn) -> {
         try (PreparedStatement ps = conn.prepareStatement(sql))
         {
            PGobject jsonObject = new PGobject();
            jsonObject.setType("json");
            jsonObject.setValue(mapper.writeValueAsString(dto));

            ps.setObject(1, jsonObject);
            ps.setString(2, dto.id.toString());

            int cnt = ps.executeUpdate();
            if (cnt != 1)
               throw new IllegalStateException("Failed to update copy reference [" + dto.id +"]");

            return dto.id;
         }
         catch(SQLException e)
         {
            throw new IllegalStateException("Failed to update note reference [" + dto.id + "]. "
                  + "\n\tEntry [" + dto.associatedEntity + "]"
                  + "\n\tCopy  [" + dto.id + "]", e);
         }
      };
   }

   private ArticleDTO parseCopyRefJson(String json)
   {
      try
      {
         return mapper.readValue(json, ArticleDTO.class);
      }
      catch (IOException e)
      {
         throw new IllegalStateException("Failed to parse relationship record\n" + json, e);
      }
   }

   private ArticleDTO getArticleDTO(String sql, UUID id) throws NoSuchCatalogRecordException
   {
      Future<ArticleDTO> result = exec.submit((conn) -> executeGetQuery(sql, conn, id));
      return unwrapGetResults(result, id.toString());
   }

   /**
    *
    * @param result The future to unwrap
    * @param id For error messaging purposes
    * @return
    * @throws NoSuchCatalogRecordException
    */
   private <T> T unwrapGetResults(Future<T> result, String id) throws NoSuchCatalogRecordException
   {
      try
      {
         return result.get();
      }
      catch (InterruptedException e)
      {
         throw new IllegalStateException("Failed to retrieve copy reference [" + id + "].", e);
      }
      catch (ExecutionException e)
      {
         // unwrap the execution exception that may be thrown from the executor
         Throwable cause = e.getCause();
         if (cause instanceof NoSuchCatalogRecordException)
            throw (NoSuchCatalogRecordException)cause;         // if not found
         else if (cause instanceof RuntimeException)
            throw (RuntimeException)cause;                     // 'expected' internal errors - json parsing, db access, etc
         else if (cause instanceof Error)
            throw (Error)cause;                                // OoM and other system errors
         else                                                  // unanticipated errors
            throw new IllegalStateException("Unknown error while attempting to retrive copy reference [" + id + "]", cause);
      }
   }

   private ArticleDTO executeGetQuery(String sql, Connection conn, UUID id) throws NoSuchCatalogRecordException
   {
      try (PreparedStatement ps = conn.prepareStatement(sql))
      {
         ps.setString(1, id.toString());
         try (ResultSet rs = ps.executeQuery())
         {
            if (!rs.next())
               throw new NoSuchCatalogRecordException("No catalog record exists for article id=" + id);

            PGobject pgo = (PGobject)rs.getObject("article");
            return parseCopyRefJson(pgo.toString());
         }
      }
      catch(SQLException e)
      {
         throw new IllegalStateException("Failed to retrive copy reference [" + id + "]. ", e);
      }
   }

   private final class ArticleChangeNotifier extends DataUpdateObserverAdapter<UUID>
   {
      private final UpdateEvent.UpdateAction type;

      public ArticleChangeNotifier(UpdateEvent.UpdateAction type)
      {
         this.type = type;
      }

      @Override
      public void onFinish(UUID id)
      {
         listeners.after(new ArticleChangeEventImpl(id, type));
      }
   }

   private class ArticleChangeEventImpl extends BaseUpdateEvent implements ArticleChangeEvent
   {
      private AtomicReference<Article> article = new AtomicReference<Article>();
      private final UUID articleId;

      public ArticleChangeEventImpl(UUID id, UpdateEvent.UpdateAction type)
      {
         super(id.toString(), type, ACCOUNT_ID_REPO, Instant.now());
         this.articleId = id;
      }

      @Override
      public synchronized Article getArticle() throws CatalogRepoException
      {
         // TODO better to use a future
         Article a = article.get();
         if (a != null)
            return a;

         try
         {
            a = get(articleId);
            article.set(a);
            return a;
         }
         catch (Exception e)
         {
            throw new CatalogRepoException("Failed to retrieve article: [" + id + "].", e);
         }
      }

      @Override
      public String toString()
      {
         return "Article Change " + super.toString();
      }
   }

   private static class PsqlArticle implements Article
   {
      private final UUID id;
      private final String title;
      private final URI associatedEntity;
      private final String authorId;
      private final String mimeType;
      private final String content;

      public PsqlArticle(UUID id, String title, URI associatedEntity, String authorId, String mimeType, String content)
      {
         this.id = id;
         this.title = title;
         this.associatedEntity = associatedEntity;
         this.authorId = authorId;
         this.mimeType = mimeType;
         this.content = content;
      }

      @Override
      public UUID getId()
      {
         return id;
      }

      @Override
      public String getTitle()
      {
         return title;
      }

      @Override
      public URI getEntity()
      {
         return associatedEntity;
      }

      @Override
      public UUID getAuthorId()
      {
         return UUID.fromString(authorId);
      }

      @Override
      public String getMimeType()
      {
         return mimeType;
      }

      @Override
      public String getContent()
      {
         return content;
      }
   }
}
