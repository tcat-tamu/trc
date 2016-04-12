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
package edu.tamu.tcat.trc.notes.search.solr;

import java.util.Arrays;
import java.util.Collection;

import edu.tamu.tcat.trc.notes.search.NotesSearchProxy;
import edu.tamu.tcat.trc.search.solr.SolrIndexConfig;
import edu.tamu.tcat.trc.search.solr.SolrIndexField;
import edu.tamu.tcat.trc.search.solr.impl.BasicFields;

public class NotesSolrConfig implements SolrIndexConfig
{
   public static final SolrIndexField<String> ID = new BasicFields.BasicString("id");
   public static final SolrIndexField<String> AUTHOR_ID = new BasicFields.BasicString("author_id");
   public static final SolrIndexField<String> ASSOCIATED_ENTRY = new BasicFields.BasicString("associated_entry");
   public static final SolrIndexField<String> NOTE_CONTENT = new BasicFields.BasicString("note_content");
   public static final SolrIndexField<String> NOTE_MIME_TYPE = new BasicFields.BasicString("mime_type");

   public static final BasicFields.SearchProxyField<NotesSearchProxy> SEARCH_PROXY =new BasicFields.SearchProxyField<NotesSearchProxy>("note_dto", NotesSearchProxy.class);

   @Override
   public Class<NotesSearchProxy> getSearchProxyType()
   {
      return NotesSearchProxy.class;
   }

   @Override
   public Class<NoteDocument> getIndexDocumentType()
   {
      return NoteDocument.class;
   }

   @Override
   public Collection<? extends SolrIndexField<?>> getIndexedFields()
   {
      return Arrays.asList(ID, AUTHOR_ID, ASSOCIATED_ENTRY, NOTE_CONTENT, NOTE_MIME_TYPE);
   }

   @Override
   public Collection<? extends SolrIndexField<?>> getStoredFields()
   {
      return Arrays.asList(ID, AUTHOR_ID, ASSOCIATED_ENTRY, NOTE_CONTENT, NOTE_MIME_TYPE, SEARCH_PROXY);
   }

   @Override
   public Collection<? extends SolrIndexField<?>> getMultiValuedFields()
   {
      return Arrays.asList();
   }

}
