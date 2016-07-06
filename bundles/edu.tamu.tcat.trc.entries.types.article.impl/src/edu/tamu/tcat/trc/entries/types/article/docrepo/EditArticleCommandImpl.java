package edu.tamu.tcat.trc.entries.types.article.docrepo;

import static java.text.MessageFormat.format;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

import edu.tamu.tcat.trc.entries.types.article.docrepo.DataModelV1.Article;
import edu.tamu.tcat.trc.entries.types.article.docrepo.DataModelV1.ArticleAuthor;
import edu.tamu.tcat.trc.entries.types.article.repo.AuthorMutator;
import edu.tamu.tcat.trc.entries.types.article.repo.EditArticleCommand;
import edu.tamu.tcat.trc.repo.BasicChangeSet;
import edu.tamu.tcat.trc.repo.ChangeSet;
import edu.tamu.tcat.trc.repo.ChangeSet.ApplicableChangeSet;
import edu.tamu.tcat.trc.repo.EditCommandFactory.UpdateStrategy;
import edu.tamu.tcat.trc.repo.UpdateContext;

public class EditArticleCommandImpl implements EditArticleCommand
{
   private final String id;
   private final UpdateStrategy<Article> exec;
   private final ApplicableChangeSet<DataModelV1.Article> changes = new BasicChangeSet<>();

   public EditArticleCommandImpl(String id, UpdateStrategy<DataModelV1.Article> exec)
   {
      this.id = id;
      this.exec = exec;
   }

   @Override
   public String getId()
   {
      return id;
   }

   @Override
   public void setContentType(String type)
   {
      changes.add("contentType", article -> article.contentType = type);
   }

   @Override
   public void setArticleType(String type)
   {
      changes.add("articleType", article -> article.articleType = type);
   }

   @Override
   public void setTitle(String title)
   {
      changes.add("title", article -> article.title = title);
   }

   @Override
   public void setSlug(String slug)
   {
      // TODO verify that the slug is unique at creation time.
      //      Provide REPO API to allow the repo to determine if an article exists with this slug
      //      lookup articles by slug.
      changes.add("slug", article -> article.slug = slug);
   }

   @Override
   public void setAbstract(String abs)
   {
      changes.add("abstract", article -> article.articleAbstract = abs);
   }

   @Override
   public void setBody(String body)
   {
      changes.add("body", article -> article.body = body);
   }

   private Function<DataModelV1.Article, DataModelV1.ArticleAuthor> makeAuthorSelector(String id)
   {
      return (article) -> {
         // FIXME this will fail if the same id is used more than once within an artilce
         return article.authors.stream()
               .filter(author -> id.equals(author.id))
               .findAny()
               .orElseThrow(() -> new IllegalStateException(format("Cannot find author for id {0}", id)));
      };
   }

   @Override
   public AuthorMutator addAuthor(String authorId)
   {
      Objects.requireNonNull(authorId, "Author id must not be null");

      changes.add(format("author.{id} [create]", authorId), article -> {
         // TODO ensure uniqueness
         ArticleAuthor author = new DataModelV1.ArticleAuthor();
         author.id = authorId;
         article.authors.add(author);
      });

      ChangeSet<ArticleAuthor> partial = changes.partial(format("author.{id}", authorId), makeAuthorSelector(authorId));
      return new ArticleAuthorMutatorImpl(authorId, partial);
   }

   @Override
   public AuthorMutator editAuthor(String authorId)
   {
      Objects.requireNonNull(authorId, "Author id must not be null");

      ChangeSet<ArticleAuthor> partial = changes.partial(format("author.{id}", authorId), makeAuthorSelector(authorId));
      return new ArticleAuthorMutatorImpl(authorId, partial);
   }

   @Override
   public void moveAuthor(String authorId, String beforeId)
   {
      Objects.requireNonNull(authorId, "Author id must not be null");

      changes.add(format("author.{id} [move]", authorId), article -> {
         List<String> ids = article.authors.stream()
               .map(author -> author.id)
               .collect(Collectors.toList());

         int authorIx = ids.indexOf(authorId);
         if (authorIx < 0)
            throw new IllegalStateException(format("Cannot move author {0}. No author with this id was found.", authorIx));

         ids.remove(authorIx);
         DataModelV1.ArticleAuthor author = article.authors.remove(authorIx);

         int targetIx = (beforeId == null) ? -1 : ids.indexOf(beforeId);
         if (targetIx < 0)
            article.authors.add(author);
         else
            article.authors.add(targetIx, author);
      });
   }

   @Override
   public void removeAuthor(String authorId)
   {
      Objects.requireNonNull(authorId, "Author id must not be null");
      changes.add(format("author.{id} [remove]", authorId), article -> {
         article.authors.removeIf(auth -> authorId.equals(auth.id));
      });
   }

   @Override
   public CompletableFuture<String> execute()
   {
      CompletableFuture<DataModelV1.Article> modified = exec.update(ctx -> {
         DataModelV1.Article dto = prepModifiedData(ctx);
         return this.changes.apply(dto);
      });

      return modified
            .thenApply(dto -> dto.id);
   }

   private DataModelV1.Article prepModifiedData(UpdateContext<DataModelV1.Article> ctx)
   {
      DataModelV1.Article dto = null;
      DataModelV1.Article original = ctx.getOriginal();
      if (original == null)
      {
         dto = new DataModelV1.Article();
         dto.id = this.id;
      }
      else
      {
         dto = DataModelV1.Article.copy(original);
      }
      return dto;
   }
}