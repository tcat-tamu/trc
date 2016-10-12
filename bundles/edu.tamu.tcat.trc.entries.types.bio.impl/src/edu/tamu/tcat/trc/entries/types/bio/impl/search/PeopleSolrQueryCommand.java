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
package edu.tamu.tcat.trc.entries.types.bio.impl.search;

import java.util.List;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;

import edu.tamu.tcat.trc.entries.types.bio.search.BioSearchProxy;
import edu.tamu.tcat.trc.entries.types.bio.search.BioEntryQueryCommand;
import edu.tamu.tcat.trc.search.solr.SearchException;
import edu.tamu.tcat.trc.search.solr.impl.TrcQueryBuilder;

public class PeopleSolrQueryCommand implements BioEntryQueryCommand
{
   private static final int DEFAULT_MAX_RESULTS = 25;

   private final SolrClient solr;
   private final TrcQueryBuilder qb;

   public PeopleSolrQueryCommand(SolrClient solr, TrcQueryBuilder qb)
   {
      this.solr = solr;
      this.qb = qb;
      qb.max(DEFAULT_MAX_RESULTS);
   }

   @Override
   public SolrPersonResults executeSync() throws SearchException
   {
      try
      {
         QueryResponse response = solr.query(qb.get());
         SolrDocumentList results = response.getResults();

         List<BioSearchProxy> people = qb.unpack(results, BioSolrConfig.SEARCH_PROXY);
         return new SolrPersonResults(this, people);
      }
      catch (Exception e)
      {
         throw new SearchException("An error occurred while querying the author core: " + e, e);
      }
   }

   @Override
   public void query(String q) throws SearchException
   {
      qb.basic(q);
   }

   @Override
   public void queryAll() throws SearchException
   {
      qb.basic("*:*");
   }

   @Override
   public void queryFamilyName(String familyName) throws SearchException
   {
      // Add quotes so each term acts as a literal
      qb.query(BioSolrConfig.FAMILY_NAME, '"' + familyName + '"');
   }

   @Override
   public void setOffset(int start)
   {
      if (start < 0)
         throw new IllegalArgumentException("Offset ["+start+"] cannot be negative");

      qb.offset(start);
   }

   @Override
   public void setMaxResults(int max)
   {
      qb.max(max);
   }
}
