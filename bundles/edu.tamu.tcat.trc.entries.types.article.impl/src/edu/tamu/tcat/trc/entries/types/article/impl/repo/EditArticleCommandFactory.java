package edu.tamu.tcat.trc.entries.types.article.impl.repo;

import static java.text.MessageFormat.format;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

import edu.tamu.tcat.trc.entries.types.article.impl.repo.DataModelV1.Article;
import edu.tamu.tcat.trc.entries.types.article.impl.repo.DataModelV1.ArticleAuthor;
import edu.tamu.tcat.trc.entries.types.article.impl.repo.DataModelV1.Footnote;
import edu.tamu.tcat.trc.entries.types.article.repo.AuthorMutator;
import edu.tamu.tcat.trc.entries.types.article.repo.EditArticleCommand;
import edu.tamu.tcat.trc.entries.types.article.repo.FootnoteMutator;
import edu.tamu.tcat.trc.repo.BasicChangeSet;
import edu.tamu.tcat.trc.repo.ChangeSet;
import edu.tamu.tcat.trc.repo.ChangeSet.ApplicableChangeSet;
import edu.tamu.tcat.trc.repo.EditCommandFactory;
import edu.tamu.tcat.trc.repo.ExecutableUpdateContext;

public class EditArticleCommandFactory implements EditCommandFactory<DataModelV1.Article, EditArticleCommand>
{
   @Override
   public EditArticleCommand create(ExecutableUpdateContext<DataModelV1.Article> ctx)
   {
      return new EditArticleCommandImpl(ctx);
   }

   @Override
   public Article initialize(String id, Optional<DataModelV1.Article> original)
   {
      return original.map(DataModelV1.Article::copy)
               .orElseGet(() -> {
                  DataModelV1.Article dto = new DataModelV1.Article();
                  dto.id = id;
                  return dto;
               });
   }

   public class EditArticleCommandImpl implements EditArticleCommand
   {
      private final String id;
      private final ExecutableUpdateContext<DataModelV1.Article> ctx;
      private final ApplicableChangeSet<DataModelV1.Article> changes = new BasicChangeSet<>();

      public EditArticleCommandImpl(ExecutableUpdateContext<DataModelV1.Article> ctx)
      {
         this.id = ctx.getId();
         this.ctx = ctx;
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

         changes.add(format("author.{0} [create]", authorId), article -> {
            // TODO ensure uniqueness
            DataModelV1.ArticleAuthor author = new DataModelV1.ArticleAuthor();
            author.id = authorId;
            article.authors.add(author);
         });

         ChangeSet<DataModelV1.ArticleAuthor> partial = changes.partial(format("author.{0}", authorId), makeAuthorSelector(authorId));
         return new ArticleAuthorMutatorImpl(authorId, partial);
      }

      @Override
      public AuthorMutator editAuthor(String authorId)
      {
         Objects.requireNonNull(authorId, "Author id must not be null");

         ChangeSet<DataModelV1.ArticleAuthor> partial = changes.partial(format("author.{0}", authorId), makeAuthorSelector(authorId));
         return new ArticleAuthorMutatorImpl(authorId, partial);
      }

      @Override
      public void moveAuthor(String authorId, String beforeId)
      {
         Objects.requireNonNull(authorId, "Author id must not be null");

         changes.add(format("author.{0} [move]", authorId), article -> {
            List<String> ids = article.authors.stream()
                  .map(author -> author.id)
                  .collect(Collectors.toList());

            int authorIx = ids.indexOf(authorId);
            if (authorIx < 0)
               throw new IllegalStateException(format("Cannot move author {0}. No author with this id was found.", Integer.valueOf(authorIx)));

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
      public void clearAuthors()
      {
         changes.add("authors [clear]", article -> article.authors.clear());
      }

      @Override
      public void clearFootnotes()
      {
         changes.add("footnotes [clear]", article -> {
            article.footnotes.clear();
         });
      }

      private DataModelV1.Footnote makeFootnote(String id)
      {
         DataModelV1.Footnote footnote = new DataModelV1.Footnote();
         footnote.id = id;
         return footnote;
      }

      @Override
      public FootnoteMutator editFootnote(String footnoteId)
      {
         ChangeSet<Footnote> partial = changes.partial(
               format("footnotes.{0} [edit]", footnoteId),
               article -> article.footnotes.computeIfAbsent(footnoteId, this::makeFootnote));

         return new FootnoteMutatorImpl(footnoteId, partial);
      }

      @Override
      public CompletableFuture<String> execute()
      {
         return ctx.update(changes::apply).thenApply(dto -> dto.id);
      }
   }

   private static class ArticleAuthorMutatorImpl implements AuthorMutator
   {

      private final String id;
      private final ChangeSet<ArticleAuthor> changes;

      public ArticleAuthorMutatorImpl(String id, ChangeSet<ArticleAuthor> partial)
      {
         this.id = id;
         this.changes = partial;
      }

      @Override
      public String getId()
      {
         return id;
      }

      @Override
      public void setDisplayName(String name)
      {
         changes.add("displayName", author -> author.name = name);
      }

      @Override
      public void setFirstname(String name)
      {
         changes.add("firstname", author -> author.first = name);
      }

      @Override
      public void setLastname(String name)
      {
         changes.add("lastname", author -> author.last = name);
      }

      @Override
      public void setProperty(String key, String value)
      {
         changes.add(format("property [{0}]", key),
               dto -> dto.properties.put(key, value));
      }

      @Override
      public void clearProperty(String key)
      {
         changes.add(format("property [CLEAR {0}]", key),
               dto -> dto.properties.remove(key));
      }

      @Override
      public void clearProperties()
      {
         changes.add("property [CLEAR]", dto -> dto.properties.clear());
      }
   }

   private static class FootnoteMutatorImpl implements FootnoteMutator
   {
      private final String id;
      private final ChangeSet<Footnote> changes;

      public FootnoteMutatorImpl(String id, ChangeSet<DataModelV1.Footnote> changes)
      {
         this.id = id;
         this.changes = changes;
      }

      @Override
      public String getId()
      {
         return id;
      }

      @Override
      public void setBacklinkId(String backlinkId)
      {
         changes.add("backlinkId", footnote -> footnote.backlinkId = backlinkId);
      }

      @Override
      public void setContent(String content)
      {
         changes.add("content", footnote -> footnote.content = content);
      }

      @Override
      public void setMimeType(String mimeType)
      {
         changes.add("mimeType", footnote -> footnote.mimeType = mimeType);
      }
   }
}