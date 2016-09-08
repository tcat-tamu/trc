package edu.tamu.tcat.trc.search.solr;

public interface IndexService<T>
{
   boolean isIndexed(T instance);

   void index(T instance);

   void remove(T instance);

   void remove(String... id);

}
