package edu.tamu.tcat.trc.entries.bib.copies.model;

import java.net.URI;
import java.util.UUID;

import edu.tamu.tcat.trc.entries.bib.copies.CopyReference;

public class CopyRefDTO
{
   public UUID id;
   public URI associatedEntry;
   public String copyId;
   public String title;
   public String summary;
   public String rights;

   public static CopyReference instantiate(CopyRefDTO dto)
   {
      return new BasicCopyReference(dto.id, dto.associatedEntry, dto.copyId, dto.title, dto.summary, dto.rights);
   }

   public static CopyRefDTO create(CopyReference ref)
   {
      CopyRefDTO dto = new CopyRefDTO();

      dto.id = ref.getId();
      dto.associatedEntry = ref.getAssociatedEntry();
      dto.copyId = ref.getCopyId();

      dto.title = ref.getTitle();
      dto.summary = ref.getSummary();
      dto.rights = ref.getRights();

      return dto;
   }

   public static CopyRefDTO copy(CopyRefDTO orig)
   {
      CopyRefDTO dto = new CopyRefDTO();

      dto.id = orig.id;
      dto.associatedEntry = orig.associatedEntry;
      dto.copyId = orig.copyId;

      dto.title = orig.title;
      dto.summary = orig.summary;
      dto.rights = orig.rights;

      return dto;
   }
}
