package edu.tamu.tcat.trc.notes.repo.basic;

import java.net.URI;
import java.util.Objects;
import java.util.UUID;

import edu.tamu.tcat.trc.notes.Notes;
import edu.tamu.tcat.trc.notes.dto.NotesDTO;
import edu.tamu.tcat.trc.notes.repo.EditNotesCommand;

public abstract class BasicEditNotesCommand implements EditNotesCommand
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
   public final void update(NotesDTO updateDTO)
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
         setAuthorId(updateDTO.authorId);

      if (updateDTO.mimeType != null)
         setMimeType(updateDTO.mimeType);

      if (updateDTO.content != null)
         setContent(updateDTO.content);
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

   protected final boolean isNew()
   {
      return original == null;
   }
}
