package edu.tamu.tcat.trc.entries.types.article.impl.search;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;

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

   // FIXME make concrete type
   private Map<String, Map<String, List<String>>> highlighting;
   private List<FacetValueList> facets;

   public SolrArticleResults(ArticleQuery query,
                             List<ArticleSearchProxy> articles,
                             Map<String, Map<String, List<String>>> highlighting,
                             List<FacetValueList> facets,
                             long numMatched)
   {
      this.query = new ArticleQuery(query);
      this.articles = articles;
      this.highlighting = highlighting;
      this.facets = facets;
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

   @Override
   public Map<String, Map<String, List<String>>> getHits()
   {
      return highlighting;
   }

   @Override
   public List<FacetValueList> getFacets()
   {
      return facets;
   }

   // HACK TEMP inner classes pending refactor to correct location.
   public static FacetValueList adapt(FacetField field)
   {
      FacetValueList result = new FacetValueList();
      result.name = field.getName();
      result.count = field.getValueCount();

      result.items = field.getValues().stream()
            .map(SolrArticleResults::adapt)
            .collect(Collectors.toList());

      return result;
   }

   public static FacetValue adapt(Count count)
   {
      FacetValue value = new FacetValue();
      value.filter = count.getAsFilterQuery();
      value.name = count.getName();
      value.count = count.getCount();

      return value;
   }


}
