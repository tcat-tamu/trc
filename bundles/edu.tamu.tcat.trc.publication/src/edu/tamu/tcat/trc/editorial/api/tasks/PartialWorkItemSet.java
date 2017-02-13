package edu.tamu.tcat.trc.editorial.api.tasks;

import java.util.List;

/**
 *  Represents a partial set of of items returned in response to a query.
 */
public interface PartialWorkItemSet
{

   /**
    * @return The total number of items in the workset.
    */
   int getTotalMatched();

   /**
    * @return The index of the first item associated with this partial set.
    */
   int getStart();

   /**
    * @return The number of items contained in this partial set.
    */
   int getLimit();

   /**
    * @return The items in this partial set.
    */
   List<WorkItem> getItems();

   /**
    * @return The next partial set.
    */
   PartialWorkItemSet getNext();

}
