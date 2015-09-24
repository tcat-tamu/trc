package edu.tamu.tcat.trc.entries.types.article.search;

import edu.tamu.tcat.trc.search.SearchException;

/**
 *  Supports search based discovery of articles.
 *
 */
public interface ArticleSearchService
{

   ArticleSearchResult findAll() throws SearchException;

   ArticleSearchResult search(String query) throws SearchException;

   ArticleQueryCommand createQuery() throws SearchException;

   ArticleQueryCommand createQuery(ArticleQuery query) throws SearchException;

   ArticleSearchResult next(ArticleQuery query) throws SearchException;

   ArticleSearchResult previous(ArticleQuery query) throws SearchException;

   ArticleSearchResult page(ArticleQuery query, int pg) throws SearchException;

}
