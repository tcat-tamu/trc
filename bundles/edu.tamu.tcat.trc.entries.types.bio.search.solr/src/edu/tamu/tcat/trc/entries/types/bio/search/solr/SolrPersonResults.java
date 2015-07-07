package edu.tamu.tcat.trc.entries.types.bio.search.solr;

import java.util.List;

import edu.tamu.tcat.trc.entries.types.bio.search.BioSearchProxy;
import edu.tamu.tcat.trc.entries.types.bio.search.PeopleQueryCommand;
import edu.tamu.tcat.trc.entries.types.bio.search.PersonSearchResult;

public class SolrPersonResults implements PersonSearchResult
{
   private List<BioSearchProxy> items;
   private PeopleSolrQueryCommand cmd;

   SolrPersonResults(PeopleSolrQueryCommand cmd, List<BioSearchProxy> items)
   {
      this.cmd = cmd;
      this.items = items;
   }

   //HACK this is a degenerate impl for current puproses
   //TODO: what does this hack message mean? --pb
   @Override
   public List<BioSearchProxy> get()
   {
      return items;
   }

   @Override
   public PeopleQueryCommand getCommand()
   {
      return cmd;
   }
}
