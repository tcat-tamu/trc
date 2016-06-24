package edu.tamu.tcat.trc.entries.types.article.repo;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import edu.tamu.tcat.trc.entries.types.article.dto.ArticleAuthorDTO;
import edu.tamu.tcat.trc.entries.types.article.dto.ArticleDTO;
import edu.tamu.tcat.trc.entries.types.article.dto.BibliographyDTO;
import edu.tamu.tcat.trc.entries.types.article.dto.CitationDTO;
import edu.tamu.tcat.trc.entries.types.article.dto.FootnoteDTO;
import edu.tamu.tcat.trc.entries.types.article.dto.LinkDTO;
import edu.tamu.tcat.trc.entries.types.article.dto.PublicationDTO;
import edu.tamu.tcat.trc.entries.types.article.repo.DataModelV1.Article;
import edu.tamu.tcat.trc.entries.types.article.search.solr.ArticleIndexManagerService;
import edu.tamu.tcat.trc.repo.BasicChangeSet;
import edu.tamu.tcat.trc.repo.ChangeSet.ApplicableChangeSet;
import edu.tamu.tcat.trc.repo.EditCommandFactory;
import edu.tamu.tcat.trc.repo.UpdateContext;

public class EditArticleCommandFactory implements EditCommandFactory<DataModelV1.Article, EditArticleCommand>
{

   public EditArticleCommandFactory(ArticleIndexManagerService indexService)
   {
      // TODO Auto-generated constructor stub
   }

   @Override
   public EditArticleCommand create(String id, UpdateStrategy<Article> strategy)
   {
      return new EditArticleCommandImpl(id, strategy);
   }

   @Override
   public EditArticleCommand edit(String id, UpdateStrategy<Article> strategy)
   {
      return new EditArticleCommandImpl(id, strategy);
   }

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
      public void setAll(ArticleDTO article)
      {
         throw new UnsupportedOperationException();
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
      public void setAbstract(String abs)
      {
         changes.add("abstract", article -> article.articleAbstract = abs);
      }


      @Override
      public void setBody(String body)
      {
         changes.add("body", article -> article.body = body);
      }


      @Override
      public void setPublicationInfo(PublicationDTO pubData)
      {
         throw new UnsupportedOperationException();
      }

      @Override
      public void setAuthors(List<ArticleAuthorDTO> authors)
      {
         throw new UnsupportedOperationException();
      }

      @Override
      public void setFootnotes(List<FootnoteDTO> ftNotes)
      {
         throw new UnsupportedOperationException();
      }

      @Override
      public void setCitations(List<CitationDTO> citations)
      {
         throw new UnsupportedOperationException();
      }

      @Override
      public void setBibliography(List<BibliographyDTO> bibliographies)
      {
         throw new UnsupportedOperationException();
      }

      @Override
      public void setLinks(List<LinkDTO> links)
      {
         throw new UnsupportedOperationException();
      }

      @Override
      public Future<String> execute()
      {
         CompletableFuture<DataModelV1.Article> modified = exec.update(ctx -> {
            DataModelV1.Article dto = prepModifiedData(ctx);
            return this.changes.apply(dto);
         });

         return modified
               .thenApply(this::index)             // TODO move to plugable task listening on repo
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

      private DataModelV1.Article index(DataModelV1.Article dto)
      {
         // TODO index should hook in as listener to the repo rather than being invoked explicitly
         return dto;
      }
   }

}
