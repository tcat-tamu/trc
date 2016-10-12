package edu.tamu.tcat.trc.entries.types.article.search;

/**
 * Supports search based discovery of articles.
 *
 * @since 1.1
 */
public interface ArticleSearchService
{

   ArticleSearchResult findAll();

   ArticleSearchResult search(String query);

   ArticleQueryCommand createQuery();

   ArticleQueryCommand createQuery(ArticleQuery query);

   ArticleSearchResult next(ArticleQuery query);

   ArticleSearchResult previous(ArticleQuery query);

   ArticleSearchResult page(ArticleQuery query, int pg);

}
