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

import static java.text.MessageFormat.format;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

import java.util.Optional;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.solr.common.SolrInputDocument;

import edu.tamu.tcat.trc.entries.types.reln.Anchor;
import edu.tamu.tcat.trc.entries.types.reln.Relationship;
import edu.tamu.tcat.trc.entries.types.reln.repo.RelationshipRepository;
import edu.tamu.tcat.trc.entries.types.reln.search.RelnSearchProxy;
import edu.tamu.tcat.trc.resolver.EntryId;
import edu.tamu.tcat.trc.resolver.EntryIdDto;
import edu.tamu.tcat.trc.resolver.EntryReference;
import edu.tamu.tcat.trc.resolver.EntryResolverRegistry;
import edu.tamu.tcat.trc.search.solr.impl.TrcDocument;

/**
 *  A data structure for representing the searchable fields associated with a {@link Relationship}.
 */
public class RelnDocument
{
   private final static Logger logger = Logger.getLogger(RelnDocument.class.getName());

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

         doc.set(RelnSolrConfig.SEARCH_PROXY, makeProxy(reln, resolvers));
         
         EntryId entryId = new EntryId(reln.getId(), RelationshipRepository.ENTRY_TYPE_ID);
         
         doc.set(RelnSolrConfig.ENTRY_REFERENCE, resolvers.tokenize(entryId));
         // TODO: Get Entry Reference and add it.
         return doc.build();
      }
      catch (Exception ex)
      {
         throw new IllegalStateException("Failed to construct document to index for relationship" + reln);
      }
   }

   public static RelnSearchProxy makeProxy(Relationship reln, EntryResolverRegistry resolvers)
   {
      EntryId entryId = new EntryId(reln.getId(), RelationshipRepository.ENTRY_TYPE_ID);
      RelnSearchProxy result = new RelnSearchProxy();
      result.id = reln.getId();
      result.token = resolvers.tokenize(entryId);
      result.typeId = reln.getType().getIdentifier();
      result.description = reln.getDescription();

      result.related = reln.getRelatedEntities().stream()
            .map(anchor -> adapt(anchor, resolvers))
            .filter(opt -> opt.isPresent())
            .map(Optional::get)
            .collect(toSet());

      result.targets = reln.getTargetEntities().stream()
            .map(anchor -> adapt(anchor, resolvers))
            .filter(opt -> opt.isPresent())
            .map(Optional::get)
            .collect(toSet());

      return result;
   }

   private static Optional<RelnSearchProxy.Anchor> adapt(Anchor anchor, EntryResolverRegistry resolvers)
   {
      try
      {
         EntryReference<?> reference = resolvers.getReference(anchor.getTarget());

         RelnSearchProxy.Anchor dto = new RelnSearchProxy.Anchor();
         dto.label = reference.getHtmlLabel();
         dto.ref = EntryIdDto.adapt(reference);
         dto.properties = anchor.listProperties().stream()
                  .collect(toMap(Function.identity(), key -> anchor.getProperty(key)));

         return Optional.of(dto);
      }
      catch (Exception ex)
      {
         EntryId target = anchor.getTarget();
         String msg = "Failed to restore relationship anchor. Bad anchor target {0} [type={1}].";
         logger.log(Level.WARNING, format(msg, target.getId(), target.getType()), ex);

         return Optional.empty();
      }
   }
}
