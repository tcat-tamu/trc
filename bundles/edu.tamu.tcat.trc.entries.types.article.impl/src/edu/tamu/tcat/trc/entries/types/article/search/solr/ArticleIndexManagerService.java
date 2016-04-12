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

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.common.SolrInputDocument;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.tamu.tcat.osgi.config.ConfigurationProperties;
import edu.tamu.tcat.trc.entries.types.article.Article;
import edu.tamu.tcat.trc.entries.types.article.repo.ArticleChangeEvent;
import edu.tamu.tcat.trc.entries.types.article.repo.ArticleRepository;
import edu.tamu.tcat.trc.entries.types.article.search.ArticleQuery;
import edu.tamu.tcat.trc.entries.types.article.search.ArticleQueryCommand;
import edu.tamu.tcat.trc.entries.types.article.search.ArticleSearchResult;
import edu.tamu.tcat.trc.entries.types.article.search.ArticleSearchService;
import edu.tamu.tcat.trc.search.SearchException;
import edu.tamu.tcat.trc.search.solr.impl.TrcQueryBuilder;


public class ArticleIndexManagerService implements ArticleSearchService
{
   private final static Logger logger = Logger.getLogger(ArticleIndexManagerService.class.getName());

   /** Configuration property key that defines the URI for the Solr server. */
   public static final String SOLR_API_ENDPOINT = "solr.api.endpoint";

   public static final String SOLR_CORE = "trc.articles.solr.core";

   static final ObjectMapper mapper;
   static
   {
      mapper = new ObjectMapper();
      mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
   }

   private ArticleRepository repo;

   private SolrClient solr;
   private ConfigurationProperties config;

   private AutoCloseable listenerReg;

   public void setArticleRepo(ArticleRepository repo)
   {
      this.repo = repo;
   }

   public void setConfiguration(ConfigurationProperties config)
   {
      this.config = config;
   }

   public void activate()
   {
      listenerReg = repo.register(this::onArticleChange);

      // construct Solr core
      URI solrBaseUri = config.getPropertyValue(SOLR_API_ENDPOINT, URI.class);
      String solrCore = config.getPropertyValue(SOLR_CORE, String.class);

      URI coreUri = solrBaseUri.resolve(solrCore);
      logger.info("Connecting to Solr Service [" + coreUri + "]");

      solr = new HttpSolrClient(coreUri.toString());
   }

   public void dispose()
   {
      try
      {
         if (listenerReg != null)
            listenerReg.close();
      }
      catch (Exception ex)
      {
         logger.log(Level.WARNING, "Failed to unregisters article repository listener.", ex);
      }

      listenerReg = null;

      try
      {
         solr.close();
      }
      catch (Exception ex)
      {
         logger.log(Level.WARNING, "Failed to shutdown solr server client for article index manager", ex);
      }
   }

   private void onArticleChange(ArticleChangeEvent evt)
   {
      String articleId = evt.getEntityId();

      try
      {
         Article article;
         switch (evt.getUpdateAction())
         {
            case CREATE:
               article = repo.get(UUID.fromString(articleId));
               postDocument(ArticleDocument.create(article));
               break;
            case UPDATE:
               article = repo.get(UUID.fromString(articleId));
               postDocument(ArticleDocument.update(article));
               break;
            case DELETE:
               solr.deleteById(articleId);
               solr.commit();
               break;
            default:
               logger.log(Level.INFO, "Unexpected article change event " + evt);
         }
      }
      catch (Exception ex)
      {
         logger.log(Level.SEVERE, "Failed to update search indices following a change to article: " + articleId, ex);
      }
   }

   private void postDocument(ArticleDocument doc) throws SolrServerException, IOException
   {
      Collection<SolrInputDocument> solrDocs = new ArrayList<>();
      solrDocs.add(doc.getDocument());
      solr.add(solrDocs);
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
