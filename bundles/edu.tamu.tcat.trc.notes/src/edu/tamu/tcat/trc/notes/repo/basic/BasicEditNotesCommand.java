package edu.tamu.tcat.trc.notes.repo.basic;

import java.net.URI;
import java.util.UUID;

import edu.tamu.tcat.trc.notes.Notes;
import edu.tamu.tcat.trc.notes.dto.NotesDTO;
import edu.tamu.tcat.trc.notes.repo.EditNotesCommand;

public class BasicEditNotesCommand implements EditNotesCommand
{

   protected final Notes original;
   protected final NotesDTO dto;

   public BasicEditNotesCommand()
   {
      this.original = null;
      this.dto = new NotesDTO();
      this.dto.id = UUID.randomUUID();
   }

   public BasicEditNotesCommand(NotesDTO dto)
   {
      this.original = NotesDTO.instantiate(dto);
      this.dto = NotesDTO.copy(dto);
   }

   @Override
   public UUID getId()
   {
      return dto.id;
   }

   @Override
   public EditNotesCommand setEntity(URI entityURI)
   {
      dto.associatedEntity = entityURI;
      return this;
   }

   @Override
   public EditNotesCommand setAuthorId(UUID authorId)
   {
      dto.authorId = authorId;
      return this;
   }

   @Override
   public EditNotesCommand setMimeType(String mimeType)
   {
      dto.mimeType = mimeType;
      return this;
   }

   @Override
   public EditNotesCommand setContent(String content)
   {
      dto.content = content;
      return this;
   }

}
