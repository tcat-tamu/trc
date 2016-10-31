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
package edu.tamu.tcat.trc.entries.types.reln.impl.search;

import static java.util.stream.Collectors.toSet;

import java.util.Set;

import org.apache.solr.common.SolrInputDocument;

import edu.tamu.tcat.trc.entries.types.reln.Anchor;
import edu.tamu.tcat.trc.entries.types.reln.Relationship;
import edu.tamu.tcat.trc.entries.types.reln.search.RelnSearchProxy;
import edu.tamu.tcat.trc.resolver.EntryResolverRegistry;
import edu.tamu.tcat.trc.search.solr.impl.TrcDocument;

/**
 *  A data structure for representing the searchable fields associated with a {@link Relationship}.
 */
public class RelnDocument
{
   // composed instead of extended to not expose TrcDocument as API to this class
   private TrcDocument indexDocument;

   public RelnDocument()
   {
      indexDocument = new TrcDocument(new RelnSolrConfig());
   }

   public SolrInputDocument getDocument()
   {
      return indexDocument.build();
   }

   public static SolrInputDocument create(Relationship reln, EntryResolverRegistry resolvers)
   {
      TrcDocument doc = new TrcDocument(new RelnSolrConfig());
      try
      {

         doc.set(RelnSolrConfig.ID, reln.getId());
         doc.set(RelnSolrConfig.DESCRIPTION, reln.getDescription());
         doc.set(RelnSolrConfig.REL_TYPE, reln.getType().getIdentifier());

         reln.getRelatedEntities().stream()
               .map(Anchor::getTarget)
               .map(resolvers::tokenize)
               .forEach(token -> doc.set(RelnSolrConfig.RELATED_ENTITIES, token));
         reln.getTargetEntities().stream()
               .map(Anchor::getTarget)
               .map(resolvers::tokenize)
               .forEach(token -> doc.set(RelnSolrConfig.TARGET_ENTITIES, token));

         doc.set(RelnSolrConfig.SEARCH_PROXY, RelnSearchProxy.create(reln, resolvers));

         // TODO: Get Entry Reference and add it.
         return doc.build();
      }
      catch (Exception ex)
      {
         throw new IllegalStateException("Failed to construct document to index for relationship" + reln);
      }
   }

   public static SolrInputDocument update(Relationship reln, EntryResolverRegistry resolvers)
   {
      TrcDocument doc = new TrcDocument(new RelnSolrConfig());
      try
      {

         doc.set(RelnSolrConfig.ID, reln.getId());
         doc.update(RelnSolrConfig.DESCRIPTION, reln.getDescription());
         doc.update(RelnSolrConfig.REL_TYPE, reln.getType().getIdentifier());

         Set<String> related = reln.getRelatedEntities().stream()
            .map(Anchor::getTarget)
            .map(resolvers::tokenize)
            .collect(toSet());
         Set<String> targets = reln.getTargetEntities().stream()
            .map(Anchor::getTarget)
            .map(resolvers::tokenize)
            .collect(toSet());

         doc.update(RelnSolrConfig.RELATED_ENTITIES, related);
         doc.update(RelnSolrConfig.TARGET_ENTITIES, targets);

         doc.update(RelnSolrConfig.SEARCH_PROXY, RelnSearchProxy.create(reln, resolvers));

         // TODO: Get Entry Reference and add it.
         return doc.build();
      }
      catch (Exception ex)
      {
         throw new IllegalStateException("Failed to construct document to index for relationship" + reln);
      }
   }
}
