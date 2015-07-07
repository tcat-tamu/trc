package edu.tamu.tcat.trc.entries.types.bib.copies.search.solr;

import org.apache.solr.common.SolrInputDocument;

import edu.tamu.tcat.trc.entries.types.bib.copies.search.PageSearchProxy;
import edu.tamu.tcat.trc.search.SearchException;
import edu.tamu.tcat.trc.search.solr.impl.TrcDocument;

public class PageTextDocument
{
   // composed instead of extended to not expose TrcDocument as API to this class
   private TrcDocument indexDocument;

   private PageSearchProxy proxy;

   public PageTextDocument()
   {
      indexDocument = new TrcDocument(new FullTextPageConfig());
   }

   public SolrInputDocument getDocument()
   {
      return indexDocument.getSolrDocument();
   }

   public String getText()
   {
      return proxy.pageText;
   }

   public static PageTextDocument create(String pgId, int seqNo, String text) throws SearchException
   {
      PageTextDocument doc = new PageTextDocument();

      doc.indexDocument.set(FullTextPageConfig.ID, pgId);
      doc.indexDocument.set(FullTextPageConfig.TEXT, text);
      doc.indexDocument.set(FullTextPageConfig.NUMBER, Integer.valueOf(seqNo));
      doc.indexDocument.set(FullTextPageConfig.SEQUENCE, Integer.toString(seqNo));

      PageSearchProxy proxy = new PageSearchProxy();
      proxy.id = pgId;
      proxy.pageNumber = Integer.toString(seqNo);
      proxy.pageSequence = Integer.toString(seqNo);
      proxy.pageText = text;

      doc.proxy = proxy;
      return doc;
   }
}
