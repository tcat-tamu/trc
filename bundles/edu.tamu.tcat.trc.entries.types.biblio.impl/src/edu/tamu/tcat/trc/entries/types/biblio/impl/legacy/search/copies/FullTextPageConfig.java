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
package edu.tamu.tcat.trc.entries.types.biblio.impl.legacy.search.copies;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.apache.solr.client.solrj.SolrQuery;

import edu.tamu.tcat.trc.entries.types.biblio.search.copies.PageSearchProxy;
import edu.tamu.tcat.trc.search.solr.SearchException;
import edu.tamu.tcat.trc.search.solr.SolrIndexConfig;
import edu.tamu.tcat.trc.search.solr.SolrIndexField;
import edu.tamu.tcat.trc.search.solr.impl.BasicFields;

/**
 * Defines the Solr configuration for indexing full text search results on a page level.
 */
public class FullTextPageConfig implements SolrIndexConfig
{

   public static final SolrIndexField<String> ID = new BasicFields.BasicString("id");
   public static final SolrIndexField<String> TEXT = new BasicFields.BasicString("pageText");
   public static final SolrIndexField<Integer> NUMBER = new BasicFields.BasicInteger("pageNumber");
   public static final SolrIndexField<String> SEQUENCE = new BasicFields.BasicString("pageSequence");


   public FullTextPageConfig()
   {
   }

   @Override
   public void initialConfiguration(SolrQuery params) throws SearchException
   {
   }

   // TODO ulg -- can we configure some of this in a plugin?
   @Override
   public Class<PageSearchProxy> getSearchProxyType()
   {
      return PageSearchProxy.class;
   }

   @Override
   public Class<PageTextDocument> getIndexDocumentType()
   {
      return PageTextDocument.class;
   }

   @Override
   public Collection<? extends SolrIndexField<?>> getIndexedFields()
   {
      return Arrays.asList(ID, TEXT, NUMBER, SEQUENCE);
   }

   @Override
   public Collection<? extends SolrIndexField<?>> getStoredFields()
   {
      return Arrays.asList(ID, TEXT, NUMBER, SEQUENCE);
   }

   @Override
   public Collection<? extends SolrIndexField<?>> getMultiValuedFields()
   {
      return Collections.emptyList();
   }

}
