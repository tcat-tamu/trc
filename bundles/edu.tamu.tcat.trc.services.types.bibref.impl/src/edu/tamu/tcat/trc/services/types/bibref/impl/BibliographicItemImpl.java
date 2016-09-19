package edu.tamu.tcat.trc.services.types.bibref.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.tamu.tcat.trc.services.types.bibref.BibliographicItem;
import edu.tamu.tcat.trc.services.types.bibref.BibliographicItemMeta;
import edu.tamu.tcat.trc.services.types.bibref.Creator;

public class BibliographicItemImpl implements BibliographicItem
{
   private String id;
   private String type;
   private final BibliographicItemMeta meta;
   private final List<Creator> creators = new ArrayList<>();
   private final Map<String, String> fields = new HashMap<>();

   public BibliographicItemImpl()
   {
      meta = new BibliographicItemMetaImpl();
   }

   public BibliographicItemImpl(String id, String type, BibliographicItemMeta meta, List<Creator> creators, Map<String, String> fields)
   {
      this.id = id;
      this.type = type;
      this.meta = new BibliographicItemMetaImpl(meta);

      if (creators != null)
         creators.stream().map(CreatorImpl::new).forEach(this.creators::add);

      if (fields != null)
         this.fields.putAll(fields);
   }

   public BibliographicItemImpl(BibliographicItem other)
   {
      if (other == null)
      {
         meta = new BibliographicItemMetaImpl();
         return;
      }

      id = other.getId();
      type = other.getType();
      meta = new BibliographicItemMetaImpl(other.getMetadata());

      List<Creator> otherCreators = other.getCreators();
      if (otherCreators != null && !otherCreators.isEmpty())
         otherCreators.stream().map(CreatorImpl::new).forEach(creators::add);

      Map<String, String> otherFields = other.getFields();
      if (otherFields != null && !otherFields.isEmpty())
         fields.putAll(otherFields);
   }

   @Override
   public String getId()
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
