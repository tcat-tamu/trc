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
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.common.SolrInputDocument;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.tamu.tcat.osgi.config.ConfigurationProperties;
import edu.tamu.tcat.trc.entries.notification.UpdateListener;
import edu.tamu.tcat.trc.entries.types.article.Article;
import edu.tamu.tcat.trc.entries.types.article.repo.ArticleChangeEvent;
import edu.tamu.tcat.trc.entries.types.article.repo.ArticleRepository;


public class ArticleIndexManagerService
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

   private SolrServer solr;
   private ConfigurationProperties config;

   private AutoCloseable register;

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
      register = repo.register(new ArticleUpdateListener());

      // construct Solr core
      URI solrBaseUri = config.getPropertyValue(SOLR_API_ENDPOINT, URI.class);
      String solrCore = config.getPropertyValue(SOLR_CORE, String.class);

      URI coreUri = solrBaseUri.resolve(solrCore);
      logger.info("Connecting to Solr Service [" + coreUri + "]");

      solr = new HttpSolrServer(coreUri.toString());
   }

   public void dispose()
   {
      try
      {
         if (register != null)
            register.close();
      }
      catch (Exception ex)
      {
         logger.log(Level.WARNING, "Failed to unregisters article repository listener.", ex);
      }

      register = null;

      try
      {
         solr.shutdown();
      }
      catch (Exception ex)
      {
         logger.log(Level.WARNING, "Failed to shutdown solr server client for article index manager", ex);
      }
   }

   private void onEvtChange(ArticleChangeEvent evt)
   {
      try
      {
         switch (evt.getUpdateAction())
         {
            case CREATE:
               onCreate(evt.getArticle());
               break;
            case UPDATE:
               onUpdate(evt.getArticle());
               break;
            case DELETE:
               onDelete(evt.getEntityId());
               break;
            default:
               logger.log(Level.INFO, "Unexpected article change event " + evt);
         }
      }
      catch (Exception ex)
      {
         logger.log(Level.WARNING, "Failed to update search indices following a change to article: " + evt, ex);
      }
   }

   private void onCreate(Article article)
   {
      try
      {
         ArticleDocument proxy = ArticleDocument.create(article);
         postDocument(proxy);
      }
      catch (SolrServerException | IOException e)
      {
         logger.log(Level.SEVERE, "Failed to adapt Article to indexable data transfer objects for article id: [" + article.getId() + "]", e);
         return;
      }
      catch (Exception e)
      {
         logger.log(Level.SEVERE, "Failed to adapt Article to indexable data transfer objects for article id: [" + article.getId() + "]", e);
         return;
      }
   }

   private void onUpdate(Article article)
   {
      try
      {
         ArticleDocument proxy = ArticleDocument.update(article);
         postDocument(proxy);
      }
      catch (SolrServerException | IOException e)
      {
         logger.log(Level.SEVERE, "Failed to adapt Article to indexable data transfer objects for article id: [" + article.getId() + "]", e);
         return;
      }
      catch (Exception e)
      {
         logger.log(Level.SEVERE, "Failed to adapt Article to indexable data transfer objects for article id: [" + article.getId() + "]", e);
         return;
      }
   }

   private void postDocument(ArticleDocument doc) throws SolrServerException, IOException
   {
      Collection<SolrInputDocument> solrDocs = new ArrayList<>();
      solrDocs.add(doc.getDocument());
      solr.add(solrDocs);
      solr.commit();
   }

   private void onDelete(String id)
   {
      try
      {
         solr.deleteById(id);
         solr.commit();
      }
      catch (SolrServerException | IOException e)
      {
         logger.log(Level.SEVERE, "Failed to commit the article id: [" + id + "] to the SOLR server. " + e);
      }
   }

   private class ArticleUpdateListener implements UpdateListener<ArticleChangeEvent>
   {
      @Override
      public void handle(ArticleChangeEvent evt)
      {
         onEvtChange(evt);
      }
   }
}
