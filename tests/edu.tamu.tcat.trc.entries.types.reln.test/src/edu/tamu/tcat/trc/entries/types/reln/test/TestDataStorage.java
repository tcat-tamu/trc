package edu.tamu.tcat.trc.entries.types.reln.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;

import java.net.URI;
import java.sql.PreparedStatement;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edu.tamu.tcat.db.core.DataSourceException;
import edu.tamu.tcat.db.exec.sql.SqlExecutor;
import edu.tamu.tcat.osgi.config.ConfigurationProperties;
import edu.tamu.tcat.osgi.config.file.SimpleFileConfigurationProperties;
import edu.tamu.tcat.trc.entries.types.reln.Anchor;
import edu.tamu.tcat.trc.entries.types.reln.Relationship;
import edu.tamu.tcat.trc.entries.types.reln.dto.AnchorDTO;
import edu.tamu.tcat.trc.entries.types.reln.dto.ProvenanceDTO;
import edu.tamu.tcat.trc.entries.types.reln.dto.RelationshipDTO;
import edu.tamu.tcat.trc.entries.types.reln.internal.dto.BasicAnchorSet;
import edu.tamu.tcat.trc.entries.types.reln.postgres.ExtPointRelnTypeRegistry;
import edu.tamu.tcat.trc.entries.types.reln.postgres.RelationshipRepositoryImpl;
import edu.tamu.tcat.trc.entries.types.reln.repo.EditRelationshipCommand;
import edu.tamu.tcat.trc.repo.IdFactoryProvider;
import edu.tamu.tcat.trc.repo.RepositoryException;
import edu.tamu.tcat.trc.test.TestUtils;


public class TestDataStorage
{
   private SqlExecutor exec;
   private ConfigurationProperties config;

   private RelationshipRepositoryImpl repo;
   private ExtPointRelnTypeRegistry registry;
//   private IdFactory idFactory;
   private boolean canWrite = false;

   @Before
   public void setupTest() throws DataSourceException
   {
      IdFactoryProvider idFactoryProvider = TestUtils.makeIdFactoryProvider();
//      idFactory = idFactoryProvider.getIdFactory(RelationshipRepositoryImpl.ID_CONTEXT);
      registry = new ExtPointRelnTypeRegistry();
      registry.activate();

      config = TestUtils.loadConfigFile();
      exec = TestUtils.initPostgreSqlExecutor(config);

      this.repo = new RelationshipRepositoryImpl();
      repo.setDatabaseExecutor(exec);
      repo.setIdFactory(idFactoryProvider);
      repo.setTypeRegistry(registry);
      repo.activate();
      canWrite = true;
   }

   @After
   public void tearDownTest() throws Exception
   {
      cleanDB();
      registry.dispose();
      repo.dispose();
      exec = null;

      if (config instanceof SimpleFileConfigurationProperties)
         ((SimpleFileConfigurationProperties)config).dispose();
   }

   private void cleanDB() throws InterruptedException, ExecutionException
   {
      if (!canWrite)
         return;

      String sql = "DELETE FROM relationships";
      Future<Void> future = exec.submit((conn) -> {
         try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.executeUpdate();
            return null;
         }
      });

      future.get();
      canWrite = false;
   }
   
   @Test
   public void testCreateRelationship() throws InterruptedException, ExecutionException
   {
      RelationshipDTO relndto = createDTO();
      String relnId = createRelationship(relndto);
      Relationship relationship = repo.get(relnId);
      
      assertEquals(relndto.typeId, relationship.getType().getIdentifier());
      assertEquals(relndto.description, relationship.getDescription());
      assertEquals(relndto.descriptionMimeType, relationship.getDescriptionFormat());
      assertEquals(relndto.provenance.dateCreated, relationship.getProvenance().getDateCreated().toString());
      assertEquals(relndto.provenance.dateModified, relationship.getProvenance().getDateModified().toString());
      
      assertEquals(getURI(relndto.relatedEntities), getURI(relationship.getRelatedEntities().getAnchors()));
      assertEquals(getURI(relndto.targetEntities), getURI(relationship.getTargetEntities().getAnchors()));
   }
   
   @Test
   public void testEditRelationship() throws InterruptedException, ExecutionException
   {
      RelationshipDTO relndto = createDTO();
      String relnId = createRelationship(relndto);
      Relationship orig = repo.get(relnId);
      
      editRelationship(relnId);
      
      Relationship updated = repo.get(relnId);
      
      assertEquals(orig.getId(), updated.getId());
      assertNotEquals("Descriptions have not changed",orig.getDescription(), updated.getDescription());
   }
   
   @Test
   public void testDeleteRelationship() throws InterruptedException, ExecutionException
   {
      try
      {
         RelationshipDTO relndto = createDTO();
         String relnId = createRelationship(relndto);
         repo.delete(relnId);
         repo.get(relnId);
         assertFalse("An IllegalArgumentException should have been thrown.", true);
      }
      catch (IllegalArgumentException e)
      {
         
      }
   }
   
   @Test
   public void testRetreiveAllRelns() throws InterruptedException, ExecutionException
   {

      RelationshipDTO relndto = createDTO();
      createRelationship(relndto);
      createRelationship(relndto);
      createRelationship(relndto);
      createRelationship(relndto);
      
      Iterator<Relationship> allRelationships = repo.getAllRelationships();
      int countReln = 0;
      while (allRelationships.hasNext())
      {
         allRelationships.next();
         countReln++;
      }
      
      assertEquals(countReln, 4);
   }
   
   private String getURI(Collection<Anchor> anchors)
   {
      String result = "";
      for(Anchor a : anchors)
      {
         for(URI uri : a.getEntryIds())
         {
            result = uri.toString();
         }
      }
      return result;
   }
   
   private String getURI(Set<AnchorDTO> anchors)
   {
      String result = "";
      for(AnchorDTO a : anchors)
      {
         for(String uri : a.entryUris)
         {
            return uri;
         }
      }
      return result;
   }
   
   private RelationshipDTO createDTO()
   {
      
      ProvenanceDTO provDTO = new ProvenanceDTO();
      provDTO.creatorUris = new HashSet<>();
      provDTO.dateCreated = "2015-03-10T18:31:10.179Z";
      provDTO.dateModified = "2015-03-10T18:31:10.179Z";
      
      AnchorDTO relatedDTO = new AnchorDTO();
      relatedDTO.entryUris = new HashSet<>(Arrays.asList("works/39"));
      
      AnchorDTO targetedDTO = new AnchorDTO();
      targetedDTO.entryUris = new HashSet<>(Arrays.asList("works/390"));
      
      RelationshipDTO relnDTO = new RelationshipDTO();
      relnDTO.typeId = "uk.ac.ox.bodleian.sda.relationships.provoked";
      relnDTO.description = "";
      relnDTO.descriptionMimeType = "text/html";
      relnDTO.provenance = provDTO;
      relnDTO.relatedEntities = new HashSet<>(Arrays.asList(relatedDTO));
      relnDTO.targetEntities = new HashSet<>(Arrays.asList(targetedDTO));
      
      return relnDTO;
   }
   
   private String createRelationship(RelationshipDTO dto) throws InterruptedException, ExecutionException
   {
      
      RelationshipDTO relndto = createDTO();
      EditRelationshipCommand createReln = repo.create();
      createReln.setTypeId(relndto.typeId);
      createReln.setDescription(relndto.description);
      createReln.setDescriptionFormat(relndto.descriptionMimeType);
      createReln.setProvenance(relndto.provenance);
      createReln.setRelatedEntities(createAnchorSet(relndto.relatedEntities));
      createReln.setTargetEntities(createAnchorSet(relndto.targetEntities));
      
      return createReln.execute().get();
   }
   
   private void editRelationship(String id) throws InterruptedException, ExecutionException
   {

      EditRelationshipCommand editReln = repo.edit(id);
      editReln.setDescription("adding a new description");
      editReln.execute().get();
   }
   
   private static BasicAnchorSet createAnchorSet(Set<AnchorDTO> entities)
   {
      if (entities.isEmpty())
         return new BasicAnchorSet(new HashSet<>());

      Set<Anchor> anchors = new HashSet<>();
      for (AnchorDTO anchorData : entities)
      {
         anchors.add(AnchorDTO.instantiate(anchorData));
      }

      return new BasicAnchorSet(anchors);
   }
}
