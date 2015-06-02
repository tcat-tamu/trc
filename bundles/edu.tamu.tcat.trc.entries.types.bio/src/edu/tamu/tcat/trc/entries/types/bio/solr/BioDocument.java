package edu.tamu.tcat.trc.entries.types.bio.solr;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.solr.common.SolrInputDocument;

import edu.tamu.tcat.trc.entries.common.dto.DateDescriptionDTO;
import edu.tamu.tcat.trc.entries.common.dto.HistoricalEventDTO;
import edu.tamu.tcat.trc.entries.search.SearchException;
import edu.tamu.tcat.trc.entries.search.solr.impl.TrcDocument;
import edu.tamu.tcat.trc.entries.types.bio.Person;
import edu.tamu.tcat.trc.entries.types.bio.dto.PersonDTO;
import edu.tamu.tcat.trc.entries.types.bio.dto.PersonNameDTO;
import edu.tamu.tcat.trc.entries.types.bio.search.BioSearchProxy;

/**
 * Represents a document in the SOLR search index. Exports its representation as
 * a {@link SolrInputDocument}, which includes a {@link BioSearchProxy} DTO as one of the fields.
 *
 * @see {@link BioSearchProxy} which is the DTO stored in one of the fields of this proxy.
 */
public class BioDocument
{
   private final static Logger logger = Logger.getLogger(BioDocument.class.getName());

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
      PersonDTO personDV = PersonDTO.create(person);

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
      DateDescriptionDTO bDate = birth.date;
      if (bDate != null)
         doc.indexDocument.set(BioSolrConfig.BIRTH_DATE, convertDate(bDate));

      HistoricalEventDTO death = personDV.birth;
      doc.indexDocument.set(BioSolrConfig.DEATH_LOCATION, guardNull(death.location));
      if (death.date != null)
         doc.indexDocument.set(BioSolrConfig.DEATH_DATE, convertDate(death.date));

      doc.indexDocument.set(BioSolrConfig.SUMMARY, guardNull(personDV.summary));

      return doc;
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
