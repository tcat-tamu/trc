package edu.tamu.tcat.trc.refman.dto;

import edu.tamu.tcat.trc.refman.types.CreatorRole;

public class CreatorRoleDTO
{
   public String id;
   public String label;
   public String description;

   public static CreatorRoleDTO create(CreatorRole role)
   {
      CreatorRoleDTO dto = new CreatorRoleDTO();
      
      dto.id = role.getId();
      dto.label = role.getLabel();
      dto.description = role.getDescription();
      
      return dto;
   }
}
