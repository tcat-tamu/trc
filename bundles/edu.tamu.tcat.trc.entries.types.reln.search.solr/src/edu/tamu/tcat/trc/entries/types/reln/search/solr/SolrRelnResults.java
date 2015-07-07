package edu.tamu.tcat.trc.entries.types.reln.search.solr;

import java.util.List;

import edu.tamu.tcat.trc.entries.types.reln.search.RelationshipSearchResult;
import edu.tamu.tcat.trc.entries.types.reln.search.RelnSearchProxy;

public class SolrRelnResults implements RelationshipSearchResult
{
   private List<RelnSearchProxy> items;
   private RelationshipSolrQueryCommand cmd;

   SolrRelnResults(RelationshipSolrQueryCommand cmd, List<RelnSearchProxy> items)
   {
      this.cmd = cmd;
      this.items = items;
   }

   //HACK this is a degenerate impl for current puproses
   //TODO: what does this hack message mean? --pb
   @Override
   public List<RelnSearchProxy> get()
   {
      return items;
   }

   @Override
   public RelationshipSolrQueryCommand getCommand()
   {
      return cmd;
   }
}
