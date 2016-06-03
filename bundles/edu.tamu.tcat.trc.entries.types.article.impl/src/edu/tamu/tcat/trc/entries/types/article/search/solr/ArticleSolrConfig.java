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

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collection;

import org.apache.solr.client.solrj.SolrQuery;

import edu.tamu.tcat.trc.entries.types.article.search.ArticleSearchProxy;
import edu.tamu.tcat.trc.search.SearchException;
import edu.tamu.tcat.trc.search.solr.SolrIndexConfig;
import edu.tamu.tcat.trc.search.solr.SolrIndexField;
import edu.tamu.tcat.trc.search.solr.impl.BasicFields;


public class ArticleSolrConfig implements SolrIndexConfig
{
   public static final SolrIndexField<String> ID = new BasicFields.BasicString("id");
   public static final SolrIndexField<String> TITLE = new BasicFields.BasicString("title");
   public static final SolrIndexField<String> AUTHOR_ID = new BasicFields.BasicString("author_id");
   public static final SolrIndexField<String> ASSOCIATED_ENTRY = new BasicFields.BasicString("associated_entry");
   public static final SolrIndexField<String> ARTICLE_CONTENT = new BasicFields.BasicString("article_content");
   public static final SolrIndexField<String> ARTICLE_MIME_TYPE = new BasicFields.BasicString("mime_type");
   public static final SolrIndexField<String> AUTHOR_NAMES = new BasicFields.BasicString("author_names");
   public static final SolrIndexField<String> ARTICLE_ABSTRACT = new BasicFields.BasicString("article_abstract");
   public static final SolrIndexField<LocalDate>   PUBLISHED = new BasicFields.BasicDate("published");

   public static final String CATCH_ALL_NAMES = "catchAllNames";

   public static final BasicFields.SearchProxyField<ArticleSearchProxy> SEARCH_PROXY =
         new BasicFields.SearchProxyField<>("article_dto", ArticleSearchProxy.class);

   /**
    * @since 1.1
    */
   @Override
   public void initialConfiguration(SolrQuery params) throws SearchException
   {
      /*
       * Using eDisMax seemed like a more adventagous way of doing the query. This will allow
       * additional solr Paramaters to be set in order to 'fine tune' the query.
       */
      params.set("defType", "edismax");
      params.set("qf",TITLE.getName(), AUTHOR_NAMES.getName(), ARTICLE_CONTENT.getName(), ARTICLE_ABSTRACT.getName());
      params.set("qf",TITLE.getName(), CATCH_ALL_NAMES, ARTICLE_CONTENT.getName(), ARTICLE_ABSTRACT.getName());

      params.set("hl", "true");
      params.set("hl.fl", ARTICLE_ABSTRACT.getName(), ARTICLE_CONTENT.getName());

      // TODO refactor, enable more general configuration tooling
      params.set("facet", "true");
      params.set("facet.field", AUTHOR_NAMES.getName());
      params.set("facet.mincount", "1");
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
   public Class<ArticleDocument> getIndexDocumentType()
   {
      return ArticleDocument.class;
   }

   @Override
   public Collection<? extends SolrIndexField<?>> getIndexedFields()
   {
      return Arrays.asList(ID, AUTHOR_ID, AUTHOR_NAMES, ARTICLE_ABSTRACT, PUBLISHED, ASSOCIATED_ENTRY, ARTICLE_CONTENT, ARTICLE_MIME_TYPE);
   }

   @Override
   public Collection<? extends SolrIndexField<?>> getStoredFields()
   {
      return Arrays.asList(ID, AUTHOR_ID, AUTHOR_NAMES, ARTICLE_ABSTRACT, PUBLISHED, ASSOCIATED_ENTRY, ARTICLE_CONTENT, ARTICLE_MIME_TYPE, SEARCH_PROXY);
   }

   @Override
   public Collection<? extends SolrIndexField<?>> getMultiValuedFields()
   {
      return Arrays.asList();
   }

}
