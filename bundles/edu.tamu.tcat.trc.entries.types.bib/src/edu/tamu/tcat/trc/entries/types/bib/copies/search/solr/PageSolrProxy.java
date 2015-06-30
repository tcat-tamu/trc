package edu.tamu.tcat.trc.entries.types.bib.copies.search.solr;

import org.apache.solr.common.SolrInputDocument;

@Deprecated // legacy proxy used to wrap a SolrInputDocument and set values for the
            // purpose of creating or adding a document to the index.
            // Use PageTextDocument instead
public class PageSolrProxy
{

   private final static String ID = "id";
   private final static String TEXT = "pageText";
   private final static String NUMBER = "pageNumber";
   private final static String SEQUENCE = "pageSequence";

   private SolrInputDocument document;

   public SolrInputDocument getDocument()
   {
      return document;
   }

   public PageSolrProxy create()
   {
      document = new SolrInputDocument();
      return this;
   }

   public PageSolrProxy setId(String id)
   {
      document.addField(ID, id);
      return this;
   }

   public PageSolrProxy setPageText(String pageText)
   {
      document.addField(TEXT, pageText);
      return this;
   }

   public PageSolrProxy setPageNumber(String pageNum)
   {
      document.addField(NUMBER, pageNum);
      return this;
   }

   public PageSolrProxy setPageNumber(int pageNum)
   {
      document.addField(NUMBER, pageNum);
      return this;
   }

   public PageSolrProxy setPageSequence(String pageSequence)
   {
      document.addField(SEQUENCE, pageSequence);
      return this;
   }

   public PageSolrProxy setPageSequence(int pageSequence)
   {
      document.addField(SEQUENCE, pageSequence);
      return this;
   }

}
