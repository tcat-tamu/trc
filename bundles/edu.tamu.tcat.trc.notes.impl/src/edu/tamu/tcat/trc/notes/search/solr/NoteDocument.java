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

import edu.tamu.tcat.account.Account;
import edu.tamu.tcat.trc.SearchException;
import edu.tamu.tcat.trc.entries.core.resolver.EntryReference;
import edu.tamu.tcat.trc.entries.core.resolver.EntryResolverRegistry;
import edu.tamu.tcat.trc.notes.Note;
import edu.tamu.tcat.trc.notes.NotesSearchProxy;
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

   public static SolrInputDocument create(Note note, EntryResolverRegistry resolvers)
   {
      try
      {
         Account author = note.getAuthor();
         EntryReference ref = note.getAssociatedEntry();
         TrcDocument indexDoc = new TrcDocument(new NotesSolrConfig());

         indexDoc.set(NotesSolrConfig.SEARCH_PROXY, toProxy(note));
         indexDoc.set(NotesSolrConfig.ID, note.getId());
         indexDoc.set(NotesSolrConfig.AUTHOR_ID, author != null ? author.getId().toString() : "");
         indexDoc.set(NotesSolrConfig.ASSOCIATED_ENTRY, ref != null ? resolvers.tokenize(ref) : "");
         indexDoc.set(NotesSolrConfig.NOTE_MIME_TYPE, guardNull(note.getMimeType()));
         indexDoc.set(NotesSolrConfig.NOTE_CONTENT, guardNull(note.getContent()));

         return indexDoc.build();
      }
      catch (SearchException ex)
      {
         throw new IllegalStateException("Failed to serialize NotesSearchProxy data", ex);
      }
   }

   private static NotesSearchProxy toProxy(Note note)
   {
      return null;
   }

   public static SolrInputDocument update(Note note, EntryResolverRegistry resolvers)
   {

      try
      {
         Account author = note.getAuthor();
         EntryReference ref = note.getAssociatedEntry();
         TrcDocument indexDoc = new TrcDocument(new NotesSolrConfig());

         indexDoc.update(NotesSolrConfig.SEARCH_PROXY, toProxy(note));
         indexDoc.update(NotesSolrConfig.ID, note.getId());
         indexDoc.update(NotesSolrConfig.AUTHOR_ID, author != null ? author.getId().toString() : "");
         indexDoc.update(NotesSolrConfig.ASSOCIATED_ENTRY, ref != null ? resolvers.tokenize(ref) : "");
         indexDoc.update(NotesSolrConfig.NOTE_MIME_TYPE, guardNull(note.getMimeType()));
         indexDoc.update(NotesSolrConfig.NOTE_CONTENT, guardNull(note.getContent()));

         return indexDoc.build();
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
