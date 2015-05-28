package edu.tamu.tcat.trc.entries.types.bib.rest.v1;

import java.util.ArrayList;
import java.util.List;

import edu.tamu.tcat.trc.entries.types.bib.dto.AuthorRefDV;
import edu.tamu.tcat.trc.entries.types.bib.search.WorkSearchProxy;

/**
 * An encapsulation of adapter methods to convert between the search API and
 * the {@link RestApiV1} schema DTOs.
 */
public class SearchAdapter
{
   public static List<RestApiV1.WorkSearchResult> toDTO(List<WorkSearchProxy> origList)
   {
      if (origList == null)
         return null;

      List<RestApiV1.WorkSearchResult> dtoList = new ArrayList<>();
      for (WorkSearchProxy orig : origList)
      {
         RestApiV1.WorkSearchResult dto = new RestApiV1.WorkSearchResult();
         dto.id = orig.id;
         dto.label = orig.label;
         dto.title = orig.title;
         dto.uri = orig.uri;
         dto.pubYear = orig.pubYear;
         dto.summary = orig.summary;
         if (orig.authors != null)
         {
            dto.authors = new ArrayList<>();
            for (AuthorRefDV auth : orig.authors)
               dto.authors.add(RepoAdapter.toDTO(auth));
         }

         dtoList.add(dto);
      }

      return dtoList;
   }
}
