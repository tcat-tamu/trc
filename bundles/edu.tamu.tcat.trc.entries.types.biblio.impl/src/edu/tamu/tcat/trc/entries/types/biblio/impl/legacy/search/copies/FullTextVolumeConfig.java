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

import edu.tamu.tcat.trc.search.solr.SearchException;
import edu.tamu.tcat.trc.search.solr.SolrIndexConfig;
import edu.tamu.tcat.trc.search.solr.SolrIndexField;
import edu.tamu.tcat.trc.search.solr.impl.BasicFields;

/**
 * Defines the Solr configuration for indexing full text search results on a page level.
 */
public class FullTextVolumeConfig implements SolrIndexConfig
{

   public static final SolrIndexField<String> ID = new BasicFields.BasicString("id");
   public static final SolrIndexField<String> ASSOC_ENTRY = new BasicFields.BasicString("associatedEntry");
   public static final SolrIndexField<String> TEXT = new BasicFields.BasicString("volumeText");

   public FullTextVolumeConfig()
   {
      // TODO Auto-generated constructor stub
   }

   @Override
   public void initialConfiguration(SolrQuery params) throws SearchException
   {
   }

   @Override
   public Class<Void> getSearchProxyType()
   {
      return Void.class;
   }

   @Override
   public Class<VolumeTextDocument> getIndexDocumentType()
   {
      return VolumeTextDocument.class;
   }

   @Override
   public Collection<? extends SolrIndexField<?>> getIndexedFields()
   {
      return Arrays.asList(ID, ASSOC_ENTRY, TEXT);
   }

   @Override
   public Collection<? extends SolrIndexField<?>> getStoredFields()
   {
      return Arrays.asList(ID, ASSOC_ENTRY, TEXT);
   }

   @Override
   public Collection<? extends SolrIndexField<?>> getMultiValuedFields()
   {
      return Collections.emptyList();
   }

}
