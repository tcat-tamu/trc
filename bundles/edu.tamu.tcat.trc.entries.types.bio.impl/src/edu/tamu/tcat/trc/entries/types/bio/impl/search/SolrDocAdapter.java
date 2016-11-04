package edu.tamu.tcat.trc.entries.types.bio.impl.search;

import java.text.MessageFormat;
import java.time.LocalDate;
import java.util.Set;
import java.util.function.Function;

import org.apache.solr.common.SolrInputDocument;

import edu.tamu.tcat.trc.entries.common.DateDescription;
import edu.tamu.tcat.trc.entries.common.HistoricalEvent;
import edu.tamu.tcat.trc.entries.types.bio.BiographicalEntry;
import edu.tamu.tcat.trc.entries.types.bio.PersonName;
import edu.tamu.tcat.trc.entries.types.bio.repo.BiographicalEntryRepository;
import edu.tamu.tcat.trc.entries.types.bio.search.BioSearchProxy;
import edu.tamu.tcat.trc.resolver.EntryId;
import edu.tamu.tcat.trc.resolver.EntryResolverRegistry;
import edu.tamu.tcat.trc.search.solr.SearchException;
import edu.tamu.tcat.trc.search.solr.SolrIndexField;
import edu.tamu.tcat.trc.search.solr.impl.TrcDocument;

public class SolrDocAdapter implements Function<BiographicalEntry, SolrInputDocument>
{
   private final Function<String, String> sentenceParser;
   private final EntryResolverRegistry resolvers;

   public SolrDocAdapter(Function<String, String> sentenceParser, EntryResolverRegistry resolvers)
   {
      this.sentenceParser = sentenceParser;
      this.resolvers = resolvers;
   }

   @Override
   public SolrInputDocument apply(BiographicalEntry person)
   {
      try
      {
         TrcDocument indexDocument = new TrcDocument(new BioSolrConfig());
         EntryId entryId = new EntryId(person.getId(), BiographicalEntryRepository.ENTRY_TYPE_ID);
         String token = resolvers.tokenize(entryId);

         indexDocument.set(BioSolrConfig.SEARCH_PROXY, toProxy(person, token));
         indexDocument.set(BioSolrConfig.ID, person.getId());
         indexDocument.set(BioSolrConfig.ENTRY_REFERENCE, token);

         setName(person, indexDocument);
         setBirth(person, indexDocument);
         setDeath(person, indexDocument);

         person.getAlternativeNames().forEach(name ->
               indexDocument.set(BioSolrConfig.ALT_NAMES, makeAltNameText(name)));

         indexDocument.set(BioSolrConfig.SUMMARY, guardNull(person.getSummary()));
         return indexDocument.build();
      }
      catch (Exception ex)
      {
         throw new IllegalStateException("Failed to construct searchable representation of a person." + person , ex);
      }
   }

   private void setName(BiographicalEntry person, TrcDocument indexDocument)
   {
      PersonName name = getDisplayName(person);
      if (name == null)
         return;

      indexDocument.set(BioSolrConfig.FAMILY_NAME, guardNull(name.getFamilyName()));
      indexDocument.set(BioSolrConfig.GIVEN_NAME, guardNull(name.getGivenName()));
   }

   private void setBirth(BiographicalEntry person, TrcDocument indexDocument)
   {
      HistoricalEvent birth = person.getBirth();
      if (birth == null)
         return;

      indexDocument.set(BioSolrConfig.BIRTH_LOCATION, guardNull(birth.getLocation()));
      setDateValue(indexDocument, BioSolrConfig.BIRTH_DATE, birth.getDate());
   }

   private void setDeath(BiographicalEntry person, TrcDocument indexDocument)
   {
      HistoricalEvent death = person.getDeath();
      if (death == null)
         return;

      indexDocument.set(BioSolrConfig.DEATH_LOCATION, guardNull(death.getLocation()));
      setDateValue(indexDocument, BioSolrConfig.DEATH_DATE, death.getDate());
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

   private static String makeAltNameText(PersonName name)
   {
      return String.join(" ", guardNull(name.getTitle()),
                              guardNull(name.getGivenName()),
                              guardNull(name.getMiddleName()),
                              guardNull(name.getFamilyName()),
                              guardNull(name.getSuffix()),
                              guardNull(name.getDisplayName()));
   }

   private BioSearchProxy toProxy(BiographicalEntry person, String token)
   {
      BioSearchProxy proxy = new BioSearchProxy();
      proxy.id = person.getId();
      proxy.token = token;

      PersonName dname = getDisplayName(person);
      proxy.formattedName = getFormattedName(person);
      proxy.displayName.family = dname.getFamilyName();
      proxy.displayName.given = dname.getGivenName();
      proxy.displayName.display = dname.getDisplayName();
      proxy.summaryExcerpt = getSummaryExcerpt(person);

      return proxy;
   }

   /**
    * Assembles a formatted name consisting of the person's full display name followed by their
    * lifespan. For example, "Reuben Archer Torrey (1856–1928)"
    *
    * @param person Person whose name to format.
    * @return Formatted name and lifespan.
    */
   private String getFormattedName(BiographicalEntry person)
   {
      return MessageFormat.format("{0} ({1}-{2})",
            formatName(person),
            formatYear(person.getBirth()),
            formatYear(person.getDeath()));
   }

   private String formatYear(HistoricalEvent evt)
   {
      LocalDate date = null;
      if (evt != null && evt.getDate() != null)
         date = evt.getDate().getCalendar();

      return (date == null) ? "?" : String.valueOf(date.getYear());
   }

   private String formatName(BiographicalEntry person)
   {
      String displayName = "Name Not Available";
      PersonName name = getDisplayName(person);
      if (name != null) {
         displayName = name.getDisplayName();
         if (displayName == null) {
            displayName = MessageFormat.format("{0} {1}",
                  guardNull(name.getGivenName()),
                  guardNull(name.getFamilyName()));
         }
      }
      return displayName.trim();
   }

   /**
    * Gets a display name for a person using the canonical name by default or the first
    * available alternative name if the canonical name is not supplied.
    */
   private PersonName getDisplayName(BiographicalEntry person)
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
    */
   private String getSummaryExcerpt(BiographicalEntry person)
   {
      String summary = person.getSummary();
      if (summary == null)
         return "";

      // remove HTML tags for sentence extraction
      String summaryStripped = summary.replaceAll("<[^>]+>", "");
      return sentenceParser.apply(summaryStripped);
   }

   private static String guardNull(String value)
   {
      return value == null ? "" : value.trim();
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
