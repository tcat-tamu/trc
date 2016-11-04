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
package edu.tamu.tcat.trc.entries.types.article.impl.search;

import static java.util.stream.Collectors.toList;

import org.apache.solr.common.SolrInputDocument;

import edu.tamu.tcat.trc.entries.types.article.Article;
import edu.tamu.tcat.trc.entries.types.article.repo.ArticleRepository;
import edu.tamu.tcat.trc.entries.types.article.search.ArticleSearchProxy;
import edu.tamu.tcat.trc.resolver.EntryId;
import edu.tamu.tcat.trc.resolver.EntryResolverRegistry;
import edu.tamu.tcat.trc.search.solr.impl.TrcDocument;


public class SearchAdapter
{
   public static SolrInputDocument adapt(Article article, EntryResolverRegistry resolvers)
   {
      TrcDocument doc = new TrcDocument(new ArticleSolrConfig());

      try
      {
         String token = resolvers.tokenize(new EntryId(article.getId(), ArticleRepository.ENTRY_TYPE_ID));
         doc.set(ArticleSolrConfig.SEARCH_PROXY, makeSearchProxy(article, token));

         doc.set(ArticleSolrConfig.ID, article.getId());
         doc.set(ArticleSolrConfig.TITLE, guardNull(article.getTitle()));
         doc.set(ArticleSolrConfig.ARTICLE_ABSTRACT, guardNull(article.getAbstract()));
         doc.set(ArticleSolrConfig.ARTICLE_CONTENT, guardNull(article.getBody()));
         doc.set(ArticleSolrConfig.ENTRY_REFERENCE, token);

         article.getAuthors().stream()
            .map(author -> author.getDisplayName())
            .forEach(name -> doc.set(ArticleSolrConfig.AUTHOR_NAMES, name));

         return doc.build();
      }
      catch (Exception e)
      {
         throw new IllegalStateException("Failed to serialize AritcleSearchProxy data", e);
      }
   }

   private static ArticleSearchProxy makeSearchProxy(Article article, String token)
   {
      ArticleSearchProxy proxy = new ArticleSearchProxy();
      proxy.id = article.getId();
      proxy.token = token;
      proxy.title = article.getTitle();
      proxy.articleType = article.getArticleType();

      proxy.authors = article.getAuthors().stream()
            .map(author -> author.getDisplayName())
            .collect(toList());

      return proxy;
   }

   private static String guardNull(String value)
   {
      return value == null ? "" : value;
   }

}
