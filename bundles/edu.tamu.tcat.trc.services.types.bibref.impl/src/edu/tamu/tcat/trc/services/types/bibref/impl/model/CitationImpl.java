package edu.tamu.tcat.trc.services.types.bibref.impl.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import edu.tamu.tcat.trc.services.types.bibref.BibliographicItemReference;
import edu.tamu.tcat.trc.services.types.bibref.Citation;
import edu.tamu.tcat.trc.services.types.bibref.impl.repo.DataModelV1;

public class CitationImpl implements Citation
{
   private final String id;
   private final List<BibliographicItemReference> citedItems = new ArrayList<>();

   public CitationImpl(String id)
   {
      this(id, Collections.emptyList());
   }

   public CitationImpl(String id, List<BibliographicItemReference> citedItems)
   {
      this.id = id;

      if (citedItems != null && !citedItems.isEmpty())
         citedItems.stream()
               .map(BibliographicItemReferenceImpl::new)
               .forEach(this.citedItems::add);
   }

   public CitationImpl(Citation other)
   {
      Objects.requireNonNull(other);

      id = other.getId();

      List<BibliographicItemReference> otherCitedItems = other.getCitedItems();
      if (otherCitedItems != null && !otherCitedItems.isEmpty())
         otherCitedItems.stream()
               .map(BibliographicItemReferenceImpl::new)
               .forEach(citedItems::add);
   }

   public CitationImpl(DataModelV1.Citation dto)
   {
      Objects.requireNonNull(dto);

      id = dto.id;

      dto.citedItems.stream()
            .map(BibliographicItemReferenceImpl::new)
            .forEach(citedItems::add);
   }

   @Override
   public String getId()
   {
      return id;
   }

   @Override
   public List<BibliographicItemReference> getCitedItems()
   {
      return Collections.unmodifiableList(citedItems);
   }

}
