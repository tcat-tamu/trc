package edu.tamu.tcat.trc.entries.types.bio.search.solr;

import java.text.MessageFormat;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

import org.apache.solr.common.SolrInputDocument;

import edu.tamu.tcat.trc.entries.common.DateDescription;
import edu.tamu.tcat.trc.entries.common.HistoricalEvent;
import edu.tamu.tcat.trc.entries.types.bio.Person;
import edu.tamu.tcat.trc.entries.types.bio.PersonName;
import edu.tamu.tcat.trc.entries.types.bio.search.BioSearchProxy;
import edu.tamu.tcat.trc.search.SearchException;
import edu.tamu.tcat.trc.search.solr.SolrIndexField;
import edu.tamu.tcat.trc.search.solr.impl.TrcDocument;

public class SolrDocAdapter implements Function<Person, SolrInputDocument>
{
   private final Function<String, String> sentenceParser;

   public SolrDocAdapter(Function<String, String> sentenceParser)
   {
      this.sentenceParser = sentenceParser;
   }

   @Override
   public SolrInputDocument apply(Person person)
   {
      try
      {
         TrcDocument indexDocument = new TrcDocument(new BioSolrConfig());

         indexDocument.set(BioSolrConfig.SEARCH_PROXY, toProxy(person));
         indexDocument.set(BioSolrConfig.ID, person.getId());
         indexDocument.set(BioSolrConfig.SYNTHETIC_NAME, constructSyntheticName(person.getNames()));

         PersonName name = person.getCanonicalName();
         if (name != null)
         {
            indexDocument.set(BioSolrConfig.FAMILY_NAME, guardNull(name.getFamilyName()));
            indexDocument.set(BioSolrConfig.DISPLAY_NAME, guardNull(name.getDisplayName()));
         }

         HistoricalEvent birth = person.getBirth();
         if (birth != null)
         {
            indexDocument.set(BioSolrConfig.BIRTH_LOCATION, guardNull(birth.getLocation()));
            setDateValue(indexDocument, BioSolrConfig.BIRTH_DATE, birth.getDate());
         }

         HistoricalEvent death = person.getDeath();
         if (death != null)
         {
            indexDocument.set(BioSolrConfig.DEATH_LOCATION, guardNull(death.getLocation()));
            setDateValue(indexDocument, BioSolrConfig.DEATH_DATE, death.getDate());
         }

         indexDocument.set(BioSolrConfig.SUMMARY, guardNull(person.getSummary()));
         return indexDocument.getSolrDocument();
      }
      catch (Exception ex)
      {
         throw new IllegalStateException("Failed to construct searchable representation of a person." + person , ex);
      }
   }



   private BioSearchProxy toProxy(Person person)
   {
      BioSearchProxy proxy = new BioSearchProxy();
      proxy.id = person.getId();

      PersonName dname = getDisplayName(person);
      proxy.displayName.family = dname.getFamilyName();
      proxy.displayName.given = dname.getGivenName();
      proxy.displayName.display = dname.getDisplayName();
      proxy.formattedName = getFormattedName(person);
      proxy.summaryExcerpt = getSummaryExcerpt(person);

      return proxy;
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
   private static String constructSyntheticName(Collection<PersonName> names)
   {
      Set<String> nameParts = new HashSet<>();
      for(PersonName name : names)
      {
         nameParts.add(name.getTitle());
         nameParts.add(name.getGivenName());
         nameParts.add(name.getMiddleName());
         nameParts.add(name.getFamilyName());
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

   /**
    * Must not supply null values, so perform the required check here.
    *
    * @param doc
    * @param field
    * @param date
    * @throws SearchException
    */
   private static void setDateValue(TrcDocument doc, SolrIndexField<LocalDate> field, DateDescription date) throws SearchException
   {
      if (date == null)
         return;

      LocalDate value = date.getCalendar();
      if (value == null)
         return;

      doc.set(field, value);
   }

   private static String guardNull(String value)
   {
      String result = value == null ? "" : value;
      return result.trim();
   }

   /**
    * Assembles a formatted name consisting of the person's full display name followed by their
    * lifespan. For example, "Reuben Archer Torrey (1856–1928)"
    *
    * @param person Person whose name to format.
    * @return Formatted name and lifespan.
    */
   private String getFormattedName(Person person)
   {
      String displayName = "unnamed";
      PersonName name = getDisplayName(person);
      if (name != null) {
         displayName = name.getDisplayName();
         if (displayName == null) {
            displayName = MessageFormat.format("{0} {1}",
                  guardNull(name.getGivenName()),
                  guardNull(name.getFamilyName()));
         }
      }

      LocalDate birthDate = person.getBirth().getDate().getCalendar();
      LocalDate deathDate = person.getDeath().getDate().getCalendar();
      return MessageFormat.format("{0} ({1}-{2})",
            displayName.trim(),
            (birthDate == null) ? "?" : String.valueOf(birthDate.getYear()),
            (deathDate == null) ? "?" : String.valueOf(deathDate.getYear()));
   }

   /**
    * Gets a display name for a person
    *
    * @param person
    * @return
    */
   private PersonName getDisplayName(Person person)
   {
      // use canonical name by default
      PersonName name = person.getCanonicalName();

      // fall back to first element of alternate names
      if (name == null) {
         Set<? extends PersonName> names = person.getAlternativeNames();
         if (!names.isEmpty())
            name = names.iterator().next();
      }

      return name;
   }

   /**
    * Gets a summary excerpt for a person
    *
    * @param person
    * @return
    */
   private String getSummaryExcerpt(Person person)
   {
      // remove HTML tags for sentence extraction
      String summary = person.getSummary();

      if (summary == null)
      {
         return "";
      }

      String summaryStripped = summary.replaceAll("<[^>]+>", "");
      return sentenceParser.apply(summaryStripped);
   }

   /**
    * Gets the first sentence from a string of English text,
    * or {@code null} if the string cannot be parsed.
    *
    * @param text
    * @return
    */
//   public static String getFirstSentence(String text)
//   {
//      URL modelFileUri = Activator.getContext().getBundle().getEntry(SENTENCE_MODEL_FILE_PATH);
//
//      try (InputStream modelInput = modelFileUri.openStream())
//      {
//         SentenceModel sentenceModel = new SentenceModel(modelInput);
//         SentenceDetectorME detector = new SentenceDetectorME(sentenceModel);
//         String[] summarySentences = detector.sentDetect(text);
//         return (summarySentences.length == 0) ? null : summarySentences[0];
//      }
//      catch (InvalidFormatException e) {
//         logger.log(Level.SEVERE, "sentence detect model input has incorrect format", e);
//      }
//      catch (IOException e) {
//         logger.log(Level.SEVERE, "unable to open sentence detect model input file", e);
//      }
//
//      return null;
//   }

}
