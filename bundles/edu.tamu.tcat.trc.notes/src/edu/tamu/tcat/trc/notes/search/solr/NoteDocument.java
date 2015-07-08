package edu.tamu.tcat.trc.notes.search.solr;

import org.apache.solr.common.SolrInputDocument;

import com.fasterxml.jackson.core.JsonProcessingException;

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
      return indexDoc.getSolrDocument();
   }

   public static NoteDocument create(Note note) throws JsonProcessingException, SearchException
   {
      NoteDocument doc = new NoteDocument();

      NoteDTO dto = NoteDTO.create(note);

      try
      {
         doc.indexDoc.set(NotesSolrConfig.SEARCH_PROXY, new NotesSearchProxy(note));
      }
      catch (Exception e)
      {
         throw new IllegalStateException("Failed to serialize NotesSearchProxy data", e);
      }

      doc.indexDoc.set(NotesSolrConfig.ID, dto.id.toString());
      doc.indexDoc.set(NotesSolrConfig.AUTHOR_ID, guardNull(dto.authorId));
      doc.indexDoc.set(NotesSolrConfig.ASSOCIATED_ENTRY, guardNull(dto.associatedEntity.toString()));
      doc.indexDoc.set(NotesSolrConfig.NOTE_MIME_TYPE, guardNull(dto.mimeType));
      doc.indexDoc.set(NotesSolrConfig.NOTE_CONTENT, guardNull(dto.content));

      return doc;
   }

   public static NoteDocument update(Note note) throws SearchException
   {
      NoteDocument doc = new NoteDocument();
      NoteDTO dto = NoteDTO.create(note);

      try
      {
         doc.indexDoc.update(NotesSolrConfig.SEARCH_PROXY, new NotesSearchProxy(note));
      }
      catch (Exception e)
      {
         throw new IllegalStateException("Failed to serialize NotesSearchProxy data", e);
      }

      doc.indexDoc.set(NotesSolrConfig.ID, dto.id.toString());
      doc.indexDoc.update(NotesSolrConfig.AUTHOR_ID, guardNull(dto.authorId));
      doc.indexDoc.update(NotesSolrConfig.ASSOCIATED_ENTRY, guardNull(dto.associatedEntity.toString()));
      doc.indexDoc.update(NotesSolrConfig.NOTE_MIME_TYPE, guardNull(dto.mimeType));
      doc.indexDoc.update(NotesSolrConfig.NOTE_CONTENT, guardNull(dto.content));

      return doc;
   }

   private static String guardNull(String value)
   {
      return value == null ? "" : value;
   }

}
