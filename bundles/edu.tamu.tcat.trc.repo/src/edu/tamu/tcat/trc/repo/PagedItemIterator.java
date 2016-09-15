package edu.tamu.tcat.trc.repo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Future;
import java.util.function.Function;

/**
 * Provides an iterator over a set of paged data.
 *
 * @param <T>
 */
public class PagedItemIterator<T> implements Iterator<T>
{
   // TODO this is a speculative implementation designed to support paged general access to paged
   //     results. It is currently unused.

   public static interface JsonPageResolver
   {
      Future<List<String>> resolve(int offset, int size);
   }

   private List<String> currentPage;
   private int currentIndex = 0;

   private int offset = 0;
   private int pageSize = 100;
   private volatile Future<List<String>> nextBlock = null;

   private final Function<String, T> parser;
   private final PagedItemIterator.JsonPageResolver pageResolver;

   public PagedItemIterator(PagedItemIterator.JsonPageResolver pageResolver, Function<String, T> parser, int pageSize)
   {
      this.pageResolver = pageResolver;
      this.parser = parser;
      this.pageSize = pageSize;

      // init to empty list and start next page load
      currentPage = new ArrayList<>();
      nextBlock = pageResolver.resolve(offset, pageSize);
   }

   @Override
   public boolean hasNext()
   {
      return (currentIndex < currentPage.size() - 1) ? true : loadNextPage();
   }

   @Override
   public T next()
   {
      String json = "";
      synchronized (this)
      {
         currentIndex++;
         json = currentPage.get(currentIndex);
      }

      return parser.apply(json);
   }

   private synchronized boolean loadNextPage()
   {
      currentIndex = -1;
      currentPage = null;

      if (nextBlock == null)
         return false;

      try
      {
         // TODO add time delay
         currentPage = nextBlock.get();
      }
      catch (Exception ex)
      {
         throw new IllegalStateException("Failed to load next page", ex);
      }

      if (currentPage.isEmpty())
         return false;

      // start next page loading
      offset = offset + currentPage.size();
      nextBlock = pageResolver.resolve(offset, pageSize);
      return true;
   }
}