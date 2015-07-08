package edu.tamu.tcat.trc.notes.search;

import edu.tamu.tcat.trc.notes.Note;

public class NotesSearchProxy
{

   public String id;
   public String authorId;
   public String associatedEntity;
   public String content;
   public String mimeType;


   public NotesSearchProxy()
   {
   }

   public NotesSearchProxy(Note note)
   {
      this.id = note.getId().toString();
      this.authorId = note.getAuthorId().toString();
      this.associatedEntity = note.getEntity().toString();
      this.content = note.getContent();
      this.mimeType = note.getMimeType();
   }
}
