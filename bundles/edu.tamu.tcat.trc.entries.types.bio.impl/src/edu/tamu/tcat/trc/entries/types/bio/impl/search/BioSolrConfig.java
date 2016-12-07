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
package edu.tamu.tcat.trc.entries.types.bio.impl.search;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collection;

import org.apache.solr.client.solrj.SolrQuery;

import edu.tamu.tcat.trc.entries.types.bio.search.BioSearchProxy;
import edu.tamu.tcat.trc.search.solr.SolrIndexConfig;
import edu.tamu.tcat.trc.search.solr.SolrIndexField;
import edu.tamu.tcat.trc.search.solr.impl.BasicFields;

public class BioSolrConfig implements SolrIndexConfig
{
   public static final SolrIndexField<String> ID = new BasicFields.BasicString("id");
   public static final SolrIndexField<String> ENTRY_REFERENCE = new BasicFields.BasicString("entryRef");

   public static final SolrIndexField<String> SUMMARY = new BasicFields.BasicString("summary");
   public static final SolrIndexField<String> FAMILY_NAME = new BasicFields.BasicString("familyName");
   public static final SolrIndexField<String> GIVEN_NAME = new BasicFields.BasicString("givenName");
   public static final SolrIndexField<String> ALT_NAMES = new BasicFields.BasicString("altNames");
   // Using LocalDate for yyyy-MM-dd
   public static final SolrIndexField<String> BIRTH_LOCATION = new BasicFields.BasicString("birthLocation");
   public static final SolrIndexField<LocalDate> BIRTH_DATE = new BasicFields.BasicDate("birthDate");
   public static final SolrIndexField<String> DEATH_LOCATION = new BasicFields.BasicString("deathLocation");
   public static final SolrIndexField<LocalDate> DEATH_DATE = new BasicFields.BasicDate("deathDate");

   public static final BasicFields.SearchProxyField<BioSearchProxy> SEARCH_PROXY = new BasicFields.SearchProxyField<>("proxy", BioSearchProxy.class);


   @Override
   public void initialConfiguration(SolrQuery params)
   {
   }

   @Override
   public Class<BioSearchProxy> getSearchProxyType()
   {
      return BioSearchProxy.class;
   }

   @Override
   public Collection<? extends SolrIndexField<?>> getIndexedFields()
   {
      return Arrays.asList(ID,
                           ENTRY_REFERENCE,
                           SUMMARY,
                           FAMILY_NAME,
                           GIVEN_NAME,
                           ALT_NAMES,
                           BIRTH_LOCATION,
                           BIRTH_DATE,
                           DEATH_LOCATION,
                           DEATH_DATE);
   }

   @Override
   public Collection<? extends SolrIndexField<?>> getStoredFields()
   {
      return Arrays.asList(ID,
                           ENTRY_REFERENCE,
                           SUMMARY,
                           BIRTH_LOCATION,
                           BIRTH_DATE,
                           DEATH_LOCATION,
                           DEATH_DATE);
   }

   @Override
   public Collection<? extends SolrIndexField<?>> getMultiValuedFields()
   {
      return Arrays.asList(ALT_NAMES);
   }
}
