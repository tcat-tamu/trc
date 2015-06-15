package edu.tamu.tcat.trc.notes.solr.index;

import java.net.URI;
import java.util.UUID;

import org.apache.solr.common.SolrInputDocument;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.tamu.tcat.trc.notes.Notes;
import edu.tamu.tcat.trc.notes.dto.NotesDTO;

public class NotesSolrProxy
{
   private final String NOTE_ID = "id";
   private final String AUTHOR_ID = "author_id";
   private final String ASSOCIATED_ENTRY = "associated_entry";
   private final String NOTE_CONTENT = "note_content";
   private final String NOTE_MIME_TYPE = "mime_type";
   private final String NOTE_DTO = "note_dto";

   private SolrInputDocument noteDoc;

   public NotesSolrProxy()
   {
      noteDoc = new SolrInputDocument();
   }

   public SolrInputDocument create(Notes note) throws JsonProcessingException
   {
      setNoteId(note.getId());
      setAuthorId(note.getAuthorId());
      setAssociatedEntry(note.getEntity());
      setContent(note.getContent());
      setMimeType(note.getMimeType());
      setNoteDTO(note);
      return this.noteDoc;
   }

   public SolrInputDocument update(Notes note) throws JsonProcessingException
   {
      setNoteId(note.getId());
      setAuthorId(note.getAuthorId());
      setAssociatedEntry(note.getEntity());
      setContent(note.getContent());
      setMimeType(note.getMimeType());
      setNoteDTO(note);
      return this.noteDoc;
   }

   private void setNoteId(UUID id)
   {
      noteDoc.addField(NOTE_ID, id.toString());
   }

   private void setAuthorId(UUID id)
   {
      noteDoc.addField(AUTHOR_ID, id.toString());
   }

   private void setAssociatedEntry(URI entry)
   {
      noteDoc.addField(ASSOCIATED_ENTRY, entry.toString());
   }

   private void setContent(String content)
   {
      noteDoc.addField(NOTE_CONTENT, content);
   }

   private void setMimeType(String mimeType)
   {
      noteDoc.addField(NOTE_MIME_TYPE, mimeType);
   }

   private void setNoteDTO(Notes note) throws JsonProcessingException
   {
      NotesDTO noteDTO = NotesDTO.create(note);
      ObjectMapper mapper = new ObjectMapper();
      mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

      noteDoc.addField(NOTE_DTO, mapper.writeValueAsString(noteDTO));
   }
}
