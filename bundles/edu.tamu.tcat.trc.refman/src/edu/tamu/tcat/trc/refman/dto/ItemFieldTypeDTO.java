package edu.tamu.tcat.trc.refman.dto;

import edu.tamu.tcat.trc.refman.types.ItemFieldType;

public class ItemFieldTypeDTO
{
   public String id;
   public String label;
   public String type;
   public String description;

   public static ItemFieldTypeDTO create(ItemFieldType itemField)
   {
      ItemFieldTypeDTO dto = new ItemFieldTypeDTO();
      
      dto.id = itemField.getId();
      dto.label = itemField.getLabel();
      dto.type = itemField.getType();
      dto.description = itemField.getDescription();
      
      return dto;
   }
}
