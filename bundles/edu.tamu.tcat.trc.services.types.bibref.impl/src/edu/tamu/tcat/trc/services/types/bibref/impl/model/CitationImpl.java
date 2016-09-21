package edu.tamu.tcat.trc.services.types.bibref.impl.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import edu.tamu.tcat.trc.services.types.bibref.BibliographicItemReference;
import edu.tamu.tcat.trc.services.types.bibref.Citation;
import edu.tamu.tcat.trc.services.types.bibref.impl.repo.DataModelV1;

public class CitationImpl implements Citation
{
   private final String id;
   private final List<BibliographicItemReference> citedItems = new ArrayList<>();

   public CitationImpl(DataModelV1.Citation dto)
   {
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
