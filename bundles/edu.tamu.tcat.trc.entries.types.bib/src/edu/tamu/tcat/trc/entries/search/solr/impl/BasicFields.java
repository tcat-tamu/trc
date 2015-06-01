package edu.tamu.tcat.trc.entries.search.solr.impl;

import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Objects;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.tamu.tcat.trc.entries.search.SearchException;
import edu.tamu.tcat.trc.entries.search.solr.SolrIndexField;

public class BasicFields
{
   protected static abstract class FieldBase<T> implements SolrIndexField<T>
   {
      public final String name;
      public final Class<T> type;

      public FieldBase(String name, Class<T> type)
      {
         this.name = Objects.requireNonNull(name, "Missing name");
         this.type = Objects.requireNonNull(type, "Missing type");
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
      public boolean equals(Object obj)
      {
         if (obj instanceof FieldBase<?>)
         {
            FieldBase<?> fb = (FieldBase<?>)obj;
            return Objects.equals(name, fb.name) &&
                   Objects.equals(type, fb.type);
         }
         return false;
      }

      @Override
      public int hashCode()
      {
         return name.hashCode();
      }
   }

   public static class BasicString extends FieldBase<String>
   {
      public BasicString(String name)
      {
         super(name, String.class);
      }

      @Override
      public String toSolrValue(String value) throws SearchException
      {
         return value;
      }
   }

   public static class BasicDate<T extends TemporalAccessor> extends FieldBase<T>
   {
      public BasicDate(String name, Class<T> type)
      {
         super(name, type);
      }

      @Override
      public String toSolrValue(T value) throws SearchException
      {
         String out = DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(value);
         return out;
      }
   }

   public static class SearchProxyField<T> extends FieldBase<T>
   {
      public SearchProxyField(String name, Class<T> type)
      {
         super(name, type);
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
