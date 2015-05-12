package edu.tamu.tcat.trc.entries.reln.solr;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.solr.common.SolrInputDocument;

import com.fasterxml.jackson.core.JsonProcessingException;

import edu.tamu.tcat.trc.entries.reln.Relationship;
import edu.tamu.tcat.trc.entries.reln.model.AnchorDV;
import edu.tamu.tcat.trc.entries.reln.model.ProvenanceDV;
import edu.tamu.tcat.trc.entries.reln.model.RelationshipDV;

/**
 *  A data structure for representing the searchable fields associated with a {@link Relationship}.
 */
public class RelnSolrProxy
{

   // NOTE this is internal to the Solr search service. Probably/possibly add helper methods to retrieve
   //      the reln (using the repo owned by the service) and other data structures as needed.

   // Solr field names
   private final static String relnId = "id";
   private final static String description = "description";
   private final static String descriptMimeType = "descriptionMimeType";
   private final static String relationshipType = "relationshipType";
   private final static String relatedEntities = "relatedEntities";
   private final static String targetEntities = "targetEntities";
   private final static String provCreators = "provCreator";
   private final static String provCreateDate = "provCreateDate";
   private final static String provModifiedDate = "provModifiedDate";
   private final static String relationshipModel = "relationshipModel";

   private SolrInputDocument document;
   private Map<String,Object> fieldModifier;
   private final static String SET = "set";

   public static RelnSolrProxy create(Relationship reln)
   {
      RelnSolrProxy proxy = new RelnSolrProxy();
      RelationshipDV relnDV = RelationshipDV.create(reln);


      proxy.addField(relnId, relnDV.id);
      proxy.addField(description, relnDV.description);
      proxy.addField(descriptMimeType, relnDV.descriptionMimeType);
      proxy.addField(relationshipType, relnDV.typeId);
      proxy.addEntities(relatedEntities, relnDV.relatedEntities);
      proxy.addEntities(targetEntities, relnDV.targetEntities);
      proxy.addProvenance(relnDV.provenance);

      try
      {
         proxy.addField(relationshipModel, SolrRelationshipSearchService.mapper.writeValueAsString(relnDV));
      }
      catch (JsonProcessingException e)
      {
         throw new IllegalStateException("Failed to serialize relationship DV", e);
      }

      return proxy;
   }

   public static RelnSolrProxy update(Relationship reln)
   {
      RelnSolrProxy proxy = new RelnSolrProxy();
      RelationshipDV relnDV = RelationshipDV.create(reln);

      proxy.addField(relnId, relnDV.id);

      proxy.updateField(description, relnDV.description, SET);
      proxy.updateField(descriptMimeType, relnDV.descriptionMimeType, SET);
      proxy.updateField(relationshipType, relnDV.typeId, SET);
      proxy.updateEntities(relatedEntities, relnDV.relatedEntities, SET);
      proxy.updateEntities(targetEntities, relnDV.targetEntities, SET);
      proxy.updateProvenance(relnDV.provenance, SET);

      try
      {
         proxy.addField(relationshipModel, SolrRelationshipSearchService.mapper.writeValueAsString(relnDV));
      }
      catch (JsonProcessingException e)
      {
         throw new IllegalStateException("Failed to serialize relationship DV", e);
      }


      return proxy;
   }

   public RelnSolrProxy()
   {
      document = new SolrInputDocument();
   }

   public SolrInputDocument getDocument()
   {
      return document;
   }

   void addField(String fieldName, String fieldValue)
   {
      document.addField(fieldName, fieldValue);
   }


   void updateField(String fieldName, String value, String updateType)
   {
      fieldModifier = new HashMap<>(1);
      fieldModifier.put(updateType, value);
      document.addField(fieldName, fieldModifier);
   }

   void addEntities(String fieldName, Set<AnchorDV> anchors)
   {
      for (AnchorDV anchor : anchors)
      {
         for (String uri : anchor.entryUris)
         {
            document.addField(fieldName, uri);
         }
      }
   }

   private void updateEntities(String fieldName, Set<AnchorDV> anchors, String updateType)
   {
      Set<String> allEntities = new HashSet<>();
      fieldModifier = new HashMap<>(1);

      for (AnchorDV anchor : anchors)
      {
         allEntities.addAll(anchor.entryUris);
      }
      fieldModifier.put(updateType, allEntities);
      document.addField(fieldName, fieldModifier);
   }

   void addProvenance(ProvenanceDV prov)
   {
      String dateCreated = prov.dateCreated;
      String dateModified = prov.dateModified;

      document.addField(provCreators, prov.creatorUris);
      document.addField(provCreateDate, (dateCreated != null) ? dateCreated : null);
      document.addField(provModifiedDate, (dateModified != null) ? dateModified : null);
   }

   private void updateProvenance(ProvenanceDV prov, String updateType)
   {
      String dateCreated = prov.dateCreated;
      String dateModified = prov.dateModified;

      fieldModifier = new HashMap<>();
      fieldModifier.put(updateType, prov.creatorUris);

      document.addField(provCreators, fieldModifier);
      document.addField(provCreateDate, (dateCreated != null) ? dateCreated : null);
      document.addField(provModifiedDate, (dateModified != null) ? dateModified : null);
   }

}
