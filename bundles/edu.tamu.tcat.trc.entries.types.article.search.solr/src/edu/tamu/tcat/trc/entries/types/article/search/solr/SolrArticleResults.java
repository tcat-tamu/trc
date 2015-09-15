package edu.tamu.tcat.trc.entries.types.article.search.solr;

import java.util.List;

import edu.tamu.tcat.trc.entries.types.article.search.ArticleQueryCommand;
import edu.tamu.tcat.trc.entries.types.article.search.ArticleSearchProxy;
import edu.tamu.tcat.trc.entries.types.article.search.ArticleSearchResult;

public class SolrArticleResults implements ArticleSearchResult
{
   private ArticleQueryCommand cmd;
   private List<ArticleSearchProxy> articles;
   
   SolrArticleResults(ArticleQueryCommand cmd, List<ArticleSearchProxy> articles)
   {
      this.cmd = cmd;
      this.articles = articles;
      
   }

   @Override
   public ArticleQueryCommand getCommand()
   {
      return cmd;
   }

   @Override
   public List<ArticleSearchProxy> get()
   {
      return articles;
   }

}
