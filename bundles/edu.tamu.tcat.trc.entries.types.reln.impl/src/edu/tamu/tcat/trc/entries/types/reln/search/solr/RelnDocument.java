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
package edu.tamu.tcat.trc.entries.types.reln.search.solr;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;

import org.apache.solr.common.SolrInputDocument;

import edu.tamu.tcat.trc.entries.types.reln.Relationship;
import edu.tamu.tcat.trc.entries.types.reln.dto.AnchorDTO;
import edu.tamu.tcat.trc.entries.types.reln.dto.ProvenanceDTO;
import edu.tamu.tcat.trc.entries.types.reln.dto.RelationshipDTO;
import edu.tamu.tcat.trc.entries.types.reln.search.RelnSearchProxy;
import edu.tamu.tcat.trc.search.SearchException;
import edu.tamu.tcat.trc.search.solr.SolrIndexField;
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
      return indexDocument.getSolrDocument();
   }

   public static RelnDocument create(Relationship reln)
   {
      RelnDocument doc = new RelnDocument();
      try
      {
         RelationshipDTO relnDV = RelationshipDTO.create(reln);

         doc.indexDocument.set(RelnSolrConfig.ID, relnDV.id);
         doc.indexDocument.set(RelnSolrConfig.DESCRIPTION, relnDV.description);
         doc.indexDocument.set(RelnSolrConfig.DESCRIPTION_MIME_TYPE, relnDV.descriptionMimeType);
         doc.indexDocument.set(RelnSolrConfig.REL_TYPE, relnDV.typeId);
         doc.addEntities(RelnSolrConfig.RELATED_ENTITIES, relnDV.relatedEntities);
         doc.addEntities(RelnSolrConfig.TARGET_ENTITIES, relnDV.targetEntities);
         doc.addProvenance(relnDV.provenance);
      }
      catch (SearchException ex)
      {
         throw new IllegalStateException("Failed to construct document to index for relationship" + reln);
      }

      try
      {
         doc.indexDocument.set(RelnSolrConfig.SEARCH_PROXY, RelnSearchProxy.create(reln));
      }
      catch (Exception e)
      {
         throw new IllegalStateException("Failed to serialize Relationship Search Proxy data", e);
      }

      return doc;
   }

   public static RelnDocument update(Relationship reln)
   {
      RelnDocument doc = new RelnDocument();
      RelationshipDTO relnDV = RelationshipDTO.create(reln);
      try
      {
         doc.indexDocument.set(RelnSolrConfig.ID, relnDV.id);

         doc.indexDocument.update(RelnSolrConfig.DESCRIPTION, relnDV.description);
         doc.indexDocument.update(RelnSolrConfig.DESCRIPTION_MIME_TYPE, relnDV.descriptionMimeType);
         doc.indexDocument.update(RelnSolrConfig.REL_TYPE, relnDV.typeId);
         doc.updateEntities(RelnSolrConfig.RELATED_ENTITIES, relnDV.relatedEntities);
         doc.updateEntities(RelnSolrConfig.TARGET_ENTITIES, relnDV.targetEntities);
         doc.updateProvenance(relnDV.provenance);
      }
      catch (SearchException ex)
      {
         throw new IllegalStateException("Failed to construct document to index for relationship" + reln);
      }

      try
      {
         doc.indexDocument.update(RelnSolrConfig.SEARCH_PROXY, RelnSearchProxy.create(reln));
      }
      catch (Exception e)
      {
         throw new IllegalStateException("Failed to serialize Relationship Search Proxy data", e);
      }

      return doc;
   }

   private void addEntities(SolrIndexField<String> field, Set<AnchorDTO> anchors) throws SearchException
   {
      for (AnchorDTO anchor : anchors)
      {
         for (String uri : anchor.entryUris)
         {
            indexDocument.set(field, uri);
         }
      }
   }

   private void updateEntities(SolrIndexField<String> field, Set<AnchorDTO> anchors) throws SearchException
   {
      Set<String> allEntities = new HashSet<>();

      for (AnchorDTO anchor : anchors)
      {
         allEntities.addAll(anchor.entryUris);
      }

      indexDocument.update(field, allEntities);
   }

   private void addProvenance(ProvenanceDTO prov) throws SearchException
   {
      Set<String> uris = new HashSet<>();
      for (String uri : uris)
         indexDocument.set(RelnSolrConfig.PROV_CREATORS, uri);

      if (prov.dateCreated != null)
         indexDocument.set(RelnSolrConfig.PROV_CREATED_DATE, Instant.from(DateTimeFormatter.ISO_INSTANT.parse(prov.dateCreated)));
      if (prov.dateModified != null)
         indexDocument.set(RelnSolrConfig.PROV_MODIFIED_DATE, Instant.from(DateTimeFormatter.ISO_INSTANT.parse(prov.dateModified)));
   }

   private void updateProvenance(ProvenanceDTO prov) throws SearchException
   {
      indexDocument.update(RelnSolrConfig.PROV_CREATORS, prov.creatorUris);
      if (prov.dateCreated != null)
         indexDocument.update(RelnSolrConfig.PROV_CREATED_DATE, Instant.from(DateTimeFormatter.ISO_INSTANT.parse(prov.dateCreated)));
      if (prov.dateModified != null)
         indexDocument.update(RelnSolrConfig.PROV_MODIFIED_DATE, Instant.from(DateTimeFormatter.ISO_INSTANT.parse(prov.dateModified)));
   }
}
