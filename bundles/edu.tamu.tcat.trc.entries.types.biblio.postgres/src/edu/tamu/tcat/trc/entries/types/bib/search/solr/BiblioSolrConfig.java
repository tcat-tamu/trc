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
package edu.tamu.tcat.trc.entries.types.bib.search.solr;

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
   public static final BasicFields.SearchProxyField<BiblioSearchProxy> SEARCH_PROXY = new BasicFields.SearchProxyField<BiblioSearchProxy>("workInfo", BiblioSearchProxy.class);
   public static final SolrIndexField<String> AUTHOR_IDS = new BasicFields.BasicString("authorIds");
   public static final SolrIndexField<String> AUTHOR_NAMES = new BasicFields.BasicString("authorNames");
   public static final SolrIndexField<String> AUTHOR_ROLES = new BasicFields.BasicString("authorRole");  // not needed
   public static final SolrIndexField<String> TITLE_TYPES = new BasicFields.BasicString("titleTypes");   // not needed
   public static final SolrIndexField<String> LANGUAGES = new BasicFields.BasicString("lang");           // not needed
   public static final SolrIndexField<String> TITLES = new BasicFields.BasicString("titles");
   public static final SolrIndexField<String> SUBTITLES = new BasicFields.BasicString("subtitles");      // needed? could be joined with titles unless we want to boost separately
   public static final SolrIndexField<String> PUBLISHER = new BasicFields.BasicString("publisher");
   public static final SolrIndexField<String> PUBLISHER_LOCATION = new BasicFields.BasicString("publisherLocation");
   public static final SolrIndexField<String> PUBLICATION_DATE_STRING = new BasicFields.BasicString("publishDateString");  // simply date, expressed as a YYYY-MM-DD
   // Using LocalDate for yyyy-MM-dd
   public static final SolrIndexField<LocalDate> PUBLICATION_DATE = new BasicFields.BasicDate("publishDateValue");
   public static final SolrIndexField<String> SERIES = new BasicFields.BasicString("series");
   public static final SolrIndexField<String> SUMMARY = new BasicFields.BasicString("summary");          // NOTE - critical search field
   public static final SolrIndexField<String> EDITION_ID = new BasicFields.BasicString("editionId");
   public static final SolrIndexField<String> EDITION_NAME = new BasicFields.BasicString("editionName");    // not needed (store in info)
   public static final SolrIndexField<String> VOLUME_ID = new BasicFields.BasicString("volumeId");
   public static final SolrIndexField<String> VOLUME_NUMBER = new BasicFields.BasicString("volumeNumber");  // not needed (store in info)
   public static final SolrIndexField<String> HAS_IMAGES = new BasicFields.BasicString("hasImages");
   public static final SolrIndexField<String> VOLUME_TAGS = new BasicFields.BasicString("volumeTags");
   public static final SolrIndexField<String> VOLUME_NOTES = new BasicFields.BasicString("volumeNotes");

   @Override
   public void initialConfiguration(SolrQuery params)
   {
      /*
       * Using eDisMax seemed like a more adventagous way of doing the query. This will allow
       * additional solr Paramaters to be set in order to 'fine tune' the query.
       */
      params.set("defType", "edismax");
   }

   @Override
   public void configureBasic(String q, SolrQuery params)
   {
      //HACK: if no query specified, should this throw and require a call to queryAll() ?
      if (q == null || q.trim().isEmpty())
         q = "*:*";

      // NOTE query against all fields, boosted appropriately, free text
      //      I think that means *:(qBasic)
      // NOTE in general, if this is applied, the other query params are unlikely to be applied
      StringBuilder qBuilder = new StringBuilder(q);

      // Avoid searching over editions and volumes, only for "basic" search
      qBuilder.append(" -editionName:(*)")
              .append(" -volumeNumber:(*)");

      params.set("q", qBuilder.toString());

      // Basic query only searches over these fields
      params.set("qf", "titles^3 authorNames authorIds");
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
                           AUTHOR_IDS,
                           AUTHOR_NAMES,
                           LANGUAGES,
                           TITLES,
                           SUBTITLES,
                           PUBLISHER,
                           PUBLISHER_LOCATION,
                           PUBLICATION_DATE_STRING,
                           PUBLICATION_DATE,
                           SERIES,
                           SUMMARY,
                           EDITION_ID,
                           EDITION_NAME,
                           VOLUME_ID,
                           VOLUME_NUMBER,
                           HAS_IMAGES,
                           VOLUME_TAGS,
                           VOLUME_NOTES);
   }

   @Override
   public Collection<? extends SolrIndexField<?>> getStoredFields()
   {
      return Arrays.asList(ID,
                           SEARCH_PROXY,
                           AUTHOR_IDS,
                           AUTHOR_NAMES,
                           AUTHOR_ROLES,
                           TITLE_TYPES,
                           LANGUAGES,
                           TITLES,
                           SUBTITLES,
                           PUBLISHER,
                           PUBLISHER_LOCATION,
                           PUBLICATION_DATE_STRING,
                           PUBLICATION_DATE,
                           SERIES,
                           SUMMARY,
                           EDITION_ID,
                           EDITION_NAME,
                           VOLUME_ID,
                           VOLUME_NUMBER,
                           HAS_IMAGES,
                           VOLUME_TAGS,
                           VOLUME_NOTES);
   }

   @Override
   public Collection<? extends SolrIndexField<?>> getMultiValuedFields()
   {
      return Arrays.asList(AUTHOR_IDS,
                           AUTHOR_NAMES,
                           AUTHOR_ROLES,
                           TITLE_TYPES,
                           LANGUAGES,
                           TITLES,
                           SUBTITLES,
                           VOLUME_TAGS,
                           VOLUME_NOTES);
   }
}
