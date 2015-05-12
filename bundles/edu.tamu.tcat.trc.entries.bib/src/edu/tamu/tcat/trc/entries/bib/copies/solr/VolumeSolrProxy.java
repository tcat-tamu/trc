package edu.tamu.tcat.trc.entries.bib.copies.solr;

import org.apache.solr.common.SolrInputDocument;

public class VolumeSolrProxy
{
   private final static String ID = "id";
   private final static String TEXT = "volumeText";
   private final static String ASSOCIATED_ENTRY = "associatedEntry";

   private SolrInputDocument document;

   public SolrInputDocument getDocument()
   {
      return document;
   }

   public VolumeSolrProxy create()
   {
      document = new SolrInputDocument();
      return this;
   }

   public VolumeSolrProxy setId(String id)
   {
      document.addField(ID, id);
      return this;
   }

   public VolumeSolrProxy setId(int id)
   {
      document.addField(ID, id);
      return this;
   }

   public VolumeSolrProxy setVolumeText(String volText)
   {
      document.addField(TEXT, volText);
      return this;
   }

   public VolumeSolrProxy setAssociatedEntry(String entry)
   {
      document.addField(ASSOCIATED_ENTRY, entry);
      return this;
   }

}