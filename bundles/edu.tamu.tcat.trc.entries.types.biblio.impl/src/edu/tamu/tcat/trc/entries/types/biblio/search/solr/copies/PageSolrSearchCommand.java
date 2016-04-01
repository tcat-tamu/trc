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
package edu.tamu.tcat.trc.entries.types.biblio.search.solr.copies;

import java.util.List;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

import edu.tamu.tcat.trc.entries.types.biblio.copies.search.PageSearchCommand;
import edu.tamu.tcat.trc.entries.types.biblio.copies.search.PageSearchProxy;
import edu.tamu.tcat.trc.entries.types.biblio.copies.search.PageSearchResult;
import edu.tamu.tcat.trc.search.SearchException;
import edu.tamu.tcat.trc.search.solr.impl.TrcQueryBuilder;

public class PageSolrSearchCommand implements PageSearchCommand
{
   private static final int DEFAULT_MAX_RESULTS = 25;

   private SolrServer solr;
   private TrcQueryBuilder qb;

   public PageSolrSearchCommand(SolrServer solrPages, TrcQueryBuilder qb)
   {
      solr = solrPages;
      this.qb = qb;
      this.qb.max(DEFAULT_MAX_RESULTS);
   }

   @Override
   public PageSearchResult execute() throws SearchException
   {
      // TODO need to supply volume id and page number.
      try
      {
         QueryResponse response = solr.query(qb.get());
         SolrDocumentList results = response.getResults();
         List<PageSearchProxy> page = qb.unpack(results, PageSolrSearchCommand::proxyAdapter);
         return new SolrPageResults(this, page);
      }
      catch (SolrServerException e)
      {
         throw new SearchException("An error occurred while querying the volume core", e);
      }
   }

   private static PageSearchProxy proxyAdapter(SolrDocument doc)
   {
      PageSearchProxy proxy = new PageSearchProxy();
      proxy.id = doc.getFieldValue("pageText").toString();
      return proxy;
   }

   @Override
   public void query(String basicQueryString) throws SearchException
   {
      qb.basic(basicQueryString);
   }

   @Override
   public void addVolumeFilter(String volumeId) throws SearchException
   {
      qb.filter(FullTextPageConfig.ID, volumeId);
   }

   @Override
   public void setOffset(int offset)
   {
      if (offset < 0)
         throw new IllegalArgumentException("Offset [" + offset + "] cannot be negative.");
      qb.offset(offset);
   }

   @Override
   public void setMaxResults(int count)
   {
      qb.max(count);
   }

}
