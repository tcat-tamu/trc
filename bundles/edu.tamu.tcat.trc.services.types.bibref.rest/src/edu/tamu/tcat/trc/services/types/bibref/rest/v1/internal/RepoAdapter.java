package edu.tamu.tcat.trc.services.types.bibref.rest.v1.internal;

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
import edu.tamu.tcat.trc.services.types.bibref.rest.v1.RestApiV1;

/**
 * An encapsulation of adapter methods to convert between repository API and the {@link RestApiV1} schema DTOs.
 */
public class RepoAdapter
{
   public static RestApiV1.ReferenceCollection toDTO(ReferenceCollection orig)
   {
      if (orig == null)
         return null;

      RestApiV1.ReferenceCollection dto = new RestApiV1.ReferenceCollection();

      dto.citations.clear();
      Collection<Citation> citations = orig.getCitations();
      if (citations != null && !citations.isEmpty())
         citations.stream()
               .map(RepoAdapter::toDTO)
               .forEach(citation -> dto.citations.put(citation.id, citation));

      dto.bibliography.clear();
      Collection<BibliographicItem> items = orig.getItems();
      if (items != null && !items.isEmpty())
         items.stream()
               .map(RepoAdapter::toDTO)
               .forEach(item -> dto.bibliography.put(item.id, item));

      return dto;
   }

   public static RestApiV1.Citation toDTO(Citation orig)
   {
      if (orig == null)
         return null;

      RestApiV1.Citation dto = new RestApiV1.Citation();

      dto.id = orig.getId();

      dto.items.clear();
      List<BibliographicItemReference> citedItems = orig.getCitedItems();
      if (citedItems != null && !citedItems.isEmpty())
         citedItems.stream()
               .map(RepoAdapter::toDTO)
               .forEach(dto.items::add);

      return dto;
   }

   public static RestApiV1.BibliographicItemReference toDTO(BibliographicItemReference orig)
   {
      if (orig == null)
         return null;

      RestApiV1.BibliographicItemReference dto = new RestApiV1.BibliographicItemReference();

      dto.id = orig.getItemId();
      dto.locatorType = orig.getLocatorType();
      dto.locator = orig.getLocator();
      dto.label = orig.getLabel();

      return dto;
   }

   public static RestApiV1.BibliographicItem toDTO(BibliographicItem orig)
   {
      if (orig == null)
         return null;

      RestApiV1.BibliographicItem dto = new RestApiV1.BibliographicItem();

      dto.id = orig.getId();
      dto.type = orig.getType();

      adaptOnto(dto.meta, orig.getMetadata());

      dto.creators.clear();
      List<Creator> creators = orig.getCreators();
      if (creators != null && !creators.isEmpty())
         creators.stream()
               .map(RepoAdapter::toDTO)
               .forEach(dto.creators::add);

      dto.fields.clear();
      Map<String,String> fields = orig.getFields();
      if (fields != null && !fields.isEmpty())
         dto.fields.putAll(fields);

      return dto;
   }

   public static RestApiV1.Creator toDTO(Creator orig)
   {
      if (orig == null)
         return null;

      RestApiV1.Creator dto = new RestApiV1.Creator();

      dto.role = orig.getRole();
      dto.firstName = orig.getFirstName();
      dto.lastName = orig.getLastName();
      dto.name = orig.getName();

      return dto;
   }

   public static BibliographicItemMutator.Creator toRepo(RestApiV1.Creator restDto)
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

   private static RestApiV1.BibliographicItemMeta adaptOnto(RestApiV1.BibliographicItemMeta dest, BibliographicItemMeta source)
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
