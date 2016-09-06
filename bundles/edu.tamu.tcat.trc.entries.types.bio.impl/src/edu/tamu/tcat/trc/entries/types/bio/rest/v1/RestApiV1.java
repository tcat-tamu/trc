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
package edu.tamu.tcat.trc.entries.types.bio.rest.v1;

import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * An encapsulation of the data vehicle types used to process JSON requests and responses
 * for version 1 of the TRC REST API for Persons.
 */
public class RestApiV1
{
   public static class PersonId
   {
      public String id;
   }

   public static class SimpleLink
   {
      public URI uri;
      public String type;
   }

   public static class EntryMeta
   {
      /**
       * A short, unique identifier for this entry that is intended to be more readily
       * human readable than the id.
       */
      public String slug;

      /**
       *  A monotonically increasing version number. This will be updated on any changes to
       *  the information associated with this person.
       */
      public int version;

      /**
       * Links to various representations of this entity and related resources. The map keys
       * indicate the role of the link (e.g., self, alternate) while the type is the mimetype
       * of the related resource.
       */
      public Map<String, SimpleLink> links;

      /** ISO 8601 formatted timestamp of when this record was last modified. */
      public String lastModified;

      // TODO publication?? provenance, state,...
   }

   /**
    * A representation of the biographical entry for a person.
    */
   public static class Person
   {

      // TODO tags, notes, categories, classification, bibliographic references etc.

      /** Descriptive information about this record. */
      public EntryMeta meta;

      /** The unique identifier for this person. */
      public String id;

      /**
       *  The primary name used to identify this person for the purposes of this biographical
       *  entry. Note that the selection of a primary name reflects an editorial decision that
       *  will be made within the overall editorial and rhetorical goals of a project.
       *  For example, for a particular individual, should the name 'Mark Twain' or
       *  'Samuel Langhorne Clemens'. Unless this is clarified by context or other editorial
       *  commentary, the primary name should generally be the name commonly used to refer to
       *  this individual in modern scholarship or public discourse. For example, prefer
       *  Lord Kelvin to William Thomson.
       */
      public PersonName name = new PersonName();

      /**
       *  Other names by which this person is known. Historical figures are often known by a
       *  variety of names including pen-names, titles of nobility, alternate spellings,
       *  language variants and more. This field is intended to document key names by which
       *  this person is known other than the person's primary name.
       */
      public Set<PersonName> altNames = new HashSet<>();

      // TODO simplify? Use links to historical events separately? Might have more than one
      //      (e.g., for contested/uncertain dates). Need to think through this a bit.

      /** The date this person was born. May be {@code null} if the date of birth for this
       *  person is not known. */
      public HistoricalEvent birth;

      /** The date this person died. May be {@code null} if the date of birth for this
       *  person is not known. */
      public HistoricalEvent death;

      /**
       *  A brief summary of this person that contextualizes their significance relative to
       *  this thematic research collection. Will be plain-text or lightly marked HTML. While
       *  specific projects may provide different editorial guidelines this is expected to be
       *  on the order of 150-500 words. The first sentence should be a mini-summary as many
       *  applications will use it to supply initial context for the person.
       */
      public String summary;
   }

   /**
    * A simplified representation of information about a person for use in contexts like
    * search results where basic information is needed while minimizing the amount of data
    * transferred for each record.
    */
   public static class SimplePerson
   {
      public EntryMeta meta;

      /** Unique id of this person. */
      public String id;

      /** Structured representation of this person's name. */
      public PersonName name;

      /**
       * Formatted name to display. Intended to be a string representation of the display
       * name plus the lifespan of the person.
       */
      public String label;

      /**  A leading excerpt from the full bibliographic summary */
      public String summaryExcept;
   }

   /**
    * A structured representation of a person's name. Note that this is a relatively simple
    * model and does not attempt account for the full variety of person names. Most notably,
    * it does not include notions of particles (such as <em>von</em> in <em>von Miller</em>).
    *
    * <p>See the following for a detailed discussion of structuring names in bibliographic
    * contexts:
    * @see http://docs.citationstyles.org/en/1.0.1/specification.html#name-particles
    * @see http://docs.citationstyles.org/en/1.0.1/specification.html#names
    */
   public static class PersonName
   {
      /**
       *  The role of this name. Historical figures are often known by a variety of names.
       *  These include pen-names, titles of nobility, alternate spellings, language variants
       *  and more. This field is designed to clarify the use of this name.
       */
      public String role;

      /**
       *  A simple representation of this name for display purposes. This may be a
       *  straight forward combination of fields above or may include user-supplied nicknames
       *  or other reformatting. For example, 'James "Red" Duke' for
       *  'James Henry Duke, Jr., M.D.'
       */
      public String label;

      /** The title of address for this person. May be {@code null} or empty string. */
      public String title;

      /** Given or first name for this name. May be {@code null} or empty string. */
      public String givenName;

      /** The middle name for this name. May be {@code null} or empty string.*/
      public String middleName;

      /** The family or last name for this name. May be {@code null} or empty string. */
      public String familyName;

      /** The suffix (such as Jr, III) for this name. May be {@code null} or empty string. */
      public String suffix;

      @Override
      public boolean equals(Object obj)
      {
         if (!PersonName.class.isInstance(obj))
            return false;

         PersonName pn = (PersonName)obj;

         return Objects.equals(label, pn.label) &&
                Objects.equals(title, pn.title) &&
                Objects.equals(givenName, pn.givenName) &&
                Objects.equals(middleName, pn.middleName) &&
                Objects.equals(familyName, pn.familyName) &&
                Objects.equals(suffix, pn.suffix);
      }

      @Override
      public int hashCode()
      {
         int result = 17;
         result = 31 * result + ((label == null) ? 0 : label.hashCode());
         result = 31 * result + ((title == null) ? 0 : title.hashCode());
         result = 31 * result + ((givenName == null) ? 0 : givenName.hashCode());
         result = 31 * result + ((middleName == null) ? 0 : middleName.hashCode());
         result = 31 * result + ((familyName == null) ? 0 : familyName.hashCode());
         result = 31 * result + ((suffix == null) ? 0 : suffix.hashCode());

         return result;
      }
   }

   /**
    *  A representation of a historical event.
    *
    *  <p>
    *  Currently this is used for the birth and death dates of a person. As the TRC framework
    *  grows, this is likely to be incorporated into a separate module.
    */
   public static class HistoricalEvent
   {
      /** A unique identifier for this event. */
      public String id;

      /** A brief title for this event for display purposes. For example, "The birth
       *  of William Paley", "The Battle of Hastings", or "The Council in Acts 15" */
      public String title;

      /** A longer descriptive summary of this event. */
      public String description;

      /** The location in which this event took place. Ideally, this is a reference that can
       *  be resolved to a specific location using geo-location tools. */
      public String location;   // TODO eventaually, more structure will be needed here.

      /** The date this event took place. */
      public DateDescription date;
   }

   /**
    *  A description of a date. Dates of historical events are commonly related through
    *  narrative descriptions that convey both explicit and implicit information about the
    *  date. For example, a manuscript might be dated as "likely early third century" or a
    *  painting made "in the winter of 1834". Those narrative descriptions convey a degree of
    *  certainty and precision (or lack thereof) as well as geographic reference (winter in
    *  Finland is not the same as winter in Spain) and disciplinary convention. The richness of
    *  these narrative descriptions is hard to replicate in a traditional software systems in
    *  which dates are treated as precise chronological structures. Of course, those
    *  chronological structures are also quite useful for data visualizations and queries.  *
    *
    *  <p>
    *  The goal of the {@code DateDescription} is to bridge these two representations for a
    *  date. In this model, the descriptive, human readable representation of the date is
    *  taken as primary while the formal calendar data provides supplementary contextual
    *  information to aid in searching and visualization.
    */
   public static class DateDescription
   {
      /** ISO 8601 local (YYYY-MM-DD) representation of this date. */
      public String calendar;

      /** A human readable description of this date. */
      public String description;
   }

   public static class PersonSearchResultSet
   {
      public List<SimplePerson> items;
      /** The querystring that resulted in this result set */
      public String qs;
      public String qsNext;
      public String qsPrev;
   }


}
