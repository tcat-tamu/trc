package edu.tamu.tcat.trc.entries.types.article.search;

import edu.tamu.tcat.trc.search.SearchException;

public interface ArticleSearchService
{
   ArticleQueryCommand createQueryCmd() throws SearchException;
}
