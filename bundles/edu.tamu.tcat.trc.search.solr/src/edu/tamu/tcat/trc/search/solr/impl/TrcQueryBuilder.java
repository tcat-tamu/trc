/*
 * Copyright 2015 Texas A&M Engineering Experiment Station
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.tamu.tcat.trc.search.solr.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.StringJoiner;
import java.util.function.Function;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.SolrParams;

import edu.tamu.tcat.trc.search.SearchException;
import edu.tamu.tcat.trc.search.solr.SolrIndexConfig;
import edu.tamu.tcat.trc.search.solr.SolrIndexField;
import edu.tamu.tcat.trc.search.solr.SolrQueryBuilder;

/**
 * The main TRC implementation of a {@link SolrQueryBuilder}. This class also offers utilities
 * for handling {@link SolrIndexField} elements of SOLR documents.
 */
public class TrcQueryBuilder implements SolrQueryBuilder
{
   private SolrQuery params;
   private SolrIndexConfig cfg;

   public TrcQueryBuilder(SolrIndexConfig cfg) throws SearchException
   {
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

   public void facetLimit(int max)
   {
      if (max < 0)
         throw new IllegalArgumentException("Facet limit cannot be negative");
      params.setFacetLimit(max);
   }

   /**
    * Enables or disables hit-highlighting on the returned results. Note that this is
    * used to override default behavior of the underlying SOLR core.
    *
    * @param enable Indicates if hitHighlighting should be enabled.
    */
   public void hitHighlight(boolean enable)
   {
      params.set("hl", enable);
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
   List<T> unpack(SolrDocumentList docs, F searchProxyField)
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
            // TODO gather all errors and report rather than fail outright.
            throw new SearchException("Failed to parse search proxy: [" + workInfo + "]", e);
         }
      }
      return rv;
   }

   /**
   * Unpack data from each document in the provided collection using the supplied {@link Function}.
   *
   * @param <T> The type of data to be returned. This is a "search proxy" will be generated
   *            from data stored in each document.
   * @param docs The SOLR documents from which to unpack search proxies.
   * @param adapter A function that will be used to convert the result Solr document into a
   *             an instance of the type to be returned by the query.
   * @return
   * @throws SearchException
   */
  public <T> List<T> unpack(SolrDocumentList docs, Function<SolrDocument, T> adapter) throws SearchException
  {
     List<T> rv = new ArrayList<>();
     for (SolrDocument doc : docs)
     {
        String workInfo = null;
        try
        {
           rv.add(adapter.apply(doc));
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
   public <P> void filterMulti(SolrIndexField<P> param, Collection<P> values, String tag) throws SearchException
   {
      if (values == null || values.isEmpty())
      {
         throw new IllegalArgumentException("No filter values provided");
      }

      // add another 'fq' parameter with the new values
      StringJoiner joiner = new StringJoiner(" OR ");

      try
      {
         // TODO: should quoting of the value be done here or as part of the field definition?
         values.parallelStream()
               .map(value -> param.getName() + ":\"" + toSolrValue(param, value).replace("\\", "\\\\").replace("\"", "\\\"") + "\"")
               .forEach(joiner::add);

         params.add("fq", (tag == null || tag.isEmpty() ? "" : "{!tag=\"" + tag + "\"}") + joiner.toString());
      }
      catch (Exception e)
      {
         // threw runtime exception to use streams; send original
         if (e.getCause() instanceof SearchException)
         {
            throw (SearchException)e.getCause();
         }
         throw e;
      }
   }

   private static <P> String toSolrValue(SolrIndexField<P> param, P value)
   {
      try
      {
         return param.toSolrValue(value);
      }
      catch (Exception e)
      {
         throw new IllegalStateException("failed to convert value to Solr String value", e);
      }
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
