package edu.tamu.tcat.trc.entries.search.solr.impl;

import org.apache.solr.common.SolrInputDocument;

import edu.tamu.tcat.trc.entries.search.SearchException;
import edu.tamu.tcat.trc.entries.search.solr.SolrIndexField;

public class TrcDocument
{
   protected final SolrInputDocument document;

   public TrcDocument()
   {
      document = new SolrInputDocument();
   }

   public SolrInputDocument getSolrDocument()
   {
      return document;
   }
}
