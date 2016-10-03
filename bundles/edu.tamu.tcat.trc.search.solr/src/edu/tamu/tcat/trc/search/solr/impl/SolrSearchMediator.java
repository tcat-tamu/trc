package edu.tamu.tcat.trc.search.solr.impl;

import edu.tamu.tcat.trc.entries.core.repo.EntryUpdateRecord;
import edu.tamu.tcat.trc.search.solr.IndexService;

public class SolrSearchMediator
{
   public static <T> void index(IndexService<T> indexSvc, EntryUpdateRecord<T> ctx)
   {
      switch(ctx.getAction())
      {
         case CREATE:
            indexSvc.index(ctx.getModifiedState());
            break;
         case UPDATE:
            // TODO perform update
            indexSvc.index(ctx.getModifiedState());
            break;
         case REMOVE:
            indexSvc.remove(ctx.getEntryReference().id);
            break;
      }
   }
}
