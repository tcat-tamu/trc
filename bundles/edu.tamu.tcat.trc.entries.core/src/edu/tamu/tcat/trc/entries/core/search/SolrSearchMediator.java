package edu.tamu.tcat.trc.entries.core.search;

import edu.tamu.tcat.trc.entries.core.repo.EntryUpdateRecord;
import edu.tamu.tcat.trc.search.solr.IndexService;

public class SolrSearchMediator
{
   public static <T> void index(IndexService<T, ?> indexSvc, EntryUpdateRecord<T> ctx)
   {
      switch(ctx.getAction())
      {
         case CREATE:
            indexSvc.index(ctx.getModifiedState());
            break;
         case UPDATE:
            indexSvc.index(ctx.getModifiedState());
            break;
         case REMOVE:
            indexSvc.remove(ctx.getEntryReference().id);
            break;
      }
   }
}
