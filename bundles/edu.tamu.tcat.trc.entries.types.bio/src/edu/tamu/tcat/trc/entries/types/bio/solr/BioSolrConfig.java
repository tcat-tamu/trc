package edu.tamu.tcat.trc.entries.types.bio.solr;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collection;

import org.apache.solr.client.solrj.SolrQuery;

import edu.tamu.tcat.trc.entries.search.solr.SolrIndexConfig;
import edu.tamu.tcat.trc.entries.search.solr.SolrIndexField;
import edu.tamu.tcat.trc.entries.search.solr.impl.BasicFields;
import edu.tamu.tcat.trc.entries.types.bio.search.BioSearchProxy;

public class BioSolrConfig implements SolrIndexConfig
{
   public static final SolrIndexField<String> ID = new BasicFields.BasicString("id");
   public static final SolrIndexField<BioSearchProxy> SEARCH_PROXY = new BasicFields.SearchProxyField<BioSearchProxy>("personInfo", BioSearchProxy.class);
   public static final SolrIndexField<String> SYNTHETIC_NAME = new BasicFields.BasicString("syntheticName");
   public static final SolrIndexField<String> FAMILY_NAME = new BasicFields.BasicString("familyName");
   public static final SolrIndexField<String> DISPLAY_NAME = new BasicFields.BasicString("displayName");
   // Using LocalDate for yyyy-MM-dd
   public static final SolrIndexField<LocalDate> BIRTH_DATE = new BasicFields.BasicDate<LocalDate>("birthDate", LocalDate.class);
   public static final SolrIndexField<String> BIRTH_LOCATION = new BasicFields.BasicString("birthLocation");
   public static final SolrIndexField<LocalDate> DEATH_DATE = new BasicFields.BasicDate<LocalDate>("deathDate", LocalDate.class);
   public static final SolrIndexField<String> DEATH_LOCATION = new BasicFields.BasicString("deathLocation");
   public static final SolrIndexField<String> SUMMARY = new BasicFields.BasicString("summary");

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

      params.set("q", qBuilder.toString());

      // Basic query only searches over these fields
      params.set("qf", "syntheticName");
   }

   @Override
   public Class<BioSearchProxy> getSearchProxyType()
   {
      return BioSearchProxy.class;
   }

   @Override
   public Class<BioDocument> getIndexDocumentType()
   {
      return BioDocument.class;
   }

   @Override
   public Collection<? extends SolrIndexField<?>> getIndexedFields()
   {
      return Arrays.asList(ID,
                           SYNTHETIC_NAME,
                           FAMILY_NAME,
                           DISPLAY_NAME,
                           DEATH_LOCATION,
                           BIRTH_DATE,
                           BIRTH_LOCATION,
                           DEATH_DATE);
   }

   @Override
   public Collection<? extends SolrIndexField<?>> getStoredFields()
   {
      return Arrays.asList(ID,
                           SEARCH_PROXY,
                           SYNTHETIC_NAME,
                           FAMILY_NAME,
                           DISPLAY_NAME,
                           BIRTH_LOCATION,
                           BIRTH_DATE,
                           DEATH_LOCATION,
                           DEATH_DATE);
   }

   @Override
   public Collection<? extends SolrIndexField<?>> getMultiValuedFields()
   {
      return Arrays.asList(FAMILY_NAME,
                           DISPLAY_NAME);
   }
}
