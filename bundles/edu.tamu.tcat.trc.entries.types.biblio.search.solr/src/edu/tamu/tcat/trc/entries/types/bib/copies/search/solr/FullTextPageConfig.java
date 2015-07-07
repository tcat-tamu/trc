package edu.tamu.tcat.trc.entries.types.bib.copies.search.solr;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.apache.solr.client.solrj.SolrQuery;

import edu.tamu.tcat.trc.entries.types.bib.copies.search.PageSearchProxy;
import edu.tamu.tcat.trc.search.SearchException;
import edu.tamu.tcat.trc.search.solr.SolrIndexConfig;
import edu.tamu.tcat.trc.search.solr.SolrIndexField;
import edu.tamu.tcat.trc.search.solr.impl.BasicFields;

/**
 * Defines the Solr configuration for indexing full text search results on a page level.
 */
public class FullTextPageConfig implements SolrIndexConfig
{

   public static final SolrIndexField<String> ID = new BasicFields.BasicString("id");
   public static final SolrIndexField<String> TEXT = new BasicFields.BasicString("pageText");
   public static final SolrIndexField<Integer> NUMBER = new BasicFields.BasicInteger("pageNumber");
   public static final SolrIndexField<String> SEQUENCE = new BasicFields.BasicString("pageSequence");


   public FullTextPageConfig()
   {
      // TODO Auto-generated constructor stub
   }

   @Override
   public void initialConfiguration(SolrQuery params) throws SearchException
   {
      /*
       * Using eDisMax seemed like a more adventagous way of doing the query. This will allow
       * additional solr Paramaters to be set in order to 'fine tune' the query.
       */
      params.set("defType", "edismax");
   }

   // TODO ulg -- can we configure some of this in a plugin?
   @Override
   public Class<PageSearchProxy> getSearchProxyType()
   {
      return PageSearchProxy.class;
   }

   @Override
   public Class<PageTextDocument> getIndexDocumentType()
   {
      return PageTextDocument.class;
   }

   @Override
   public Collection<? extends SolrIndexField<?>> getIndexedFields()
   {
      return Arrays.asList(ID, TEXT, NUMBER, SEQUENCE);
   }

   @Override
   public Collection<? extends SolrIndexField<?>> getStoredFields()
   {
      return Arrays.asList(ID, TEXT, NUMBER, SEQUENCE);
   }

   @Override
   public Collection<? extends SolrIndexField<?>> getMultiValuedFields()
   {
      return Collections.emptyList();
   }

}
