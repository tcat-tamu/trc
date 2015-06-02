package edu.tamu.tcat.trc.entries.types.bio.rest.v1;

import java.util.ArrayList;
import java.util.List;

import edu.tamu.tcat.trc.entries.types.bio.dto.PersonNameDTO;
import edu.tamu.tcat.trc.entries.types.bio.search.BioSearchProxy;

/**
 * An encapsulation of adapter methods to convert between the search API and
 * the {@link RestApiV1} schema DTOs.
 */
public class SearchAdapter
{
   public static List<RestApiV1.PersonSearchResult> toDTO(List<BioSearchProxy> origList)
   {
      if (origList == null)
         return null;
      List<RestApiV1.PersonSearchResult> dtoList = new ArrayList<>();
      for (BioSearchProxy orig : origList)
      {
         RestApiV1.PersonSearchResult dto = new RestApiV1.PersonSearchResult();
         dto.id = orig.id;
         dto.displayName = toDTO(orig.displayName);
         dto.formattedName = orig.formattedName;

         dtoList.add(dto);
      }

      return dtoList;
   }

   private static RestApiV1.PersonName toDTO(PersonNameDTO orig)
   {
      if (orig == null)
         return null;
      RestApiV1.PersonName dto = new RestApiV1.PersonName();
      dto.title = orig.title;
      dto.givenName = orig.givenName;
      dto.middleName = orig.middleName;
      dto.familyName = orig.familyName;
      dto.suffix = orig.suffix;

      dto.displayName = orig.displayName;
      return dto;
   }
}
