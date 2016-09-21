package edu.tamu.tcat.trc.services.types.bibref.impl.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.tamu.tcat.trc.services.types.bibref.BibliographicItem;
import edu.tamu.tcat.trc.services.types.bibref.BibliographicItemMeta;
import edu.tamu.tcat.trc.services.types.bibref.Creator;
import edu.tamu.tcat.trc.services.types.bibref.impl.repo.DataModelV1;

public class BibliographicItemImpl implements BibliographicItem
{
   private final String id;
   private final String type;
   private final BibliographicItemMeta meta;
   private final List<Creator> creators = new ArrayList<>();
   private final Map<String, String> fields = new HashMap<>();

   public BibliographicItemImpl(DataModelV1.BibliographicItem dto)
   {
      id = dto.id;
      type = dto.type;
      meta = new BibliographicItemMetaImpl(dto.meta);

      dto.creators.stream()
            .map(CreatorImpl::new)
            .forEach(creators::add);

      fields.putAll(dto.fields);
   }

   @Override
   public String getItemId()
   {
      return id;
   }

   @Override
   public String getType()
   {
      return type;
   }

   @Override
   public BibliographicItemMeta getMetadata()
   {
      return meta;
   }

   @Override
   public List<Creator> getCreators()
   {
      return Collections.unmodifiableList(creators);
   }

   @Override
   public Map<String, String> getFields()
   {
      return Collections.unmodifiableMap(fields);
   }
}
