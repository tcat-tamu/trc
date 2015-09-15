package edu.tamu.tcat.trc.entries.types.article.search;

import java.util.List;

public interface ArticleSearchResult
{
   ArticleQueryCommand getCommand();
   
   List<ArticleSearchProxy> get();
}
