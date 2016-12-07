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
package edu.tamu.tcat.trc.entries.types.reln.impl.search;

import java.util.Arrays;
import java.util.Collection;

import org.apache.solr.client.solrj.SolrQuery;

import edu.tamu.tcat.trc.entries.types.reln.search.RelnSearchProxy;
import edu.tamu.tcat.trc.search.solr.SolrIndexConfig;
import edu.tamu.tcat.trc.search.solr.SolrIndexField;
import edu.tamu.tcat.trc.search.solr.impl.BasicFields;

public class RelnSolrConfig implements SolrIndexConfig
{
   public static final SolrIndexField<String> ID = new BasicFields.BasicString("id");
   public static final SolrIndexField<String> ENTRY_REFERENCE = new BasicFields.BasicString("entryRef");
   public static final SolrIndexField<String> REL_TYPE = new BasicFields.BasicString("relnType");
   public static final SolrIndexField<String> DESCRIPTION = new BasicFields.BasicString("summary");
   public static final SolrIndexField<String> RELATED_ENTITIES = new BasicFields.BasicString("related");
   public static final SolrIndexField<String> TARGET_ENTITIES = new BasicFields.BasicString("target");
   public static final BasicFields.SearchProxyField<RelnSearchProxy> SEARCH_PROXY = new BasicFields.SearchProxyField<>("proxy", RelnSearchProxy.class);

   @Override
   public void initialConfiguration(SolrQuery params)
   {
   }

   @Override
   public Class<RelnSearchProxy> getSearchProxyType()
   {
      return RelnSearchProxy.class;
   }

   @Override
   public Class<RelnDocument> getIndexDocumentType()
   {
      return RelnDocument.class;
   }

   @Override
   public Collection<? extends SolrIndexField<?>> getIndexedFields()
   {
      return Arrays.asList(ID,
                           DESCRIPTION,
                           REL_TYPE,
                           RELATED_ENTITIES,
                           TARGET_ENTITIES,
                           ENTRY_REFERENCE);
   }

   @Override
   public Collection<? extends SolrIndexField<?>> getStoredFields()
   {
      return Arrays.asList(ID,
                           SEARCH_PROXY,
                           DESCRIPTION,
                           REL_TYPE,
                           RELATED_ENTITIES,
                           TARGET_ENTITIES,
                           ENTRY_REFERENCE);
   }

   @Override
   public Collection<? extends SolrIndexField<?>> getMultiValuedFields()
   {
      return Arrays.asList(RELATED_ENTITIES,
                           TARGET_ENTITIES);
   }
}
