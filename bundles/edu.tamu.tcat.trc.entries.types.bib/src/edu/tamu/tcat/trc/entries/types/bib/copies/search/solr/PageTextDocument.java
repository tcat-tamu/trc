package edu.tamu.tcat.trc.entries.types.bib.copies.search.solr;

import org.apache.solr.common.SolrInputDocument;

import edu.tamu.tcat.trc.entries.search.solr.impl.TrcDocument;

public class PageTextDocument
{

   // composed instead of extended to not expose TrcDocument as API to this class
   private TrcDocument indexDocument;

   public PageTextDocument()
   {
      indexDocument = new TrcDocument(new FullTextPageConfig());
   }

   public SolrInputDocument getDocument()
   {
      return indexDocument.getSolrDocument();
   }
}
