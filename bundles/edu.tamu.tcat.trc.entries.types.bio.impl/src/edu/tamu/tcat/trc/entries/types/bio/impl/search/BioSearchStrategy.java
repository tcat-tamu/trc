package edu.tamu.tcat.trc.entries.types.bio.impl.search;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.common.SolrInputDocument;

import edu.tamu.tcat.osgi.config.ConfigurationProperties;
import edu.tamu.tcat.trc.entries.types.bio.BiographicalEntry;
import edu.tamu.tcat.trc.entries.types.bio.search.BioEntryQueryCommand;
import edu.tamu.tcat.trc.search.solr.IndexServiceStrategy;
import edu.tamu.tcat.trc.search.solr.SearchException;
import edu.tamu.tcat.trc.search.solr.SolrIndexConfig;
import edu.tamu.tcat.trc.search.solr.impl.TrcQueryBuilder;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;

public class BioSearchStrategy implements IndexServiceStrategy<BiographicalEntry, BioEntryQueryCommand>
{
   private final static Logger logger = Logger.getLogger(BioSearchStrategy.class.getName());

   // TODO generalized sentence parser
   public static final String OPENNLP_MODELS_SENTENCE_PATH = "opennlp.models.sentence.path";

   public static final String SOLR_CORE = "biographical";

   private final SentenceDetectorME detector;

   public BioSearchStrategy(ConfigurationProperties config)
   {
      this.detector = initSentenceDetector(config);
   }

   private SentenceDetectorME initSentenceDetector(ConfigurationProperties config)
   {
      SentenceDetectorME detector;
      Path sentenceModelPath = config.getPropertyValue(OPENNLP_MODELS_SENTENCE_PATH, Path.class, null);
      if (sentenceModelPath == null)
         return null;

      try (InputStream modelInput = Files.newInputStream(sentenceModelPath))
      {
         SentenceModel sentenceModel = new SentenceModel(modelInput);
         detector = new SentenceDetectorME(sentenceModel);
      }
      catch (IOException e)
      {
         detector = null;
         logger.log(Level.SEVERE, "Unable to open sentence detect model input file", e);
      }

      return detector;
   }

   @Override
   public Class<BiographicalEntry> getType()
   {
      return BiographicalEntry.class;
   }

   @Override
   public String getCoreId()
   {
      return SOLR_CORE;
   }

   @Override
   public SolrIndexConfig getIndexCofig()
   {
      return new BioSolrConfig();
   }

   @Override
   public SolrInputDocument getDocument(BiographicalEntry person)
   {
      SolrDocAdapter adapter = new SolrDocAdapter(this::extractFirstSentence);
      return adapter.apply(person);
   }

   @Override
   public String getEntryId(BiographicalEntry entry)
   {
      return entry.getId();
   }

   @Override
   public PeopleSolrQueryCommand createQuery(SolrClient solr)
   {
      try
      {
         TrcQueryBuilder builder = new TrcQueryBuilder(getIndexCofig());
         return new PeopleSolrQueryCommand(solr, builder);
      }
      catch (SearchException ex)
      {
         throw new IllegalStateException("Failed to construct query builder", ex);
      }
   }

   /**
    * Gets the first sentence from a string of English text,
    * or {@code null} if the string cannot be parsed.
    *
    * @param text
    * @return
    */
   private String extractFirstSentence(String text)
   {
      if (text == null || text.trim().isEmpty())
         return "";

      return (detector != null)
            ? extractSentenceWithNLP(text)
            : extractSentenceWithRegex(text);
   }

   private String extractSentenceWithRegex(String text)
   {
      int ix = text.indexOf(".");
      if (ix < 0)
         ix = text.length();

      return text.substring(0, Math.min(ix, 140));
   }

   private String extractSentenceWithNLP(String text)
   {
      String[] summarySentences = detector.sentDetect(text);
      return (summarySentences.length == 0) ? "" : summarySentences[0];
   }

}
