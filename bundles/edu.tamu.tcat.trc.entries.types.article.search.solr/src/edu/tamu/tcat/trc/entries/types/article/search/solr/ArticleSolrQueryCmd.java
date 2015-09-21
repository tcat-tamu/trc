package edu.tamu.tcat.trc.entries.types.article.search.solr;

import java.util.List;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;

import edu.tamu.tcat.trc.entries.types.article.search.ArticleQueryCommand;
import edu.tamu.tcat.trc.entries.types.article.search.ArticleSearchProxy;
import edu.tamu.tcat.trc.search.SearchException;
import edu.tamu.tcat.trc.search.solr.impl.TrcQueryBuilder;

public class ArticleSolrQueryCmd implements ArticleQueryCommand
{
   
   private static final int DEFAULT_MAX_RESULTS = 25;
   
   private final SolrServer solr;
   private final TrcQueryBuilder qb;
   
   public ArticleSolrQueryCmd(SolrServer solr, TrcQueryBuilder qb)
   {
      this.solr = solr;
      this.qb = qb;
      qb.max(DEFAULT_MAX_RESULTS);
   }
   
   @Override
   public SolrArticleResults execute() throws SearchException
   {
      try
      {
         QueryResponse response = solr.query(qb.get());
         SolrDocumentList results = response.getResults();
         
         List<ArticleSearchProxy> articles = qb.unpack(results, ArticleSolrConfig.SEARCH_PROXY);
         return new SolrArticleResults(this, articles);
      }
      catch (Exception e)
      {
         throw new SearchException("An error occurred while querying the article core: " + e, e);
      }
   }

   @Override
   public void query(String q) throws SearchException
   {
      qb.basic(q);
   }

   @Override
   public void queryAll() throws SearchException
   {
      qb.basic("*:*");
   }

   @Override
   public void setOffset(int start)
   {
      if (start < 0)
         throw new IllegalArgumentException("Offset ["+start+"] cannot be negative");

      qb.offset(start);
   }

   @Override
   public void setMaxResults(int max)
   {
      qb.max(max);
   }

}
