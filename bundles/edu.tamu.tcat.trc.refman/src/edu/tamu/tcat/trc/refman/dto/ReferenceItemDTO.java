package edu.tamu.tcat.trc.refman.dto;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.tamu.tcat.trc.refman.BibliographicReference;
import edu.tamu.tcat.trc.refman.types.ItemFieldType;
import edu.tamu.tcat.trc.refman.types.ItemType;

@Deprecated // use BibRefDTO
public class ReferenceItemDTO
{
   public URI id;
   public ItemType type;
   public Map<String, String> typeFields = new HashMap<>();
   public List<CreatorDTO> creators = new ArrayList<>();

   public static ReferenceItemDTO create(BibliographicReference ref)
   {
      ReferenceItemDTO dto = new ReferenceItemDTO();

      dto.id = ref.getId();
      dto.type = ref.getType();
      for (ItemFieldType field : dto.type.getFields())
      {
         dto.typeFields.put(field.getId(), ref.getValue(field));
      }

      ref.getCreators().forEach((c)-> dto.creators.add(CreatorDTO.create(c)));

      return dto;
   }
}
