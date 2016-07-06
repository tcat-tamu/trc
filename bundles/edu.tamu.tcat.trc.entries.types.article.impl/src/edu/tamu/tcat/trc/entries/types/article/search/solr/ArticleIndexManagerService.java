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
import java.net.URI;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;

import edu.tamu.tcat.osgi.config.ConfigurationProperties;
import edu.tamu.tcat.trc.entries.types.article.search.ArticleQuery;
import edu.tamu.tcat.trc.entries.types.article.search.ArticleQueryCommand;
import edu.tamu.tcat.trc.entries.types.article.search.ArticleSearchResult;
import edu.tamu.tcat.trc.entries.types.article.search.ArticleSearchService;
import edu.tamu.tcat.trc.search.SearchException;
import edu.tamu.tcat.trc.search.solr.impl.TrcDocument;
import edu.tamu.tcat.trc.search.solr.impl.TrcQueryBuilder;


public class ArticleIndexManagerService implements ArticleSearchService
{
   private final static Logger logger = Logger.getLogger(ArticleIndexManagerService.class.getName());

   /** Configuration property key that defines the URI for the Solr server. */
   public static final String SOLR_API_ENDPOINT = "solr.api.endpoint";

   public static final String SOLR_CORE = "trc.articles.solr.core";

   // TODO wrap this in a solr core config an inject via DS
   private SolrClient solr;
   private ConfigurationProperties config;


   public void setConfiguration(ConfigurationProperties config)
   {
      this.config = config;
   }

   public void activate()
   {
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
         solr.close();
      }
      catch (Exception ex)
      {
         logger.log(Level.WARNING, "Failed to shutdown solr server client for article index manager", ex);
      }
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

   public void postDocument(TrcDocument doc) throws SolrServerException, IOException
   {
      // TODO be careful how frequently we commit
      solr.add(Arrays.asList(doc.getSolrDocument()));
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
