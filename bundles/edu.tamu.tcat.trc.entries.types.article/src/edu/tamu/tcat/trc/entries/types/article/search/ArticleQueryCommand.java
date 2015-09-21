package edu.tamu.tcat.trc.entries.types.article.search;

import edu.tamu.tcat.trc.search.SearchException;

public interface ArticleQueryCommand
{
   ArticleSearchResult execute() throws SearchException;
   
   void query(String q) throws SearchException;
   
   void queryAll() throws SearchException;
   
   void setOffset(int start);
   
   void setMaxResults(int max);
   
}
