package edu.tamu.tcat.trc.refman.dto;

import java.util.ArrayList;
import java.util.List;

import edu.tamu.tcat.trc.refman.types.CreatorRole;
import edu.tamu.tcat.trc.refman.types.ItemFieldType;
import edu.tamu.tcat.trc.refman.types.ItemType;

public class ItemTypeDTO
{
   public List<CreatorRoleDTO> creatorRoles;
   public String description;
   public List<ItemFieldTypeDTO> fields;
   public String id;
   public String label;

   public static ItemTypeDTO create(ItemType item)
   {
      ItemTypeDTO dto = new ItemTypeDTO();
      
      dto.creatorRoles = getRoles(item.getCreatorRoles());
      dto.description = item.getDescription();
      dto.fields = getItemFields(item.getFields());
      dto.id = item.getId();
      dto.label = item.getLabel();
      
      return dto;
   }
   
   static List<CreatorRoleDTO> getRoles(List<CreatorRole> roles)
   {
      List<CreatorRoleDTO> roleDTOs = new ArrayList<>();
      
      roles.forEach(role ->
      {
         roleDTOs.add(CreatorRoleDTO.create(role));
      });

      return roleDTOs;
   }
   
   static List<ItemFieldTypeDTO> getItemFields(List<ItemFieldType> fields)
   {
      List<ItemFieldTypeDTO> itemFields = new ArrayList<>();
      
      fields.forEach(field ->
      {
         itemFields.add(ItemFieldTypeDTO.create(field));
      });
      
      return itemFields;
   }
   
}
