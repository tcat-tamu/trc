package edu.tamu.tcat.trc.entries.types.article.impl.model;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import edu.tamu.tcat.trc.entries.types.article.Article;
import edu.tamu.tcat.trc.entries.types.article.ArticleAuthor;
import edu.tamu.tcat.trc.entries.types.article.Footnote;
import edu.tamu.tcat.trc.entries.types.article.impl.repo.DataModelV1;

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
   private final Map<String, Footnote> footnotes;

   public ArticleImpl(DataModelV1.Article dto)
   {
      this.id = dto.id;
      this.articleType = dto.articleType;
      this.contentType = dto.contentType;
      this.title = dto.title;
      this.articleAbstract = dto.articleAbstract;
      this.body = dto.body;
      this.slug = dto.slug;
      this.footnotes = dto.footnotes.values().stream()
            .map(FootnoteImpl::new)
            .collect(Collectors.toMap(Footnote::getId, Function.identity()));
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
   public Collection<Footnote> getFootnotes()
   {
      return footnotes.values();
   }
}
