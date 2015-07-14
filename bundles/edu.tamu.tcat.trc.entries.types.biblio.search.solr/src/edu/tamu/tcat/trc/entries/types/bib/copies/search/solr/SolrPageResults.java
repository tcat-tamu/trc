package edu.tamu.tcat.trc.entries.types.bib.copies.search.solr;

import java.util.List;

import edu.tamu.tcat.trc.entries.types.biblio.copies.search.PageSearchCommand;
import edu.tamu.tcat.trc.entries.types.biblio.copies.search.PageSearchProxy;
import edu.tamu.tcat.trc.entries.types.biblio.copies.search.PageSearchResult;

public class SolrPageResults implements PageSearchResult
{
   private PageSolrSearchCommand cmd;
   private List<PageSearchProxy> page;

   public SolrPageResults(PageSolrSearchCommand cmd, List<PageSearchProxy> page)
   {
      this.cmd = cmd;
      this.page = page;
   }

   @Override
   public PageSearchCommand getCommand()
   {
      return cmd;
   }

   @Override
   public List<PageSearchProxy> get()
   {
      return page;
   }

}
