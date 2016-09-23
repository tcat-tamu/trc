package edu.tamu.tcat.trc.services.types.bibref.rest.v1;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import edu.tamu.tcat.trc.services.types.bibref.BibliographicItem;
import edu.tamu.tcat.trc.services.types.bibref.BibliographicItemMeta;
import edu.tamu.tcat.trc.services.types.bibref.BibliographicItemReference;
import edu.tamu.tcat.trc.services.types.bibref.Citation;
import edu.tamu.tcat.trc.services.types.bibref.Creator;
import edu.tamu.tcat.trc.services.types.bibref.ReferenceCollection;
import edu.tamu.tcat.trc.services.types.bibref.repo.BibliographicItemMutator;

/**
 * An encapsulation of adapter methods to convert between repository API and the {@link BibRefRestApiV1} schema DTOs.
 */
public class BibRefRestAdapter
{
   public static BibRefRestApiV1.ReferenceCollection toDTO(ReferenceCollection orig)
   {
      if (orig == null)
         return null;

      BibRefRestApiV1.ReferenceCollection dto = new BibRefRestApiV1.ReferenceCollection();

      dto.citations.clear();
      Collection<Citation> citations = orig.getCitations();
      if (citations != null && !citations.isEmpty())
         citations.stream()
               .map(BibRefRestAdapter::toDTO)
               .forEach(citation -> dto.citations.put(citation.id, citation));

      dto.bibliography.clear();
      Collection<BibliographicItem> items = orig.getItems();
      if (items != null && !items.isEmpty())
         items.stream()
               .map(BibRefRestAdapter::toDTO)
               .forEach(item -> dto.bibliography.put(item.id, item));

      return dto;
   }

   public static BibRefRestApiV1.Citation toDTO(Citation orig)
   {
      if (orig == null)
         return null;

      BibRefRestApiV1.Citation dto = new BibRefRestApiV1.Citation();

      dto.id = orig.getId();

      dto.items.clear();
      List<BibliographicItemReference> citedItems = orig.getCitedItems();
      if (citedItems != null && !citedItems.isEmpty())
         citedItems.stream()
               .map(BibRefRestAdapter::toDTO)
               .forEach(dto.items::add);

      return dto;
   }

   public static BibRefRestApiV1.BibliographicItemReference toDTO(BibliographicItemReference orig)
   {
      if (orig == null)
         return null;

      BibRefRestApiV1.BibliographicItemReference dto = new BibRefRestApiV1.BibliographicItemReference();

      dto.id = orig.getItemId();
      dto.locatorType = orig.getLocatorType();
      dto.locator = orig.getLocator();
      dto.label = orig.getLabel();

      return dto;
   }

   public static BibRefRestApiV1.BibliographicItem toDTO(BibliographicItem orig)
   {
      if (orig == null)
         return null;

      BibRefRestApiV1.BibliographicItem dto = new BibRefRestApiV1.BibliographicItem();

      dto.id = orig.getItemId();
      dto.type = orig.getType();

      adaptOnto(dto.meta, orig.getMetadata());

      dto.creators.clear();
      List<Creator> creators = orig.getCreators();
      if (creators != null && !creators.isEmpty())
         creators.stream()
               .map(BibRefRestAdapter::toDTO)
               .forEach(dto.creators::add);

      dto.fields.clear();
      Map<String,String> fields = orig.getFields();
      if (fields != null && !fields.isEmpty())
         dto.fields.putAll(fields);

      return dto;
   }

   public static BibRefRestApiV1.Creator toDTO(Creator orig)
   {
      if (orig == null)
         return null;

      BibRefRestApiV1.Creator dto = new BibRefRestApiV1.Creator();

      dto.role = orig.getRole();
      dto.firstName = orig.getFirstName();
      dto.lastName = orig.getLastName();
      dto.name = orig.getName();

      return dto;
   }

   public static BibliographicItemMutator.Creator toRepo(BibRefRestApiV1.Creator restDto)
   {
      if (restDto == null)
         return null;

      BibliographicItemMutator.Creator modelDto = new BibliographicItemMutator.Creator();

      modelDto.role = restDto.role;
      modelDto.firstName = restDto.firstName;
      modelDto.lastName = restDto.lastName;
      modelDto.name = restDto.name;

      return modelDto;
   }

   private static BibRefRestApiV1.BibliographicItemMeta adaptOnto(BibRefRestApiV1.BibliographicItemMeta dest, BibliographicItemMeta source)
   {
      if (dest != null && source != null)
      {
         dest.key = source.getKey();
         dest.creatorSummary = source.getCreatorSummary();
         dest.parsedDate = source.getParsedDate();
         dest.dateAdded = source.getDateAdded();
         dest.dateModified = source.getDateModified();
      }

      return dest;
   }
}
