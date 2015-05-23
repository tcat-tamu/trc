package edu.tamu.tcat.trc.entries.types.bib.search.solr;

import java.util.List;

import edu.tamu.tcat.trc.entries.types.bib.search.SearchWorksResult;
import edu.tamu.tcat.trc.entries.types.bib.search.WorkSearchProxy;

public class SolrWorksResults implements SearchWorksResult
{
   private List<WorkSearchProxy> items;

   SolrWorksResults(List<WorkSearchProxy> items)
   {
      this.items = items;

   }

   // HACK this is a degenerate impl for current puproses
   @Override
   public List<WorkSearchProxy> listItems()
   {
      return items;
   }

}
