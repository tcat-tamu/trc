package edu.tamu.tcat.trc.entries.types.article.search.solr;

import java.util.Collections;
import java.util.List;

import edu.tamu.tcat.trc.entries.types.article.search.ArticleQuery;
import edu.tamu.tcat.trc.entries.types.article.search.ArticleSearchProxy;
import edu.tamu.tcat.trc.entries.types.article.search.ArticleSearchResult;

/**
 * @since 1.1
 */
public class SolrArticleResults implements ArticleSearchResult
{
   private List<ArticleSearchProxy> articles;
   private ArticleQuery query;
   private long numMatched;

   public SolrArticleResults(ArticleQuery query, List<ArticleSearchProxy> articles, long numMatched)
   {
      this.query = new ArticleQuery(query);
      this.articles = articles;
      this.numMatched = numMatched;

   }

   @Override
   public ArticleQuery getQuery()
   {
      return query;
   }

   @Override
   public long getNumberMatched()
   {
      return numMatched;
   }

   @Override
   public List<ArticleSearchProxy> getResults()
   {
      return Collections.unmodifiableList(articles);
   }

}
