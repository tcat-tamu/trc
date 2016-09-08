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

import edu.tamu.tcat.trc.entries.types.article.docrepo.DataModelV1;
import edu.tamu.tcat.trc.entries.types.article.search.ArticleSearchProxy;
import edu.tamu.tcat.trc.search.solr.DocumentBuilder;
import edu.tamu.tcat.trc.search.solr.impl.TrcDocument;


public class ArticleDocument
{


   public static DocumentBuilder adapt(DataModelV1.Article dto)
   {
      TrcDocument doc = new TrcDocument(new ArticleSolrConfig());

      try
      {
         doc.set(ArticleSolrConfig.SEARCH_PROXY, makeSearchProxy(dto));

         doc.set(ArticleSolrConfig.ID, dto.id.toString());
         doc.set(ArticleSolrConfig.TITLE, guardNull(dto.title));
         doc.set(ArticleSolrConfig.ARTICLE_ABSTRACT, guardNull(dto.articleAbstract));
         doc.set(ArticleSolrConfig.ARTICLE_CONTENT, guardNull(dto.body));

//      if (dto.authors != null || dto.authors.isEmpty())
//         setAuthorNames(doc, dto.authors);

         return doc;
      }
      catch (Exception e)
      {
         throw new IllegalStateException("Failed to serialize AritcleSearchProxy data", e);
      }
   }

   private static ArticleSearchProxy makeSearchProxy(DataModelV1.Article dto)
   {
      ArticleSearchProxy proxy = new ArticleSearchProxy();
      proxy.id = dto.id;
      proxy.title = dto.title;
      proxy.articleType = dto.articleType;

      // TODO add authors
//      proxy.authors = article.getAuthors().stream()
//            .map(AuthorRef::new)
//            .collect(Collectors.toList());

      return proxy;
   }
//
//   public static ArticleDocument create(Article article) throws JsonProcessingException, SearchException
//   {
//      ArticleDocument doc = new ArticleDocument();
//
//      ArticleDTO dto = ArticleDTO.create(article);
//
//      try
//      {
//         doc.indexDoc.set(ArticleSolrConfig.SEARCH_PROXY, new ArticleSearchProxy(article));
//      }
//      catch (Exception e)
//      {
//         throw new IllegalStateException("Failed to serialize AritcleSearchProxy data", e);
//      }
//
//      doc.indexDoc.set(ArticleSolrConfig.ID, dto.id.toString());
//      doc.indexDoc.set(ArticleSolrConfig.TITLE, guardNull(dto.title));
//      doc.indexDoc.set(ArticleSolrConfig.ARTICLE_ABSTRACT, guardNull(dto.articleAbstract));
//      doc.indexDoc.set(ArticleSolrConfig.ARTICLE_CONTENT, guardNull(dto.body));
//      if (dto.authors != null || dto.authors.isEmpty())
//         setAuthorNames(doc, dto.authors);
//
//      return doc;
//   }
//
//   public static ArticleDocument update(Article article) throws SearchException
//   {
//      // TODO use changeset, don't store proxy separately.
//      ArticleDocument doc = new ArticleDocument();
//      ArticleDTO dto = ArticleDTO.create(article);
//
//      try
//      {
//         doc.indexDoc.update(ArticleSolrConfig.SEARCH_PROXY, new ArticleSearchProxy(article));
//      }
//      catch (Exception e)
//      {
//         throw new IllegalStateException("Failed to serialize NotesSearchProxy data", e);
//      }
//
//      doc.indexDoc.set(ArticleSolrConfig.ID, dto.id.toString());
//      doc.indexDoc.update(ArticleSolrConfig.TITLE, guardNull(dto.title));
//      doc.indexDoc.update(ArticleSolrConfig.ARTICLE_ABSTRACT, guardNull(dto.articleAbstract));
//      doc.indexDoc.update(ArticleSolrConfig.ARTICLE_CONTENT, guardNull(dto.body));
//      if (dto.authors == null || dto.authors.isEmpty())
//         updateAuthorNames(doc, dto.authors);
//
//      return doc;
//   }
//
//   private static void setAuthorNames(ArticleDocument doc, List<ArticleAuthorDTO> authors)
//   {
//      authors.forEach((a) ->
//      {
//         try
//         {
//            doc.indexDoc.set(ArticleSolrConfig.AUTHOR_NAMES, a.name);
//         }
//         catch (Exception e)
//         {
//            throw new IllegalStateException("Failed to set author names", e);
//         }
//      });
//   }
//
//   private static void updateAuthorNames(ArticleDocument doc, List<ArticleAuthorDTO> authors)
//   {
//      authors.forEach((a) ->
//      {
//         try
//         {
//            doc.indexDoc.set(ArticleSolrConfig.AUTHOR_NAMES, a.name);
//         }
//         catch (Exception e)
//         {
//            throw new IllegalStateException("Failed to update author names", e);
//         }
//      });
//   }

   private static String guardNull(String value)
   {
      return value == null ? "" : value;
   }

}
