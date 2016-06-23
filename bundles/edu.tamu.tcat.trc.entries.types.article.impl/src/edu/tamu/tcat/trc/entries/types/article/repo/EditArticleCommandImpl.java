package edu.tamu.tcat.trc.entries.types.article.repo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.function.Consumer;

import edu.tamu.tcat.trc.entries.types.article.dto.ArticleAuthorDTO;
import edu.tamu.tcat.trc.entries.types.article.dto.ArticleDTO;
import edu.tamu.tcat.trc.entries.types.article.dto.BibliographyDTO;
import edu.tamu.tcat.trc.entries.types.article.dto.CitationDTO;
import edu.tamu.tcat.trc.entries.types.article.dto.FootnoteDTO;
import edu.tamu.tcat.trc.entries.types.article.dto.LinkDTO;
import edu.tamu.tcat.trc.entries.types.article.dto.PublicationDTO;
import edu.tamu.tcat.trc.repo.EntryUpdateExecutor;
import edu.tamu.tcat.trc.repo.UpdateContext;

public class EditArticleCommandImpl implements EditArticleCommand
{
   private static Map<String, Consumer<DataModelV1.Article>> updates = new HashMap<>();

   private final String id;
   private final EntryUpdateExecutor<DataModelV1.Article> exec;

   public EditArticleCommandImpl(String id, EntryUpdateExecutor<DataModelV1.Article> exec)
   {
      this.id = id;
      this.exec = exec;
   }

   @Override
   public UUID getId()
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public void setAll(ArticleDTO article)
   {
      throw new UnsupportedOperationException();
   }

   @Override
   public void setType(String type)
   {
      updates.put("type", article -> article.type = type);
   }

   @Override
   public void setTitle(String title)
   {
      updates.put("title", article -> article.title = title);
   }

   @Override
   public void setAbstract(String abs)
   {
      updates.put("abstract", article -> article.articleAbstract = abs);
   }


   @Override
   public void setBody(String body)
   {
      updates.put("body", article -> article.body = body);
   }


   @Override
   public void setPublicationInfo(PublicationDTO pubData)
   {
      // TODO Auto-generated method stub

   }

   @Override
   public void setAuthors(List<ArticleAuthorDTO> authors)
   {
      // TODO Auto-generated method stub

   }

   @Override
   public void setFootnotes(List<FootnoteDTO> ftNotes)
   {
      // TODO Auto-generated method stub

   }

   @Override
   public void setCitations(List<CitationDTO> citations)
   {
      // TODO Auto-generated method stub

   }

   @Override
   public void setBibliography(List<BibliographyDTO> bibliographies)
   {
      // TODO Auto-generated method stub

   }

   @Override
   public void setLinks(List<LinkDTO> links)
   {
      // TODO Auto-generated method stub

   }

   @Override
   public Future<String> execute()
   {
      return exec.submit(this::doUpdate)
                 .thenApply(article -> article.id);
   }

   private DataModelV1.Article doUpdate(UpdateContext<DataModelV1.Article> context)
   {
      DataModelV1.Article modified = context.getModified();
      updates.values().forEach(updater -> updater.accept(modified));

      return modified;
   }
}
