package edu.tamu.tcat.trc.entries.bib.search;

import java.util.List;

public interface SearchWorksResult
{

   // NOTES
   //     add support for retrieving facet information
   //     provide paging support (next/previous)
   //
   /**
    * @return Proxies for the works that match the current search.
    */
   List<WorkSearchProxy> listItems();
}
