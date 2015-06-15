package edu.tamu.tcat.trc.entries.search.solr.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.SolrParams;

import edu.tamu.tcat.trc.entries.search.SearchException;
import edu.tamu.tcat.trc.entries.search.solr.SolrIndexConfig;
import edu.tamu.tcat.trc.entries.search.solr.SolrIndexField;
import edu.tamu.tcat.trc.entries.search.solr.SolrQueryBuilder;

/**
 * The main TRC implementation of a {@link SolrQueryBuilder}. This class also offers utilities
 * for handling {@link SolrIndexField} elements of SOLR documents.
 */
public class TrcQueryBuilder implements SolrQueryBuilder
{
   private SolrServer solr;
   private SolrQuery params;
   private SolrIndexConfig cfg;

   public TrcQueryBuilder(SolrServer solr, SolrIndexConfig cfg) throws SearchException
   {
      this.solr = solr;
      this.cfg = cfg;
      params = new SolrQuery();

      cfg.initialConfiguration(params);
   }

   @Override
   public SolrParams get()
   {
      return params;
   }

   @Override
   public void offset(int offset)
   {
      if (offset < 0)
         throw new IllegalArgumentException("Offset cannot be negative");
      params.set("start", offset);
   }

   @Override
   public void max(int max)
   {
      if (max < 0)
         throw new IllegalArgumentException("Rows cannot be negative");
      params.set("rows", max);
   }

   /**
    * Unpack data of the given {@link BasicFields.SearchProxyField SearchProxyField} type from each
    * document in the provided collection. This involves deserializing
    * the JSON literal stored in the SOLR document into the field's type.
    *
    * @param <T> The type of data to be returned. This is a "search proxy" stored in each
    *            document.
    * @param <F> The type of {@link BasicFields.SearchProxyField SearchProxyField}, supplied for the <T> argument.
    * @param docs The SOLR documents from which to unpack search proxies.
    * @param searchProxyField The field representing the search proxy data in the documents,
    *             typically statically referenced from a {@link SolrIndexConfig} implementation
    *             for the module associated with the SOLR documents and core.
    * @return
    * @throws SearchException
    */
   public <T,F extends BasicFields.SearchProxyField<T>>
   List<T>
   unpack(SolrDocumentList docs,
          F searchProxyField)
   throws SearchException
   {
      List<T> rv = new ArrayList<>();
      for (SolrDocument doc : docs)
      {
         String workInfo = null;
         try
         {
            workInfo = doc.getFieldValue(searchProxyField.getName()).toString();
            T wi = searchProxyField.parse(workInfo);
            rv.add(wi);
         }
         catch (Exception e)
         {
            throw new SearchException("Failed to parse search proxy: [" + workInfo + "]", e);
         }
      }
      return rv;
   }

   @Override
   public void basic(String q) throws SearchException
   {
      /*
       * Because "basic" configuration could parse parameters, specify ignored fields,
       * and set up other things, it is a delegated invocation
       */
      cfg.configureBasic(q, params);
   }

   @Override
   public <P> void query(SolrIndexField<P> param, P value) throws SearchException
   {
      // Append existing 'q' to new query field:value
      String q = params.get("q", "") + " " + param.getName() + ":" + param.toSolrValue(value);
      // overwrite existing 'q'
      params.set("q", q.trim());
   }

   @Override
   public <P> void queryRangeExclusive(SolrIndexField<P> param, P start, P end, boolean excludeStart, boolean excludeEnd) throws SearchException
   {
      StringBuilder sb = new StringBuilder();
      if (excludeStart)
         sb.append("{");
      else
         sb.append("[");
      sb.append(param.toSolrValue(start))
        .append(" TO ")
        .append(param.toSolrValue(end));
      if (excludeEnd)
         sb.append("}");
      else
         sb.append("]");

      // Append existing 'q' to new query field:value
      String q = params.get("q", "") + " " + param.getName() + ":" + sb.toString();
      // overwrite existing 'q'
      params.set("q", q.trim());
   }

   @Override
   public <P> void filter(SolrIndexField<P> param, P value) throws SearchException
   {
      // add another 'fq' parameter with the new value
      params.add("fq", param.getName() + ":" + param.toSolrValue(value));
   }

   @Override
   public <P> void filterRangeExclusive(SolrIndexField<P> param, P start, P end, boolean excludeStart, boolean excludeEnd) throws SearchException
   {
      StringBuilder sb = new StringBuilder();
      if (excludeStart)
         sb.append("{");
      else
         sb.append("[");
      sb.append(param.toSolrValue(start))
        .append(" TO ")
        .append(param.toSolrValue(end));
      if (excludeEnd)
         sb.append("}");
      else
         sb.append("]");

      // add another 'fq' parameter with this value
      params.add("fq", param.getName() + ":" + sb.toString());
   }
}