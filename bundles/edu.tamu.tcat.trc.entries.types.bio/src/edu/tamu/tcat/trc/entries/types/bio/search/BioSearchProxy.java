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
package edu.tamu.tcat.trc.entries.types.bio.search;

import java.time.LocalDate;
import java.util.Set;

import edu.tamu.tcat.trc.entries.types.bio.Person;
import edu.tamu.tcat.trc.entries.types.bio.PersonName;
import edu.tamu.tcat.trc.entries.types.bio.dto.PersonNameDTO;

/**
 * JSON serializable summary information about a biographical entry (i.e. a person).
 * Intended to be returned when only a brief summary of the person is required to save
 * data transfer and parsing resources.
 */
public class BioSearchProxy
{
   /**
    * ID corresponding to the {@link Person} object that this simple data vehicle represents.
    */
   public String id;

   /**
    * Display name for this person (for use when populating fields)
    */
   public PersonNameDTO displayName;

   /**
    * Formatted name to display e.g. when linking to the underlying {@link Person}.
    *
    * This is essentially a string representation of the display name plus the lifespan of the person.
    */
   public String formattedName;


   /**
    * Default constructor
    */
   public BioSearchProxy()
   {
   }

   /**
    * Populate a new BioSearchProxy from an existing {@link Person} object.
    *
    * @param person Existing person from which to copy data.
    */
   public BioSearchProxy(Person person)
   {
      this.id = person.getId();
      this.displayName = getDisplayName(person);
      this.formattedName = getFormattedName(person);
   }

   /**
    * Assembles a formatted name consisting of the person's full display name followed by their
    * lifespan. For example, "Reuben Archer Torrey (1856–1928)"
    *
    * @param person Person whose name to format.
    * @return Formatted name and lifespan.
    */
   public static String getFormattedName(Person person)
   {
      PersonNameDTO name = getDisplayName(person);
      LocalDate birthDate = person.getBirth().getDate().getCalendar();
      LocalDate deathDate = person.getDeath().getDate().getCalendar();

      // HACK: fallback if no name for person (should this even be permissible?)
      String displayName = "unnamed";

      // HACK: with current implementation, it's possible that name could be null. Should this be the case?
      if (name != null) {
         // try to use display name
         displayName = name.displayName;

         // fall back to first + last
         if (displayName == null) {
            displayName = String.format("%s %s",
                  (name.givenName == null) ? "" : name.givenName.trim(),
                  (name.familyName == null) ? "" : name.familyName.trim()
               ).trim();
         }
      }


      return String.format("%s (%s–%s)",
            displayName,
            (birthDate == null) ? "?" : String.valueOf(birthDate.getYear()),
            (deathDate == null) ? "?" : String.valueOf(deathDate.getYear()));
   }

   /**
    * Gets a display name for a person
    *
    * @param person
    * @return
    */
   public static PersonNameDTO getDisplayName(Person person)
   {
      // use canonical name by default
      PersonName name = person.getCanonicalName();

      // fall back to first element of alternate names
      if (name == null) {
         Set<? extends PersonName> names = person.getAlternativeNames();
         if (!names.isEmpty())
            name = names.iterator().next();
      }

      return PersonNameDTO.create(name);
   }
}
