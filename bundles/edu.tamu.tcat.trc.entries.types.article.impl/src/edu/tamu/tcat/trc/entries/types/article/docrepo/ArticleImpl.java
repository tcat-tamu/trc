package edu.tamu.tcat.trc.entries.types.article.docrepo;

import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import edu.tamu.tcat.trc.entries.types.article.Article;
import edu.tamu.tcat.trc.entries.types.article.ArticleAuthor;
import edu.tamu.tcat.trc.entries.types.article.ArticleLink;
import edu.tamu.tcat.trc.entries.types.article.Bibliography;
import edu.tamu.tcat.trc.entries.types.article.Citation;
import edu.tamu.tcat.trc.entries.types.article.Footnote;

public class ArticleImpl implements Article
{
   private final static Logger logger = Logger.getLogger(ArticleImpl.class.getName());

   private final String id;
   private final String articleType;
   private final String contentType;
   private final String title;
   private final String slug;
   private final String articleAbstract;
   private final String body;

   public ArticleImpl(DataModelV1.Article dto)
   {
      this.id = dto.id;
      this.articleType = dto.articleType;
      this.contentType = dto.contentType;
      this.title = dto.title;
      this.articleAbstract = dto.articleAbstract;
      this.body = dto.body;
      this.slug = dto.slug;
   }

   @Override
   public String getId()
   {
      return id;
   }

   @Override
   public String getContentType()
   {
      return contentType;
   }

   @Override
   public String getArticleType()
   {
      return articleType;
   }

   @Override
   public String getTitle()
   {
      return title;
   }

   @Override
   public String getSlug()
   {
      return slug;
   }

//   @Override
//   public ArticlePublication getPublicationInfo()
//   {
//      logger.warning("Publication info is not currently supported");
//      return null;
//   }

   @Override
   public List<ArticleAuthor> getAuthors()
   {
      logger.warning("Authors are not currently supported");
      return Collections.emptyList();
   }

   @Override
   public String getAbstract()
   {
      return articleAbstract;
   }

   @Override
   public String getBody()
   {
      return body;
   }

   @Override
   public List<Footnote> getFootnotes()
   {
      // TODO Auto-generated method stub
      logger.warning("Footnotes are not currently supported");
      return Collections.emptyList();
   }

   @Override
   public List<Citation> getCitations()
   {
      // TODO Auto-generated method stub
      logger.warning("Citations are not currently supported");
      return Collections.emptyList();
   }

   @Override
   public List<Bibliography> getBibliographies()
   {
      // TODO Auto-generated method stub
      logger.warning("Bibliographies are not currently supported");
      return Collections.emptyList();
   }

   @Override
   public List<ArticleLink> getLinks()
   {
      // TODO Auto-generated method stub
      logger.warning("Links are not currently supported");
      return Collections.emptyList();
   }
}
