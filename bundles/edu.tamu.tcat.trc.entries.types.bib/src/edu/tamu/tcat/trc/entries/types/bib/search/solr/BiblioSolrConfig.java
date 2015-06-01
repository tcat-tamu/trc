package edu.tamu.tcat.trc.entries.types.bib.search.solr;

import java.time.Year;

import org.apache.solr.client.solrj.SolrQuery;

import edu.tamu.tcat.trc.entries.search.solr.SolrQueryBuilder;
import edu.tamu.tcat.trc.entries.search.solr.SolrQueryConfig;
import edu.tamu.tcat.trc.entries.search.solr.impl.BasicProperties;
import edu.tamu.tcat.trc.entries.types.bib.search.WorkSearchProxy;

public class BiblioSolrConfig implements SolrQueryConfig
{
   public static final SolrQueryBuilder.Parameter<String> AUTHOR_NAME = new BasicProperties.BasicString("authorNames");
   public static final SolrQueryBuilder.Parameter<Year> PUBLICATION_DATE = new BasicProperties.BasicDate<Year>("publishDateValue", Year.class);

   public BiblioSolrConfig()
   {
      // TODO Auto-generated constructor stub
   }

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
      qBuilder.append(" -editionName:(*)")
              .append(" -volumeNumber:(*)");

      params.set("q", qBuilder.toString());

      params.set("qf", "titles^3 authorNames authorIds");
   }

   @Override
   public Class<WorkSearchProxy> getSearchProxyType()
   {
      return WorkSearchProxy.class;
   }

   @Override
   public Class<BiblioDocument> getIndexDocumentType()
   {
      return BiblioDocument.class;
   }
}
