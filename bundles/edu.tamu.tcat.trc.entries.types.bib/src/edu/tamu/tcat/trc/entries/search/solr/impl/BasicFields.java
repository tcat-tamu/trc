package edu.tamu.tcat.trc.entries.search.solr.impl;

import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.tamu.tcat.trc.entries.search.SearchException;
import edu.tamu.tcat.trc.entries.search.solr.SolrIndexField;

public class BasicFields
{
   void f()
   {
      BasicFields.BasicDate<TemporalAccessor> pd = new BasicFields.BasicDate<>("date", TemporalAccessor.class);
      SolrIndexField<?> ph = pd;
      SolrIndexField<? extends TemporalAccessor> pta = pd;

      BasicFields.BasicDate<Year> pty = new BasicFields.BasicDate<>("date", Year.class);
      SolrIndexField<Year> pt = pty;
   }

   public static class BasicString implements SolrIndexField<String>
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

   public static class BasicDate<T extends TemporalAccessor> implements SolrIndexField<T>
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

   public static class SearchProxyField<T> implements SolrIndexField<T>
   {
      public final String name;
      public final Class<T> type;

      public SearchProxyField(String name, Class<T> type)
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
         try
         {
            return getMapper().writeValueAsString(value);
         }
         catch (Exception e)
         {
            throw new SearchException("Failed serializing value", e);
         }
      }

      // Allow subclass override
      protected ObjectMapper getMapper()
      {
         ObjectMapper mapper = new ObjectMapper();
         mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
         return mapper;
      }
   }
}
