/*
 * Copyright 2015 Texas A&M Engineering Experiment Station
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

   public static ArticleDocument create(Article article) throws JsonProcessingException, SearchException
   {
      ArticleDocument doc = new ArticleDocument();

      ArticleDTO dto = ArticleDTO.create(article);

      try
      {
         doc.indexDoc.set(ArticleSolrConfig.SEARCH_PROXY, new ArticleSearchProxy(article));
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

   public static ArticleDocument update(Article article) throws SearchException
   {
      ArticleDocument doc = new ArticleDocument();
      ArticleDTO dto = ArticleDTO.create(article);

      try
      {
         doc.indexDoc.update(ArticleSolrConfig.SEARCH_PROXY, new ArticleSearchProxy(article));
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
