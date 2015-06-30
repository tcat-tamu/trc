package edu.tamu.tcat.trc.entries.types.bib.copies.search;

/**
 * JSON serializable proxy for a page of full-text results, intended to be return to the
 * client for use to display results for a search hit.
 */
public class PageSearchProxy
{

   // TODO provide hit hightlighing (if possible)

   public String id;
   public String pageText;
   public String pageNumber;
   public String pageSequence;

   public PageSearchProxy()
   {
      // TODO Auto-generated constructor stub
   }

}
