package edu.tamu.tcat.trc.entries.search.solr.impl;

import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;

import edu.tamu.tcat.trc.entries.search.SearchException;
import edu.tamu.tcat.trc.entries.search.solr.SolrQueryBuilder;

public class BasicProperties
{
   public static class BasicString implements SolrQueryBuilder.Parameter<String>
   {
      public final String name;

      public BasicString(String name)
      {
         this.name = name;
      }

      @Override
      public String toString()
      {
         return name;
      }

      @Override
      public String getName()
      {
         return name;
      }

      @Override
      public Class<String> getType()
      {
         return String.class;
      }

      @Override
      public String toSolrValue(String value) throws SearchException
      {
         return value;
      }
   }

   public static class BasicDate<T extends TemporalAccessor> implements SolrQueryBuilder.Parameter<T>
   {
      public final String name;
      public final Class<T> type;

      public BasicDate(String name, Class<T> type)
      {
         this.name = name;
         this.type = type;
      }

      @Override
      public String toString()
      {
         return name;
      }

      @Override
      public String getName()
      {
         return name;
      }

      @Override
      public Class<T> getType()
      {
         return type;
      }

      @Override
      public String toSolrValue(T value) throws SearchException
      {
         String out = DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(value);
         return out;
      }
   }
}
