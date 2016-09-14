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
package edu.tamu.tcat.trc.entries.types.article.search.solr;

import static java.text.MessageFormat.format;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;

import edu.tamu.tcat.osgi.config.ConfigurationProperties;
import edu.tamu.tcat.trc.entries.core.repo.EntryRepository;
import edu.tamu.tcat.trc.entries.core.repo.EntryRepositoryRegistry;
import edu.tamu.tcat.trc.entries.core.repo.EntryUpdateRecord;
import edu.tamu.tcat.trc.entries.core.search.SolrSearchMediator;
import edu.tamu.tcat.trc.entries.types.article.Article;
import edu.tamu.tcat.trc.entries.types.article.repo.ArticleRepository;
import edu.tamu.tcat.trc.entries.types.article.search.ArticleQuery;
import edu.tamu.tcat.trc.entries.types.article.search.ArticleQueryCommand;
import edu.tamu.tcat.trc.entries.types.article.search.ArticleSearchResult;
import edu.tamu.tcat.trc.entries.types.article.search.ArticleSearchService;
import edu.tamu.tcat.trc.search.SearchException;
import edu.tamu.tcat.trc.search.solr.DocumentBuilder;
import edu.tamu.tcat.trc.search.solr.impl.BasicIndexService;
import edu.tamu.tcat.trc.search.solr.impl.TrcQueryBuilder;


public class ArticleIndexManagerService implements ArticleSearchService
{
   private final static Logger logger = Logger.getLogger(ArticleIndexManagerService.class.getName());

   /** Configuration property key that defines the URI for the Solr server. */
   public static final String SOLR_API_ENDPOINT = "solr.api.endpoint";

   public static final String SOLR_CORE = "articles";

   private ArticleRepository repo;
   private EntryRepository.ObserverRegistration registration;
   private ConfigurationProperties config;

   private SolrClient solr;

   private BasicIndexService<Article> indexSvc;

   public void setConfiguration(ConfigurationProperties config)
   {
      this.config = config;
   }

   public void setRepoRegistry(EntryRepositoryRegistry registry)
   {
      this.repo = registry.getRepository(null, ArticleRepository.class);
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
      Objects.requireNonNull(repo, "No article repository configured");
      Objects.requireNonNull(config, "No configuration properties provided.");

      // construct Solr core
      BasicIndexService.Builder<Article> indexBuilder = new BasicIndexService.Builder<>(config, SOLR_CORE);
      indexSvc = indexBuilder
                  .setDataAdapter(this::adapt)
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

   private void index(EntryUpdateRecord<Article> ctx)
   {
      SolrSearchMediator.index(indexSvc, ctx);
   }

   private SolrInputDocument adapt(Article article)
   {
      return SearchAdapter.adapt(article);
   }

   public void remove(String articleId)
   {
      try {
         solr.deleteById(articleId);
         solr.commit();
      }
      catch (Exception ex)
      {
         logger.log(Level.SEVERE, ex, () -> format("Failed to remove article {0}", articleId));
      }
   }

   public void postDocument(DocumentBuilder doc) throws SolrServerException, IOException
   {
      // TODO be careful how frequently we commit
      solr.add(Arrays.asList(doc.build()));
      solr.commit();
   }

   /**
    * @since 1.1
    */
   @Override
   public ArticleSearchResult findAll() throws SearchException
   {
      ArticleQueryCommand query = createQuery();
      return query.execute();
   }

   /**
    * @since 1.1
    */
   @Override
   public ArticleSearchResult search(String query) throws SearchException
   {
      ArticleQueryCommand qCmd = createQuery();
      qCmd.setQuery(query);
      return qCmd.execute();
   }

   /**
    * @since 1.1
    */
   @Override
   public ArticleQueryCommand createQuery() throws SearchException
   {
      return createQuery(new ArticleQuery());
   }

   /**
    * @since 1.1
    */
   @Override
   public ArticleQueryCommand createQuery(ArticleQuery query) throws SearchException
   {

      TrcQueryBuilder builder = new TrcQueryBuilder(new ArticleSolrConfig());
      return new ArticleSolrQueryCmd(solr, query, builder);
   }

   /**
    * @since 1.1
    */
   @Override
   public ArticleSearchResult next(ArticleQuery query) throws SearchException
   {
      ArticleQueryCommand cmd = createQuery(query);
      cmd.setOffset(query.offset + query.max);
      return cmd.execute();
   }

   /**
    * @since 1.1
    */
   @Override
   public ArticleSearchResult previous(ArticleQuery query) throws SearchException
   {
      ArticleQueryCommand cmd = createQuery(query);
      int offset = Math.min(0,  query.offset - query.max);
      cmd.setOffset(offset);
      return cmd.execute();
   }

   /**
    * @since 1.1
    */
   @Override
   public ArticleSearchResult page(ArticleQuery query, int pg) throws SearchException
   {
      ArticleQueryCommand cmd = createQuery(query);
      cmd.setOffset(query.max * pg);
      return cmd.execute();
   }
}
