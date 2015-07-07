package edu.tamu.tcat.trc.entries.types.bib.search.solr;

import java.util.List;

import edu.tamu.tcat.trc.entries.types.biblio.search.BiblioSearchProxy;
import edu.tamu.tcat.trc.entries.types.biblio.search.SearchWorksResult;
import edu.tamu.tcat.trc.entries.types.biblio.search.WorkQueryCommand;

public class SolrWorksResults implements SearchWorksResult
{
   private List<BiblioSearchProxy> items;
   private WorkSolrQueryCommand cmd;

   SolrWorksResults(WorkSolrQueryCommand cmd, List<BiblioSearchProxy> items)
   {
      this.cmd = cmd;
      this.items = items;
   }

   //HACK this is a degenerate impl for current puproses
   //TODO: what does this hack message mean? --pb
   @Override
   public List<BiblioSearchProxy> get()
   {
      return items;
   }

   @Override
   public WorkQueryCommand getCommand()
   {
      return cmd;
   }
}
