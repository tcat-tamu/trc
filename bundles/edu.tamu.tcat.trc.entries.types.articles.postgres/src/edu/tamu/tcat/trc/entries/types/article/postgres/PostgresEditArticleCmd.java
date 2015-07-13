package edu.tamu.tcat.trc.entries.types.article.postgres;

import java.net.URI;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import edu.tamu.tcat.trc.entries.types.article.dto.ArticleDTO;
import edu.tamu.tcat.trc.entries.types.article.repo.EditArticleCommand;

public class PostgresEditArticleCmd implements EditArticleCommand
{

   private final ArticleDTO article;
   private final AtomicBoolean executed = new AtomicBoolean(false);

   private Function<ArticleDTO, Future<UUID>> commitHook;

   public PostgresEditArticleCmd(ArticleDTO note)
   {
      this.article = note;
   }

   public void setCommitHook(Function<ArticleDTO, Future<UUID>> hook)
   {
      commitHook = hook;
   }

   @Override
   public UUID getId()
   {
      return article.id;
   }

   @Override
   public void setAll(ArticleDTO updateArticle)
   {
      if (updateArticle.id != null && !updateArticle.id.equals(article.id))
         throw new IllegalArgumentException("The supplied article ");

      article.title = updateArticle.title;
      article.authorId = updateArticle.authorId;
      article.associatedEntity = updateArticle.associatedEntity;
      article.mimeType = updateArticle.mimeType;
      article.content = updateArticle.content;
   }

   @Override
   public void setTitle(String title)
   {
      article.title = title;
   }

   @Override
   public void setEntity(URI entityURI)
   {
      article.associatedEntity = entityURI;
   }

   @Override
   public void setAuthorId(String authorId)
   {
      article.authorId = authorId;
   }

   @Override
   public void setMimeType(String mimeType)
   {
      article.mimeType = mimeType;
   }

   @Override
   public void setContent(String content)
   {
      article.content = content;
   }

   @Override
   public Future<UUID> execute()
   {
      Objects.requireNonNull(commitHook, "No commit hook supplied.");
      if (!executed.compareAndSet(false, true))
         throw new IllegalStateException("This edit copy command has already been invoked.");

      return commitHook.apply(article);
   }

}
