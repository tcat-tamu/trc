package edu.tamu.tcat.trc.entries.bib.copies.model;

import java.net.URI;
import java.util.Objects;
import java.util.UUID;

import edu.tamu.tcat.trc.entries.bib.copies.CopyReference;
import edu.tamu.tcat.trc.entries.bib.copies.EditCopyReferenceCommand;

public abstract class BaseEditCopyRefCmd implements EditCopyReferenceCommand
{

   protected final CopyReference original;
   protected final CopyRefDTO dto;

   public BaseEditCopyRefCmd()
   {
      this.original = null;
      this.dto = new CopyRefDTO();
      this.dto.id = UUID.randomUUID();
   }

   public BaseEditCopyRefCmd(CopyRefDTO dto)
   {
      this.original = CopyRefDTO.instantiate(dto);
      this.dto = CopyRefDTO.copy(dto);
   }

   @Override
   public final CopyReference getCurrentState()
   {
      return CopyRefDTO.instantiate(dto);
   }

   @Override
   public final UUID getId()
   {
      return dto.id;
   }

   @Override
   public final void update(CopyRefDTO updates) throws IllegalArgumentException
   {
      if (!isNew())
      {
         if (dto.id != null && updates.id != null && !Objects.equals(dto.id, updates.id))
            throw new IllegalArgumentException("Invalid update: identifiers do not match. "
                  + "Current [" + dto.id + "]. Updates [" + updates.id + "]");
      }

      if (dto.id == null)
         dto.id = updates.id;

      if (updates.associatedEntry != null)
         setAssociatedEntry(updates.associatedEntry);

      if (updates.copyId != null)
         setCopyId(updates.copyId);
      if (updates.title != null)
         setTitle(updates.title);
      if (updates.summary != null)
         setSummary(updates.summary);
      if (updates.rights != null)
         setRights(updates.rights);
   }

   @Override
   public final EditCopyReferenceCommand setAssociatedEntry(URI uri)
   {
      dto.associatedEntry = uri;
      return this;
   }

   @Override
   public final EditCopyReferenceCommand setCopyId(String id)
   {
      dto.copyId = id;
      return this;
   }

   @Override
   public final EditCopyReferenceCommand setTitle(String value)
   {
      dto.title = value;
      return this;
   }

   @Override
   public final EditCopyReferenceCommand setSummary(String value)
   {
      dto.summary = value;
      return this;
   }

   @Override
   public final EditCopyReferenceCommand setRights(String description)
   {
      dto.rights = description;
      return this;
   }

   protected final boolean isNew()
   {
      return original == null;
   }

}
