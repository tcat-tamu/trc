package edu.tamu.tcat.trc.entries.types.reln.search.solr;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.solr.common.SolrInputDocument;

import edu.tamu.tcat.trc.entries.search.SearchException;
import edu.tamu.tcat.trc.entries.search.solr.SolrIndexField;
import edu.tamu.tcat.trc.entries.search.solr.impl.TrcDocument;
import edu.tamu.tcat.trc.entries.types.reln.Relationship;
import edu.tamu.tcat.trc.entries.types.reln.dto.AnchorDV;
import edu.tamu.tcat.trc.entries.types.reln.dto.ProvenanceDV;
import edu.tamu.tcat.trc.entries.types.reln.dto.RelationshipDV;
import edu.tamu.tcat.trc.entries.types.reln.search.RelnSearchProxy;

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
      return indexDocument.getSolrDocument();
   }

   public static RelnDocument create(Relationship reln) throws SearchException
   {
      RelnDocument doc = new RelnDocument();
      RelationshipDV relnDV = RelationshipDV.create(reln);

      doc.indexDocument.set(RelnSolrConfig.ID, relnDV.id);
      doc.indexDocument.set(RelnSolrConfig.DESCRIPTION, relnDV.description);
      doc.indexDocument.set(RelnSolrConfig.DESCRIPTION_MIME_TYPE, relnDV.descriptionMimeType);
      doc.indexDocument.set(RelnSolrConfig.REL_TYPE, relnDV.typeId);
      doc.addEntities(RelnSolrConfig.RELATED_ENTITIES, relnDV.relatedEntities);
      doc.addEntities(RelnSolrConfig.TARGET_ENTITIES, relnDV.targetEntities);
      doc.addProvenance(relnDV.provenance);

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

   public static RelnDocument update(Relationship reln) throws SearchException
   {
      RelnDocument doc = new RelnDocument();
      RelationshipDV relnDV = RelationshipDV.create(reln);

      doc.indexDocument.update(RelnSolrConfig.ID, relnDV.id);

      doc.indexDocument.update(RelnSolrConfig.DESCRIPTION, relnDV.description);
      doc.indexDocument.update(RelnSolrConfig.DESCRIPTION_MIME_TYPE, relnDV.descriptionMimeType);
      doc.indexDocument.update(RelnSolrConfig.REL_TYPE, relnDV.typeId);
      doc.updateEntities(RelnSolrConfig.RELATED_ENTITIES, relnDV.relatedEntities);
      doc.updateEntities(RelnSolrConfig.TARGET_ENTITIES, relnDV.targetEntities);
      doc.updateProvenance(relnDV.provenance);

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

   private void addEntities(SolrIndexField<String> field, Set<AnchorDV> anchors) throws SearchException
   {
      for (AnchorDV anchor : anchors)
      {
         for (String uri : anchor.entryUris)
         {
            indexDocument.set(field, uri);
         }
      }
   }

   private void updateEntities(SolrIndexField<String> field, Set<AnchorDV> anchors) throws SearchException
   {
      Set<String> allEntities = new HashSet<>();

      for (AnchorDV anchor : anchors)
      {
         allEntities.addAll(anchor.entryUris);
      }

      indexDocument.update(field, allEntities);
   }

   private void addProvenance(ProvenanceDV prov) throws SearchException
   {
      Set<String> uris = new HashSet<>();
      for (String uri : uris)
         indexDocument.set(RelnSolrConfig.PROV_CREATORS, uri);

      if (prov.dateCreated != null)
         indexDocument.set(RelnSolrConfig.PROV_CREATED_DATE, LocalDate.from(DateTimeFormatter.ISO_DATE.parse(prov.dateCreated)));
      if (prov.dateModified != null)
         indexDocument.set(RelnSolrConfig.PROV_MODIFIED_DATE, LocalDate.from(DateTimeFormatter.ISO_DATE.parse(prov.dateModified)));
   }

   private void updateProvenance(ProvenanceDV prov) throws SearchException
   {
      indexDocument.update(RelnSolrConfig.PROV_CREATORS, prov.creatorUris);
      if (prov.dateCreated != null)
         indexDocument.update(RelnSolrConfig.PROV_CREATED_DATE, LocalDate.from(DateTimeFormatter.ISO_DATE.parse(prov.dateCreated)));
      if (prov.dateModified != null)
         indexDocument.update(RelnSolrConfig.PROV_MODIFIED_DATE, LocalDate.from(DateTimeFormatter.ISO_DATE.parse(prov.dateModified)));
   }
}
