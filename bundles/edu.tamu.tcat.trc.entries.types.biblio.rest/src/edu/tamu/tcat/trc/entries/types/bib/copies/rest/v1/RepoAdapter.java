package edu.tamu.tcat.trc.entries.types.bib.copies.rest.v1;

import edu.tamu.tcat.trc.entries.types.biblio.copies.CopyReference;
import edu.tamu.tcat.trc.entries.types.biblio.copies.dto.CopyRefDTO;

/**
 * An encapsulation of adapter methods to convert between the repository API and
 * the {@link RestApiV1} schema DTOs.
 */
public class RepoAdapter
{
   public static RestApiV1.CopyReference toDTO(CopyReference orig)
   {
      if (orig == null)
         return null;
      
      RestApiV1.CopyReference dto = new RestApiV1.CopyReference();
      dto.id = orig.getId();
      dto.associatedEntry = orig.getAssociatedEntry();
      dto.copyId = orig.getCopyId();

      dto.title = orig.getTitle();
      dto.summary = orig.getSummary();
      dto.rights = orig.getRights();
      
      return dto;
   }
   
   public static CopyRefDTO toRepo(RestApiV1.CopyReference orig)
   {
      if (orig == null)
         return null;
      
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
