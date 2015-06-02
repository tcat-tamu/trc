package edu.tamu.tcat.trc.entries.types.bib.search.solr;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collection;

import org.apache.solr.client.solrj.SolrQuery;

import edu.tamu.tcat.trc.entries.search.solr.SolrIndexConfig;
import edu.tamu.tcat.trc.entries.search.solr.SolrIndexField;
import edu.tamu.tcat.trc.entries.search.solr.impl.BasicFields;
import edu.tamu.tcat.trc.entries.types.bib.search.BiblioSearchProxy;

public class BiblioSolrConfig implements SolrIndexConfig
{
   public static final SolrIndexField<String> ID = new BasicFields.BasicString("id");
   public static final BasicFields.SearchProxyField<BiblioSearchProxy> SEARCH_PROXY = new BasicFields.SearchProxyField<BiblioSearchProxy>("workInfo", BiblioSearchProxy.class);
   public static final SolrIndexField<String> AUTHOR_NAMES = new BasicFields.BasicString("authorNames");
   public static final SolrIndexField<String> TITLES = new BasicFields.BasicString("titles");
   // Using LocalDate for yyyy-MM-dd
   public static final SolrIndexField<LocalDate> PUBLICATION_DATE = new BasicFields.BasicDate<LocalDate>("publishDateValue", LocalDate.class);

   //TODO: add additional fields
//   private final static String authorIds = "authorIds";
//   private final static String authorRoles = "authorRole";           // not needed
//   private final static String titleTypes = "titleTypes";            // not needed
//   private final static String language = "lang";                    // not needed
//   private final static String subtitles = "subtitles";              // needed? could be joined with titles unless we want to boost separately
//   private final static String publisher = "publisher";
//   private final static String pubLocation = "publisherLocation";
//   private final static String pubDateString = "publishDateString";  // simply date, expressed as a YYYY-MM-DD
//   private final static String docSeries = "series";
//   private final static String docSummary = "summary";               // NOTE - critical search field

//   private final static String editionId = "editionId";
//   private final static String editionName = "editionName";          // not needed (store in info)

//   private final static String volumeId = "volumeId";
//   private final static String volumeNumber = "volumeNumber";        // not needed (store in info)

   @Override
   public void initialConfiguration(SolrQuery params)
   {
      /*
       * Using eDisMax seemed like a more adventagous way of doing the query. This will allow
       * additional solr Paramaters to be set in order to 'fine tune' the query.
       */
      params.set("defType", "edismax");
   }

   @Override
   public void configureBasic(String q, SolrQuery params)
   {
      //HACK: if no query specified, should this throw and require a call to queryAll() ?
      if (q == null || q.trim().isEmpty())
         q = "*:*";

      // NOTE query against all fields, boosted appropriately, free text
      //      I think that means *:(qBasic)
      // NOTE in general, if this is applied, the other query params are unlikely to be applied
      StringBuilder qBuilder = new StringBuilder(q);

      // Avoid searching over editions and volumes, only for "basic" search
      qBuilder.append(" -editionName:(*)")
              .append(" -volumeNumber:(*)");

      params.set("q", qBuilder.toString());

      // Basic query only searches over these fields
      params.set("qf", "titles^3 authorNames authorIds");
   }

   @Override
   public Class<BiblioSearchProxy> getSearchProxyType()
   {
      return BiblioSearchProxy.class;
   }

   @Override
   public Class<BiblioDocument> getIndexDocumentType()
   {
      return BiblioDocument.class;
   }

   @Override
   public Collection<? extends SolrIndexField<?>> getIndexedFields()
   {
      return Arrays.asList(ID,
                           AUTHOR_NAMES,
                           TITLES,
                           PUBLICATION_DATE);
   }

   @Override
   public Collection<? extends SolrIndexField<?>> getStoredFields()
   {
      return Arrays.asList(ID,
                           SEARCH_PROXY,
                           AUTHOR_NAMES,
                           TITLES,
                           PUBLICATION_DATE);
   }

   @Override
   public Collection<? extends SolrIndexField<?>> getMultiValuedFields()
   {
      return Arrays.asList(AUTHOR_NAMES,
                           TITLES);
   }
}
