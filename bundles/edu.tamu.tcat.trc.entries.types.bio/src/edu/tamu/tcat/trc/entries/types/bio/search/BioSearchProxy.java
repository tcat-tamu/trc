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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.LocalDate;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.tamu.tcat.trc.entries.types.bio.Person;
import edu.tamu.tcat.trc.entries.types.bio.PersonName;
import edu.tamu.tcat.trc.entries.types.bio.dto.PersonNameDTO;
import edu.tamu.tcat.trc.entries.types.bio.internal.Activator;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.util.InvalidFormatException;

/**
 * JSON serializable summary information about a biographical entry (i.e. a person).
 * Intended to be returned when only a brief summary of the person is required to save
 * data transfer and parsing resources.
 */
public class BioSearchProxy
{
   private static final String SENTENCE_MODEL_FILE_PATH = "resources/sentence-model-en.bin";
   private static final Logger logger = Logger.getLogger(BioSearchProxy.class.getName());

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
    * An excerpt (canonically the first sentence) of the biographical summary to give the search
    * result some context. This value may be null if no summary has been provided yet.
    */
   public String summaryExcerpt;


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
      this.summaryExcerpt = getSummaryExcerpt(person);
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

   /**
    * Gets a summary excerpt for a person
    *
    * @param person
    * @return
    */
   public static String getSummaryExcerpt(Person person)
   {
      String summary = person.getSummary();

      // remove HTML tags for sentence extraction
      String summaryStripped = summary.replaceAll("<[^>]+>", "");

      return getFirstSentence(summaryStripped);
   }

   /**
    * Gets the first sentence from a string of English text,
    * or {@code null} if the string cannot be parsed.
    *
    * @param text
    * @return
    */
   public static String getFirstSentence(String text)
   {
      URL modelFileUri = Activator.getContext().getBundle().getEntry(SENTENCE_MODEL_FILE_PATH);

      try (InputStream modelInput = modelFileUri.openStream())
      {
         SentenceModel sentenceModel = new SentenceModel(modelInput);
         SentenceDetectorME detector = new SentenceDetectorME(sentenceModel);
         String[] summarySentences = detector.sentDetect(text);
         return (summarySentences.length == 0) ? null : summarySentences[0];
      }
      catch (InvalidFormatException e) {
         logger.log(Level.SEVERE, "sentence detect model input has incorrect format", e);
      }
      catch (IOException e) {
         logger.log(Level.SEVERE, "unable to open sentence detect model input file", e);
      }

      return null;
   }
}
