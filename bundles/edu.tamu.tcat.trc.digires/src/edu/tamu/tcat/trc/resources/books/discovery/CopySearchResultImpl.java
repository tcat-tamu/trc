package edu.tamu.tcat.trc.resources.books.discovery;

import java.util.Collection;

public class CopySearchResultImpl implements CopySearchResult
{
   private Collection<DigitalCopyProxy> copies;

   public CopySearchResultImpl(Collection<DigitalCopyProxy> copies)
   {
      this.copies = copies;

   }

   @Override
   public Collection<DigitalCopyProxy> asCollection()
   {
      return copies;
   }

   @Override
   public CopySearchResult getNextPage()
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public CopySearchResult getPrevPage()
   {
      // TODO Auto-generated method stub
      return null;
   }

}
