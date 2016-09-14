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

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.common.SolrInputDocument;

import edu.tamu.tcat.osgi.config.ConfigurationProperties;
import edu.tamu.tcat.trc.entries.core.repo.EntryRepository;
import edu.tamu.tcat.trc.entries.core.repo.EntryRepositoryRegistry;
import edu.tamu.tcat.trc.entries.core.repo.EntryUpdateRecord;
import edu.tamu.tcat.trc.entries.core.search.SolrSearchMediator;
import edu.tamu.tcat.trc.entries.types.bio.Person;
import edu.tamu.tcat.trc.entries.types.bio.repo.PeopleRepository;
import edu.tamu.tcat.trc.entries.types.bio.search.PeopleIndexServiceManager;
import edu.tamu.tcat.trc.entries.types.bio.search.PeopleSearchService;
import edu.tamu.tcat.trc.search.SearchException;
import edu.tamu.tcat.trc.search.solr.impl.BasicIndexService;
import edu.tamu.tcat.trc.search.solr.impl.TrcQueryBuilder;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;

public class PeopleIndexingService implements PeopleIndexServiceManager, PeopleSearchService
{
   private static final String OPENNLP_MODELS_SENTENCE_PATH = "opennlp.models.sentence.path";

   private final static Logger logger = Logger.getLogger(PeopleIndexingService.class.getName());

   /** Configuration property key that defines the URI for the Solr server. */
   public static final String SOLR_API_ENDPOINT = "solr.api.endpoint";

   /** Configuration property key that defines Solr core to be used for relationships. */
   public static final String SOLR_CORE = "catalogentries.authors.solr.core";


   private PeopleRepository repo;
   private EntryRepository.ObserverRegistration registration;
   private ConfigurationProperties config;

   private SolrClient solr;

   private BasicIndexService<Person> indexSvc;

   public void setConfiguration(ConfigurationProperties config)
   {
      this.config = config;
   }

   public void setRepoRegistry(EntryRepositoryRegistry registry)
   {
      this.repo = registry.getRepository(null, PeopleRepository.class);
   }

   public void activate()
   {
      logger.info("Activating " + getClass().getSimpleName());

      try {
         doActivation();
         logger.fine("Activated " + getClass().getSimpleName());
      } catch (Exception ex) {
         logger.log(Level.SEVERE, "Failed to activate" + getClass().getSimpleName(), ex);
         throw ex;
      }
   }

   private void doActivation()
   {
      Objects.requireNonNull(repo, "No relationship repository configured");
      Objects.requireNonNull(config, "No configuration properties provided.");

      // construct Solr core
      BasicIndexService.Builder<Person> indexBuilder = new BasicIndexService.Builder<>(config, SOLR_CORE);
      indexSvc = indexBuilder.setDataAdapter(p -> create(p, this::extractFirstSentence))
                  .setIdProvider(entry -> entry.getId())
                  .build();

      registration = repo.onUpdate(this::index);
      this.solr = indexSvc.getSolrClient();
   }

   public void deactivate()
   {
      logger.info("Deactivating " + getClass().getSimpleName());
      if (registration != null)
         registration.close();

      registration = null;
   }

   public static SolrInputDocument create(Person person, Function<String, String> sentenceParser)
   {
      SolrDocAdapter adapter = new SolrDocAdapter(sentenceParser);
      return adapter.apply(person);
   }

   private void index(EntryUpdateRecord<Person> ctx)
   {
      SolrSearchMediator.index(indexSvc, ctx);
   }

   @Override
   public PeopleSolrQueryCommand createQueryCommand() throws SearchException
   {
      TrcQueryBuilder builder = new TrcQueryBuilder(new BioSolrConfig());
      return new PeopleSolrQueryCommand(solr, builder);
   }

   public void setRepo(PeopleRepository repo)
   {
      this.repo = repo;
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
      // TODO this should be generalized
      if (text == null || text.trim().isEmpty())
         return "";

      try
      {
         Path sentenceModelPath = config.getPropertyValue(OPENNLP_MODELS_SENTENCE_PATH, Path.class, null);
         return (sentenceModelPath != null)
                  ? extractSentenceWithNLP(text, sentenceModelPath)
                  : extractSentenceWithRegex(text);
      }
      catch (IOException e)
      {
         logger.log(Level.SEVERE, "Unable to open sentence detect model input file", e);
      }


      logger.log(Level.WARNING, "Falling back to simple sentence parsing.");
      return extractFirstSentence(text);
   }

   private String extractSentenceWithRegex(String text)
   {
      int ix = text.indexOf(".");
      if (ix < 0)
         ix = text.length();

      return text.substring(0, Math.min(ix, 140));
   }

   private String extractSentenceWithNLP(String text, Path sentenceModelPath) throws IOException
   {
      try (InputStream modelInput = Files.newInputStream(sentenceModelPath))
      {
         SentenceModel sentenceModel = new SentenceModel(modelInput);
         SentenceDetectorME detector = new SentenceDetectorME(sentenceModel);
         String[] summarySentences = detector.sentDetect(text);
         return (summarySentences.length == 0) ? null : summarySentences[0];
      }

   }

}
