package edu.tamu.tcat.trc.entries.search.solr.impl;

import java.util.Collection;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.common.params.SolrParams;

import edu.tamu.tcat.trc.entries.search.SearchException;
import edu.tamu.tcat.trc.entries.search.solr.SolrQueryBuilder;
import edu.tamu.tcat.trc.entries.search.solr.SolrQueryConfig;

//NOTE: Should have nothing "works"-specific in this builder
public class TrcQueryBuilder implements SolrQueryBuilder
{
   private SolrServer solr;
   private SolrQuery params;
   private SolrQueryConfig cfg;

   public TrcQueryBuilder(SolrServer solr, SolrQueryConfig cfg) throws SearchException
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
   public <P> void query(SolrQueryBuilder.Parameter<P> param, P value) throws SearchException
   {
      params.set(param.getName(), param.toSolrValue(value));
   }

   @Override
   public <P> void filter(SolrQueryBuilder.Parameter<P> param, Collection<P> values)
   {
      // TODO Auto-generated method stub

   }
}
