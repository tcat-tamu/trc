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
package edu.tamu.tcat.trc.entries.types.bio.dto;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnore;

import edu.tamu.tcat.trc.entries.common.HistoricalEvent;
import edu.tamu.tcat.trc.entries.types.bio.Person;
import edu.tamu.tcat.trc.entries.types.bio.PersonName;

/**
 * Represents a Person
 *
 * @deprecated This is an implementation-level detail and will be removed from the API in 2.0.0.
 */
@Deprecated
public class PersonDTO
{
   public String id;
   public PersonNameDTO displayName;
   public Set<PersonNameDTO> names;
   public HistoricalEventDTO birth;
   public HistoricalEventDTO death;
   public String summary;

   public PersonDTO()
   {

   }

   /**
    * @since 1.1
    */
   public PersonDTO(Person figure)
   {
      this.id = figure.getId();

      PersonName canonicalName = figure.getCanonicalName();
      if (canonicalName != null) {
         this.displayName = PersonNameDTO.create(canonicalName);
      }

      this.names = figure.getAlternativeNames().stream()
                     .map(PersonNameDTO::create)
                     .collect(Collectors.toSet());

      this.birth = HistoricalEventDTO.create(figure.getBirth());
      this.death = HistoricalEventDTO.create(figure.getDeath());
      this.summary = figure.getSummary();
   }

   @Deprecated // use constructor instead
   public static PersonDTO create(Person figure)
   {
      return new PersonDTO(figure);
   }

   @Deprecated
   public static Person instantiate(PersonDTO figure)
   {
      PersonImpl person = new PersonImpl();
      person.id = figure.id;
      person.canonicalName = getCanonicalName(figure);
      person.names = figure.names.stream()
                        .map(PersonNameDTO::instantiate)
                        .collect(Collectors.toSet());

      if (figure.birth != null)
      {
         person.birth = new HistoricalEventImpl(figure.birth);
      }

      if (figure.death != null)
      {
         person.death = new HistoricalEventImpl(figure.death);
      }

      person.summary = figure.summary;

      return person;
   }


   /**
    * Get a canonical name from a person DV. Prefer to use the displayName field, but fall back to
    * the first element in the 'names' set if a displayName is not available.
    *
    * @param figure
    * @return canonical name for this person
    */
   private static PersonName getCanonicalName(PersonDTO figure)
   {
      // try the 'displayName' first
      if (figure.displayName != null) {
         return PersonNameDTO.instantiate(figure.displayName);
      }

      // fall back to using the first element of the 'names' set
      if (!figure.names.isEmpty()) {
         PersonNameDTO nameDV = figure.names.iterator().next();
         return PersonNameDTO.instantiate(nameDV);
      }

      // fall back to "Name Unknown" if this person does not have any names
      PersonNameDTO fallbackName = new PersonNameDTO();
      fallbackName.displayName = "Name Unknown";
      return PersonNameDTO.instantiate(fallbackName);
   }


   @Override
   public String toString()
   {
      StringBuilder sb = new StringBuilder();

      for (PersonNameDTO name : names)
      {
         if (name.displayName != null)
         {
            sb.append(name.displayName);
         }
         else
         {
            String fn = name.familyName;
            String gn = name.givenName;
            if (fn != null && !fn.trim().isEmpty()) {
               sb.append(fn.trim());
            }

            if (gn != null && !gn.trim().isEmpty())
            {
               if (sb.length() > 0) {
                  sb.append(", ");
               }

               sb.append(gn.trim());
            }
         }

         // TODO append dates

         break;
      }

      return sb.toString();
   }

   @JsonIgnore
   public Set<PersonNameDTO> getAllNames()
   {
      Set<PersonNameDTO> allNames = new HashSet<>(this.names);
      allNames.add(this.displayName);
      return allNames;
   }

   @Deprecated
   public static class PersonImpl implements Person
   {
      private String id;
      private PersonName canonicalName;
      private Set<PersonName> names;
      private HistoricalEventImpl birth;
      private HistoricalEventImpl death;
      private String summary;

      @Override
      public String getId()
      {
         return id;
      }

      @Override
      public PersonName getCanonicalName()
      {
         return canonicalName;
      }

      @Override
      public Set<PersonName> getAlternativeNames()
      {
         return Collections.unmodifiableSet(names);
      }

      @Override
      public HistoricalEvent getBirth()
      {
         return birth;
      }

      @Override
      public HistoricalEvent getDeath()
      {
         return death;
      }

      @Override
      public String getSummary()
      {
         return summary;
      }

      @Override
      public String toString()
      {
         StringBuilder sb = new StringBuilder();

         // use canonical name for display purposes
         PersonName name = canonicalName;

         // fall back to first element of names
         if (name == null && !names.isEmpty()) {
            name = names.iterator().next();
         }

         if (name != null) {
            if (name.getDisplayName() != null)
            {
               sb.append(name.getDisplayName());
            }
            else
            {
               String fn = name.getFamilyName();
               String gn = name.getGivenName();
               if (fn != null && !fn.trim().isEmpty()) {
                  sb.append(fn.trim());
               }

               if (gn != null && !gn.trim().isEmpty())
               {
                  if (sb.length() > 0) {
                     sb.append(", ");
                  }

                  sb.append(gn.trim());
               }
            }

            // TODO append dates
         }

         return sb.toString();
      }

      // equals and hash code?
   }

}
