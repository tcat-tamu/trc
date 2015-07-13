package edu.tamu.tcat.trc.entries.types.article.search.solr;

import org.apache.solr.common.SolrInputDocument;

import com.fasterxml.jackson.core.JsonProcessingException;

import edu.tamu.tcat.trc.entries.types.article.Article;
import edu.tamu.tcat.trc.entries.types.article.dto.ArticleDTO;
import edu.tamu.tcat.trc.entries.types.article.search.ArticleSearchProxy;
import edu.tamu.tcat.trc.search.SearchException;
import edu.tamu.tcat.trc.search.solr.impl.TrcDocument;


public class ArticleDocument
{
   private TrcDocument indexDoc;

   public ArticleDocument()
   {
      indexDoc = new TrcDocument(new ArticleSolrConfig());
   }

   public SolrInputDocument getDocument()
   {
      return indexDoc.getSolrDocument();
   }

   public static ArticleDocument create(Article note) throws JsonProcessingException, SearchException
   {
      ArticleDocument doc = new ArticleDocument();

      ArticleDTO dto = ArticleDTO.create(note);

      try
      {
         doc.indexDoc.set(ArticleSolrConfig.SEARCH_PROXY, new ArticleSearchProxy(note));
      }
      catch (Exception e)
      {
         throw new IllegalStateException("Failed to serialize NotesSearchProxy data", e);
      }

      doc.indexDoc.set(ArticleSolrConfig.ID, dto.id.toString());
      doc.indexDoc.set(ArticleSolrConfig.TITLE, dto.id.toString());
      doc.indexDoc.set(ArticleSolrConfig.AUTHOR_ID, guardNull(dto.authorId));
      doc.indexDoc.set(ArticleSolrConfig.ASSOCIATED_ENTRY, guardNull(dto.associatedEntity.toString()));
      doc.indexDoc.set(ArticleSolrConfig.ARTICLE_MIME_TYPE, guardNull(dto.mimeType));
      doc.indexDoc.set(ArticleSolrConfig.ARTICLE_CONTENT, guardNull(dto.content));

      return doc;
   }

   public static ArticleDocument update(Article note) throws SearchException
   {
      ArticleDocument doc = new ArticleDocument();
      ArticleDTO dto = ArticleDTO.create(note);

      try
      {
         doc.indexDoc.update(ArticleSolrConfig.SEARCH_PROXY, new ArticleSearchProxy(note));
      }
      catch (Exception e)
      {
         throw new IllegalStateException("Failed to serialize NotesSearchProxy data", e);
      }

      doc.indexDoc.set(ArticleSolrConfig.ID, dto.id.toString());
      doc.indexDoc.update(ArticleSolrConfig.TITLE, dto.id.toString());
      doc.indexDoc.update(ArticleSolrConfig.AUTHOR_ID, guardNull(dto.authorId));
      doc.indexDoc.update(ArticleSolrConfig.ASSOCIATED_ENTRY, guardNull(dto.associatedEntity.toString()));
      doc.indexDoc.update(ArticleSolrConfig.ARTICLE_MIME_TYPE, guardNull(dto.mimeType));
      doc.indexDoc.update(ArticleSolrConfig.ARTICLE_CONTENT, guardNull(dto.content));

      return doc;
   }

   private static String guardNull(String value)
   {
      return value == null ? "" : value;
   }

}
