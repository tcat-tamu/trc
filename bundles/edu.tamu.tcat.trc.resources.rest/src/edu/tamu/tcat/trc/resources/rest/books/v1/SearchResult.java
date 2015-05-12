package edu.tamu.tcat.trc.resources.rest.books.v1;

import java.util.Collection;

import edu.tamu.tcat.trc.resources.books.discovery.CopySearchResult;
import edu.tamu.tcat.trc.resources.books.discovery.DigitalCopyProxy;

public class SearchResult
{
   // Return q with proxy
   // { q: { },
   //   resutls: [ { these are the DigitalCopyProxy's} }
   public CopyQueryDTO query;
   public Collection<DigitalCopyProxy> copies;

   public SearchResult(CopySearchResult result, CopyQueryDTO query)
   {
      this.query = query;
      this.copies = result.asCollection();
   }

}
