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

import org.apache.solr.common.SolrInputDocument;

import edu.tamu.tcat.trc.notes.Note;
import edu.tamu.tcat.trc.notes.dto.NoteDTO;
import edu.tamu.tcat.trc.notes.search.NotesSearchProxy;
import edu.tamu.tcat.trc.search.SearchException;
import edu.tamu.tcat.trc.search.solr.impl.TrcDocument;

public class NoteDocument
{
   private TrcDocument indexDoc;

   public NoteDocument()
   {
      indexDoc = new TrcDocument(new NotesSolrConfig());
   }

   public SolrInputDocument getDocument()
   {
      return indexDoc.build();
   }

   public static NoteDocument create(Note note)
   {
      try
      {
         NoteDocument doc = new NoteDocument();
         NoteDTO dto = NoteDTO.create(note);

         doc.indexDoc.set(NotesSolrConfig.SEARCH_PROXY, new NotesSearchProxy(note));
         doc.indexDoc.set(NotesSolrConfig.ID, dto.id.toString());
         doc.indexDoc.set(NotesSolrConfig.AUTHOR_ID, guardNull(dto.authorId));
         doc.indexDoc.set(NotesSolrConfig.ASSOCIATED_ENTRY, guardNull(dto.associatedEntity.toString()));
         doc.indexDoc.set(NotesSolrConfig.NOTE_MIME_TYPE, guardNull(dto.mimeType));
         doc.indexDoc.set(NotesSolrConfig.NOTE_CONTENT, guardNull(dto.content));

         return doc;
      }
      catch (SearchException ex)
      {
         throw new IllegalStateException("Failed to serialize NotesSearchProxy data", ex);
      }
   }

   public static NoteDocument update(Note note)
   {

      try
      {
         NoteDocument doc = new NoteDocument();
         NoteDTO dto = NoteDTO.create(note);

         doc.indexDoc.set(NotesSolrConfig.ID, dto.id.toString());

         doc.indexDoc.update(NotesSolrConfig.SEARCH_PROXY, new NotesSearchProxy(note));
         doc.indexDoc.update(NotesSolrConfig.AUTHOR_ID, guardNull(dto.authorId));
         doc.indexDoc.update(NotesSolrConfig.ASSOCIATED_ENTRY, guardNull(dto.associatedEntity.toString()));
         doc.indexDoc.update(NotesSolrConfig.NOTE_MIME_TYPE, guardNull(dto.mimeType));
         doc.indexDoc.update(NotesSolrConfig.NOTE_CONTENT, guardNull(dto.content));

         return doc;
      }
      catch (Exception e)
      {
         throw new IllegalStateException("Failed to serialize NotesSearchProxy data", e);
      }
   }

   private static String guardNull(String value)
   {
      return value == null ? "" : value;
   }

}
