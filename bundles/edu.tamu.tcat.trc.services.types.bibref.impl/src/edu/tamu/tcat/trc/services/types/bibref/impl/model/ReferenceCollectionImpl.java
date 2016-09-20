/**
 *
 */
package edu.tamu.tcat.trc.services.types.bibref.impl.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import edu.tamu.tcat.trc.services.types.bibref.BibliographicItem;
import edu.tamu.tcat.trc.services.types.bibref.Citation;
import edu.tamu.tcat.trc.services.types.bibref.ReferenceCollection;
import edu.tamu.tcat.trc.services.types.bibref.impl.repo.DataModelV1;

/**
 * @author matthew.barry
 *
 */
public class ReferenceCollectionImpl implements ReferenceCollection
{
   private final Collection<Citation> citations = new ArrayList<>();
   private final Map<String, BibliographicItem> items = new HashMap<>();

   public ReferenceCollectionImpl()
   {
   }

   public ReferenceCollectionImpl(Collection<Citation> citations, Collection<BibliographicItem> items)
   {
      if (citations != null && !citations.isEmpty())
         citations.stream()
               .map(CitationImpl::new)
               .forEach(this.citations::add);

      if (items != null && !items.isEmpty())
         items.stream()
               .map(BibliographicItemImpl::new)
               .forEach(item -> this.items.put(item.getId(), item));
   }

   public ReferenceCollectionImpl(ReferenceCollection other)
   {
      if (other == null)
         return;

      Collection<Citation> otherCitations = other.getCitations();
      if (otherCitations != null && !otherCitations.isEmpty())
         otherCitations.stream()
               .map(CitationImpl::new)
               .forEach(citations::add);

      Collection<BibliographicItem> otherItems = other.getItems();
      if (otherItems != null && !otherItems.isEmpty())
         otherItems.stream()
               .map(BibliographicItemImpl::new)
               .forEach(item -> items.put(item.getId(), item));
   }

   public ReferenceCollectionImpl(DataModelV1.Bibliography dto)
   {
      if (dto == null)
         return;

      dto.citations.values().stream()
            .map(CitationImpl::new)
            .forEach(citations::add);

      dto.items.values().stream()
            .map(BibliographicItemImpl::new)
            .forEach(item -> items.put(item.getId(), item));
   }

   @Override
   public Collection<Citation> getCitations()
   {
      return Collections.unmodifiableCollection(citations);
   }

   @Override
   public BibliographicItem getItem(String id)
   {
      return items.get(id);
   }

   @Override
   public Collection<BibliographicItem> getItems()
   {
      return items.values();
   }

}
