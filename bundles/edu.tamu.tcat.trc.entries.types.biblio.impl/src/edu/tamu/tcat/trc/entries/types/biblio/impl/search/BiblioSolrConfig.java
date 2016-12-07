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
package edu.tamu.tcat.trc.entries.types.biblio.impl.search;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collection;

import org.apache.solr.client.solrj.SolrQuery;

import edu.tamu.tcat.trc.entries.types.biblio.search.BiblioSearchProxy;
import edu.tamu.tcat.trc.search.solr.SolrIndexConfig;
import edu.tamu.tcat.trc.search.solr.SolrIndexField;
import edu.tamu.tcat.trc.search.solr.impl.BasicFields;

public class BiblioSolrConfig implements SolrIndexConfig
{
   public static final SolrIndexField<String> ID = new BasicFields.BasicString("id");
   public static final SolrIndexField<String> ENTRY_REFERENCE = new BasicFields.BasicString("entryRef");
   public static final SolrIndexField<String> SUMMARY = new BasicFields.BasicString("summary");          // NOTE - critical search field
   public static final SolrIndexField<String> AUTHOR_IDS = new BasicFields.BasicString("authorIds");
   public static final SolrIndexField<String> AUTHOR_NAMES = new BasicFields.BasicString("authorNames");
   public static final SolrIndexField<String> TITLES = new BasicFields.BasicString("titles");
   public static final SolrIndexField<String> PUBLISHER = new BasicFields.BasicString("publisher");
   public static final SolrIndexField<String> PUBLISHER_LOCATION = new BasicFields.BasicString("pubLocation");
   public static final SolrIndexField<LocalDate> PUBLICATION_DATE = new BasicFields.BasicDate("pubDate"); // Using LocalDate for yyyy-MM-dd
   public static final BasicFields.SearchProxyField<BiblioSearchProxy> SEARCH_PROXY = new BasicFields.SearchProxyField<>("proxy", BiblioSearchProxy.class);

   @Override
   public void initialConfiguration(SolrQuery params)
   {
   }

   @Override
   public Class<BiblioSearchProxy> getSearchProxyType()
   {
      return BiblioSearchProxy.class;
   }

   @Override
   public Class<BiblioDocument> getIndexDocumentType()
   {
      return BiblioDocument.class;
   }

   @Override
   public Collection<? extends SolrIndexField<?>> getIndexedFields()
   {
      return Arrays.asList(ID,
                           ENTRY_REFERENCE,
                           SUMMARY,
                           AUTHOR_IDS,
                           AUTHOR_NAMES,
                           TITLES,
                           PUBLISHER,
                           PUBLISHER_LOCATION,
                           PUBLICATION_DATE,
                           SEARCH_PROXY);
   }

   @Override
   public Collection<? extends SolrIndexField<?>> getStoredFields()
   {
      return Arrays.asList(ID,
                           ENTRY_REFERENCE,
                           SEARCH_PROXY);
   }

   @Override
   public Collection<? extends SolrIndexField<?>> getMultiValuedFields()
   {
      return Arrays.asList(AUTHOR_IDS,
                           AUTHOR_NAMES,
                           TITLES);
   }
}
