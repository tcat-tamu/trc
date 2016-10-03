/*
 * Copyright 2015 Texas A&M Engineering Experiment Station
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.tamu.tcat.trc.search.solr.impl;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.tamu.tcat.trc.SearchException;
import edu.tamu.tcat.trc.search.solr.SolrIndexField;

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

   public static class BasicInteger extends FieldBase<Integer>
   {
      public BasicInteger(String name)
      {
         super(name, Integer.class);
      }

      @Override
      public String toSolrValue(Integer value) throws SearchException
      {
         return value != null ? value.toString() : null;
      }
   }

   public static class BasicDate extends FieldBase<LocalDate>
   {
      public BasicDate(String name)
      {
         super(name, LocalDate.class);
      }

      @Override
      public String toSolrValue(LocalDate value) throws SearchException
      {
         try
         {
            if (value == null)
               return "";

            return DateTimeFormatter.ISO_LOCAL_DATE.format(value) + "T00:00:00Z";
         }
         catch (Exception e)
         {
            throw new IllegalArgumentException("Unable to format 'date' value ["+value+"]", e);
         }
      }
   }

   public static class BasicDateTime extends FieldBase<LocalDateTime>
   {
      public BasicDateTime(String name)
      {
         super(name, LocalDateTime.class);
      }

      @Override
      public String toSolrValue(LocalDateTime value) throws SearchException
      {
         try
         {
            if (value == null)
               return "";

            // Append a literal 'Z' to all to indicate to SOLR that it is in UTC
            return DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(value) + 'Z';
         }
         catch (Exception e)
         {
            throw new IllegalArgumentException("Unable to format 'date-time' value ["+value+"]", e);
         }
      }
   }

   public static class BasicInstant extends FieldBase<Instant>
   {
      public BasicInstant(String name)
      {
         super(name, Instant.class);
      }

      @Override
      public String toSolrValue(Instant value) throws SearchException
      {
         try
         {
            if (value == null)
               return "";

            return DateTimeFormatter.ISO_INSTANT.format(value);
         }
         catch (Exception e)
         {
            throw new IllegalArgumentException("Unable to format 'instant' value ["+value+"]", e);
         }
      }
   }

   /**
    * A field that stores a search proxy instance as a JSON literal. This class
    * also contains utilities to parse the value for returning results.
    *
    * @param <T>
    */
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

      /**
       * Parse the JSON literal form of the object into the type represented by
       * this field. The JSON mapping implementation is implementation-defined and
       * may be overridden to provide different means of serializing.
       */
      public T parse(String str) throws Exception
      {
         return getMapper().readValue(str, type);
      }

      /**
       * Configured here for internal use. This method may be overridden to provide
       * a variant configuration for an ObjectMapper.
       * <p>
       * Implementations should return a new instance
       * to aid with thread-safety, deduplication, and other concerns on the caller.
       */
      protected ObjectMapper getMapper()
      {
         ObjectMapper mapper = new ObjectMapper();
         mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
         return mapper;
      }
   }
}
