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

import edu.tamu.tcat.trc.entries.types.article.Article;
import edu.tamu.tcat.trc.entries.types.article.search.ArticleSearchProxy;
import edu.tamu.tcat.trc.search.solr.impl.TrcDocument;


public class SearchAdapter
{
   public static SolrInputDocument adapt(Article article)
   {
      TrcDocument doc = new TrcDocument(new ArticleSolrConfig());

      try
      {
         doc.set(ArticleSolrConfig.SEARCH_PROXY, makeSearchProxy(article));

         doc.set(ArticleSolrConfig.ID, article.getId());
         doc.set(ArticleSolrConfig.TITLE, guardNull(article.getTitle()));
         doc.set(ArticleSolrConfig.ARTICLE_ABSTRACT, guardNull(article.getAbstract()));
         doc.set(ArticleSolrConfig.ARTICLE_CONTENT, guardNull(article.getBody()));

         // TODO index authors

         return doc.build();
      }
      catch (Exception e)
      {
         throw new IllegalStateException("Failed to serialize AritcleSearchProxy data", e);
      }
   }

   private static ArticleSearchProxy makeSearchProxy(Article dto)
   {
      ArticleSearchProxy proxy = new ArticleSearchProxy();
      proxy.id = dto.getId();
      proxy.title = dto.getTitle();
      proxy.articleType = dto.getArticleType();

      // TODO add authors

      return proxy;
   }

   private static String guardNull(String value)
   {
      return value == null ? "" : value;
   }

}
