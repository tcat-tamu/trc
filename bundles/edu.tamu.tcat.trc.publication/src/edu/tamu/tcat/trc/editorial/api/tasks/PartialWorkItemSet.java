package edu.tamu.tcat.trc.editorial.api.tasks;

import java.util.List;
import java.util.NoSuchElementException;

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
    * @return The number of work items in this partial set.
    */
   int size();

   /**
    * @return The index of the first item associated with this partial set.
    */
   int getStart();

   /**
    * @return The index of the last item in this partial set.
    */
   int getEnd();

   /**
    * @return The number of items contained in this partial set.
    */
   int getLimit();

   /**
    * @return The items in this partial set.
    */
   List<WorkItem> getItems();

   /**
    * @return <code>true</code> If this is not the final set of work items for the
    *    original query.
    */
   boolean hasNextSet();

   /**
    * @return The next partial set.
    * @throws NoSuchElementException If {@link #hasNextSet()} returns false.
    */
   PartialWorkItemSet getNext();



}
