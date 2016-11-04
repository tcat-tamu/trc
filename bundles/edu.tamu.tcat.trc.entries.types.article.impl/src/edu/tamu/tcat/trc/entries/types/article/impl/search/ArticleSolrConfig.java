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

import java.util.Arrays;
import java.util.Collection;

import org.apache.solr.client.solrj.SolrQuery;

import edu.tamu.tcat.trc.entries.types.article.search.ArticleSearchProxy;
import edu.tamu.tcat.trc.search.solr.SearchException;
import edu.tamu.tcat.trc.search.solr.SolrIndexConfig;
import edu.tamu.tcat.trc.search.solr.SolrIndexField;
import edu.tamu.tcat.trc.search.solr.impl.BasicFields;


public class ArticleSolrConfig implements SolrIndexConfig
{
   public static final SolrIndexField<String> ID = new BasicFields.BasicString("id");
   public static final SolrIndexField<String> ENTRY_REFERENCE = new BasicFields.BasicString("entryRef");
   public static final SolrIndexField<String> AUTHORS = new BasicFields.BasicString("authors");
   public static final SolrIndexField<String> TITLE = new BasicFields.BasicString("title");
   public static final SolrIndexField<String> ARTICLE_ABSTRACT = new BasicFields.BasicString("abstract");
   public static final SolrIndexField<String> ARTICLE_CONTENT = new BasicFields.BasicString("text");
   public static final SolrIndexField<String> ARTICLE_TYPE = new BasicFields.BasicString("type");

   public static final String CATCH_ALL_NAMES = "catchAllNames";

   public static final BasicFields.SearchProxyField<ArticleSearchProxy> SEARCH_PROXY =
         new BasicFields.SearchProxyField<>("proxy", ArticleSearchProxy.class);

   /**
    * @since 1.1
    */
   @Override
   public void initialConfiguration(SolrQuery params) throws SearchException
   {
//      params.set("qf",TITLE.getName(), AUTHOR_NAMES.getName(), ARTICLE_CONTENT.getName(), ARTICLE_ABSTRACT.getName());
//      params.set("qf",TITLE.getName(), CATCH_ALL_NAMES, ARTICLE_CONTENT.getName(), ARTICLE_ABSTRACT.getName());
//
//      params.set("hl", "true");
//      params.set("hl.fl", ARTICLE_ABSTRACT.getName(), ARTICLE_CONTENT.getName());
//
//      // TODO refactor, enable more general configuration tooling
//      params.set("facet", "true");
//      params.set("facet.field", AUTHOR_NAMES.getName());
//      params.set("facet.mincount", "1");
   }

   /**
    * @since 1.1
    */
   @Override
   public void configureBasic(String q, SolrQuery params) throws SearchException
   {
      params.set("q", q);
   }

   @Override
   public Class<ArticleSearchProxy> getSearchProxyType()
   {
      return ArticleSearchProxy.class;
   }

   @Override
   public Class<SearchAdapter> getIndexDocumentType()
   {
      return SearchAdapter.class;
   }

   @Override
   public Collection<? extends SolrIndexField<?>> getIndexedFields()
   {
      return Arrays.asList(ID, ENTRY_REFERENCE, AUTHORS, TITLE, ARTICLE_ABSTRACT, ARTICLE_CONTENT, ARTICLE_TYPE, SEARCH_PROXY);
   }

   @Override
   public Collection<? extends SolrIndexField<?>> getStoredFields()
   {
      return Arrays.asList(ID, ENTRY_REFERENCE, AUTHORS, TITLE, SEARCH_PROXY);
   }

   @Override
   public Collection<? extends SolrIndexField<?>> getMultiValuedFields()
   {
      return Arrays.asList(AUTHORS);
   }

}
