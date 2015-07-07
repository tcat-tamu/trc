package edu.tamu.tcat.trc.entries.types.bib.copies.search.solr;

import org.apache.solr.common.SolrInputDocument;

import edu.tamu.tcat.trc.entries.search.SearchException;
import edu.tamu.tcat.trc.entries.search.solr.impl.TrcDocument;

public class VolumeTextDocument
{
   // composed instead of extended to not expose TrcDocument as API to this class
   private TrcDocument indexDocument;

   public VolumeTextDocument()
   {
      indexDocument = new TrcDocument(new FullTextPageConfig());
   }

   public SolrInputDocument getDocument()
   {
      return indexDocument.getSolrDocument();
   }

   public static VolumeTextDocument create(String volId, String assocEntry, String text) throws SearchException
   {
      VolumeTextDocument doc = new VolumeTextDocument();

      doc.indexDocument.set(FullTextVolumeConfig.ID, volId);
      doc.indexDocument.set(FullTextVolumeConfig.TEXT, text);
      doc.indexDocument.set(FullTextVolumeConfig.ASSOC_ENTRY, assocEntry);

      return doc;
   }
}
