package edu.tamu.tcat.trc.notes.postgres;

import java.net.URI;
import java.util.Objects;
import java.util.UUID;

import edu.tamu.tcat.trc.notes.Note;
import edu.tamu.tcat.trc.notes.dto.NoteDTO;
import edu.tamu.tcat.trc.notes.repo.EditNoteCommand;

public abstract class BasicEditNoteCommand implements EditNoteCommand
{

   protected final Note original;
   protected final NoteDTO dto;

   public BasicEditNoteCommand()
   {
      this.original = null;
      this.dto = new NoteDTO();
      this.dto.id = UUID.randomUUID();
   }

   public BasicEditNoteCommand(NoteDTO dto)
   {
      this.original = NoteDTO.instantiate(dto);
      this.dto = NoteDTO.copy(dto);
   }

   @Override
   public final void update(NoteDTO updateDTO)
   {
      if (!isNew())
      {
         if(dto.id != null && updateDTO.id != null & !Objects.equals(dto.id, updateDTO.id))
            throw new IllegalArgumentException("Invalid update: identifiers do not match. "
                  + "Current [" + dto.id + "]. Updates [" + updateDTO.id + "]");
      }

      if (dto.id == null)
         dto.id = updateDTO.id;

      if (updateDTO.associatedEntity != null)
         setEntity(updateDTO.associatedEntity);

      if (updateDTO.authorId != null)
         setAuthorId(UUID.fromString(updateDTO.authorId));

      if (updateDTO.mimeType != null)
         setMimeType(updateDTO.mimeType);

      if (updateDTO.content != null)
         setContent(updateDTO.content);
   }

   @Override
   public EditNoteCommand setAll(NoteDTO note)
   {
      setEntity(note.associatedEntity);
      setAuthorId(UUID.fromString(note.authorId));
      setMimeType(note.mimeType);
      setContent(note.content);
      return this;
   }

   @Override
   public UUID getId()
   {
      return dto.id;
   }

   @Override
   public EditNoteCommand setEntity(URI entityURI)
   {
      dto.associatedEntity = entityURI;
      return this;
   }

   @Override
   public EditNoteCommand setAuthorId(UUID authorId)
   {
      dto.authorId = authorId.toString();
      return this;
   }

   @Override
   public EditNoteCommand setMimeType(String mimeType)
   {
      dto.mimeType = mimeType;
      return this;
   }

   @Override
   public EditNoteCommand setContent(String content)
   {
      dto.content = content;
      return this;
   }

   protected final boolean isNew()
   {
      return original == null;
   }
}
