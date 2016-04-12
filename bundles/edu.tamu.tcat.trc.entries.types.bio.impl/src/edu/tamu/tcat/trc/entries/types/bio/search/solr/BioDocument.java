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
package edu.tamu.tcat.trc.entries.types.bio.search.solr;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;

import org.apache.solr.common.SolrInputDocument;

import edu.tamu.tcat.trc.entries.common.dto.DateDescriptionDTO;
import edu.tamu.tcat.trc.entries.common.dto.HistoricalEventDTO;
import edu.tamu.tcat.trc.entries.types.bio.Person;
import edu.tamu.tcat.trc.entries.types.bio.dto.PersonDTO;
import edu.tamu.tcat.trc.entries.types.bio.dto.PersonNameDTO;
import edu.tamu.tcat.trc.entries.types.bio.search.BioSearchProxy;
import edu.tamu.tcat.trc.search.SearchException;
import edu.tamu.tcat.trc.search.solr.SolrIndexField;
import edu.tamu.tcat.trc.search.solr.impl.TrcDocument;

/**
 * Represents a document in the SOLR search index. Exports its representation as
 * a {@link SolrInputDocument}, which includes a {@link BioSearchProxy} DTO as one of the fields.
 *
 * @see {@link BioSearchProxy} which is the DTO stored in one of the fields of this proxy.
 */
public class BioDocument
{
   // composed instead of extended to not expose TrcDocument as API to this class
   private TrcDocument indexDocument;

   public BioDocument()
   {
      indexDocument = new TrcDocument(new BioSolrConfig());
   }

   public SolrInputDocument getDocument()
   {
      return indexDocument.getSolrDocument();
   }

   public static BioDocument create(Person person) throws SearchException
   {
      BioDocument doc = new BioDocument();
      PersonDTO personDV = new PersonDTO(person);

      try
      {
         doc.indexDocument.set(BioSolrConfig.SEARCH_PROXY, new BioSearchProxy(person));
      }
      catch (Exception e)
      {
         throw new IllegalStateException("Failed to serialize BioSearchProxy data", e);
      }

      doc.indexDocument.set(BioSolrConfig.ID, personDV.id);

      doc.indexDocument.set(BioSolrConfig.SYNTHETIC_NAME, constructSyntheticName(personDV.getAllNames()));

      PersonNameDTO name = personDV.displayName;
      doc.indexDocument.set(BioSolrConfig.FAMILY_NAME, guardNull(name.familyName));
      doc.indexDocument.set(BioSolrConfig.DISPLAY_NAME, guardNull(name.displayName));

      HistoricalEventDTO birth = personDV.birth;
      doc.indexDocument.set(BioSolrConfig.BIRTH_LOCATION, guardNull(birth.location));
      setDateValue(doc, BioSolrConfig.BIRTH_DATE, birth.date);

      HistoricalEventDTO death = personDV.death;
      doc.indexDocument.set(BioSolrConfig.DEATH_LOCATION, guardNull(death.location));
      setDateValue(doc, BioSolrConfig.DEATH_DATE, death.date);

      doc.indexDocument.set(BioSolrConfig.SUMMARY, guardNull(personDV.summary));

      return doc;
   }

   /**
    * Must not supply null values, so perform the required check here.
    *
    * @param doc
    * @param field
    * @param date
    * @throws SearchException
    */
   private static void setDateValue(BioDocument doc, SolrIndexField<LocalDate> field, DateDescriptionDTO date) throws SearchException
   {
      if (date == null)
         return;

      LocalDate value = convertDate(date);
      if (value == null)
         return;

      doc.indexDocument.set(field, value);
   }

   private static String guardNull(String value)
   {
      return value == null ? "" : value;
   }

   private static LocalDate convertDate(DateDescriptionDTO date)
   {
      if (date.calendar != null)
         return LocalDate.from(DateTimeFormatter.ISO_DATE.parse(date.calendar));
      return null;
   }

   /**
    * Constructs a synthetic name that contains the various values (title, first name,
    * family name, etc) from different names associated with this person. Each portion
    * of a person's name is collected into a set of 'name parts' that is then concatenated
    * to form a string-valued synthetic name. This allows all of the various name tokens to
    * be included in the search.
    *
    * @param names A set of names associated with a person.
    * @return A synthetic name that contains a union of the different name fields.
    */
   private static String constructSyntheticName(Set<PersonNameDTO> names)
   {
      Set<String> nameParts = new HashSet<>();
      for(PersonNameDTO name : names)
      {
         nameParts.add(name.title);
         nameParts.add(name.givenName);
         nameParts.add(name.middleName);
         nameParts.add(name.familyName);
      }

      StringBuilder sb = new StringBuilder();
      for (String part : nameParts)
      {
         if (part == null)
            continue;

         sb.append(part).append(" ");
      }

      return sb.toString().trim();
   }
}
