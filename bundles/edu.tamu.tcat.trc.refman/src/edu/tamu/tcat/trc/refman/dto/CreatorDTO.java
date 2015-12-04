package edu.tamu.tcat.trc.refman.dto;

import edu.tamu.tcat.trc.refman.BibliographicReference.CreatorValue;

public class CreatorDTO
{
   public String role;
   public String firstName;
   public String lastName;
   public String authoritiveId;

   public static CreatorDTO create(CreatorValue creatorVal)
   {
      CreatorDTO dto = new CreatorDTO();
      dto.firstName = creatorVal.getGivenName();
      dto.lastName = creatorVal.getFamilyName();
      dto.role = creatorVal.getRoleId();
      dto.authoritiveId = creatorVal.getAuthId() != null ? creatorVal.getAuthId().toString() : null;

      return dto;
   }
}
