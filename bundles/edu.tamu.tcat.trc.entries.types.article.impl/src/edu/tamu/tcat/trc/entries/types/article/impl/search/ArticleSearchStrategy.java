package edu.tamu.tcat.trc.entries.types.article.impl.search;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.common.SolrInputDocument;

import edu.tamu.tcat.trc.SearchException;
import edu.tamu.tcat.trc.entries.types.article.Article;
import edu.tamu.tcat.trc.entries.types.article.search.ArticleQuery;
import edu.tamu.tcat.trc.entries.types.article.search.ArticleQueryCommand;
import edu.tamu.tcat.trc.search.solr.IndexServiceStrategy;
import edu.tamu.tcat.trc.search.solr.SolrIndexConfig;
import edu.tamu.tcat.trc.search.solr.impl.TrcQueryBuilder;

public class ArticleSearchStrategy implements IndexServiceStrategy<Article, ArticleQueryCommand>
{
   public static final String SOLR_CORE = "articles";


   private final ArticleSolrConfig searchCfg;

   public ArticleSearchStrategy()
   {
      searchCfg = new ArticleSolrConfig();
   }

   @Override
   public Class<Article> getType()
   {
      // TODO Auto-generated method stub
      return Article.class;
   }

   @Override
   public String getCoreId()
   {
      return SOLR_CORE;
   }

   @Override
   public SolrIndexConfig getIndexCofig()
   {
      return searchCfg;
   }

   @Override
   public SolrInputDocument getDocument(Article article)
   {
      return SearchAdapter.adapt(article);
   }

   @Override
   public String getEntryId(Article entry)
   {
      return entry.getId();
   }

   @Override
   public ArticleQueryCommand createQuery(SolrClient solr)
   {
      try
      {
         TrcQueryBuilder builder = new TrcQueryBuilder(getIndexCofig());
         return new ArticleSolrQueryCmd(solr, new ArticleQuery(), builder);
      }
      catch (SearchException ex)
      {
         throw new IllegalStateException("Failed to construct query builder", ex);
      }
   }

}
