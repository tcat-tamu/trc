package edu.tamu.tcat.trc.search.solr;

import edu.tamu.tcat.trc.repo.UpdateContext;

public class SolrSearchAdapter
{
   public static <T> void index(IndexService<T> indexSvc, UpdateContext<T> ctx)
   {
      switch(ctx.getActionType())
      {
         case CREATE:
            indexSvc.index(ctx.getModified());
            break;
         case EDIT:
            indexSvc.index(ctx.getModified());
            break;
         case REMOVE:
            indexSvc.remove(ctx.getId());
            break;
      }
   }
}
