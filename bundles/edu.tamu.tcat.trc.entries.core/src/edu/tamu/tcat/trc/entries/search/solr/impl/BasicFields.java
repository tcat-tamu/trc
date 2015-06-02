package edu.tamu.tcat.trc.entries.search.solr.impl;

import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
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
         boolean hasTime = value.isSupported(ChronoField.HOUR_OF_DAY) &&
                           value.isSupported(ChronoField.MINUTE_OF_HOUR) &&
                           value.isSupported(ChronoField.SECOND_OF_MINUTE);
         boolean hasMD = value.isSupported(ChronoField.MONTH_OF_YEAR) &&
                         value.isSupported(ChronoField.DAY_OF_MONTH);
         boolean hasYear = value.isSupported(ChronoField.YEAR);

         // Append a literal 'Z' to all to indicate to SOLR that it is in UTC
         if (hasYear && hasMD && hasTime)
            return DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(value) + 'Z';
         if (hasYear && hasMD)
            return DateTimeFormatter.ISO_LOCAL_DATE.format(value) + "T00:00:00Z";
         throw new IllegalArgumentException("Unable to format value ["+value+"], must be YMD or YMD+HMS");
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
