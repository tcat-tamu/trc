package edu.tamu.tcat.trc.test.persist.postgres;

import java.net.URI;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import edu.tamu.tcat.account.Account;
import edu.tamu.tcat.db.exec.sql.SqlExecutor;
import edu.tamu.tcat.trc.refman.BibliographicReference;
import edu.tamu.tcat.trc.refman.BibliographicReference.CreatorValue;
import edu.tamu.tcat.trc.refman.EditReferenceCommand;
import edu.tamu.tcat.trc.refman.dto.BibRefDTO;
import edu.tamu.tcat.trc.refman.dto.CreatorDTO;
import edu.tamu.tcat.trc.refman.postgres.BasicBibRef;
import edu.tamu.tcat.trc.refman.postgres.EditCmdFactoryImpl;
import edu.tamu.tcat.trc.refman.postgres.EditCollectionCmdFactory;
import edu.tamu.tcat.trc.refman.postgres.EditCollectionCmdFactory.EditCollectionCommand;
import edu.tamu.tcat.trc.refman.postgres.RefCollectionMeta;
import edu.tamu.tcat.trc.refman.types.CreatorRole;
import edu.tamu.tcat.trc.refman.types.ItemType;
import edu.tamu.tcat.trc.refman.types.ItemTypeProvider;
import edu.tamu.tcat.trc.repo.BasicSchemaBuilder;
import edu.tamu.tcat.trc.repo.DocumentRepository;
import edu.tamu.tcat.trc.repo.RepositoryException;
import edu.tamu.tcat.trc.repo.RepositorySchema;
import edu.tamu.tcat.trc.repo.postgres.PsqlJacksonRepoBuilder;

public class ReferenceManagerCollection
{
   private static final String table_id = "tableSchemaId";
   private static final String idField = "id";
   private static final String dataField = "ref_collection";
   private static final String createdField = "date_created";
   private static final String modifiedField = "date_modified";
   private static final String removedField = "removed";


   private static final String refmanColl = "refman_collections";
   private static final String refmanRefs = "refman_references";
   private static MockAccount myAccount;
   private List<CreatorDTO> creators;

   private DocumentRepository<RefCollectionMeta, EditCollectionCommand> docRepoCollection;

   private SqlExecutor sqlExec;
   private Map<String, ItemTypeProvider> providers;

   public ReferenceManagerCollection(SqlExecutor sqlExec,  Map<String, ItemTypeProvider> providers)
   {
      this.sqlExec = sqlExec;
      this.providers = new HashMap<>(providers);
      creators = new ArrayList<>();

      myAccount = new MockAccount();
      myAccount.uid = UUID.randomUUID();
   }

   public String getTableId()
   {
      return table_id;
   }

   public String getIdfield()
   {
      return idField;
   }

   public String getDatafield()
   {
      return dataField;
   }

   public String getCreatedfield()
   {
      return createdField;
   }

   public String getModifiedfield()
   {
      return modifiedField;
   }

   public String getRemovedfield()
   {
      return removedField;
   }

   public String getRefmancoll()
   {
      return refmanColl;
   }

   public String getRefmanrefs()
   {
      return refmanRefs;
   }

   public RepositorySchema buildDefaultSchema(BasicSchemaBuilder builder)
   {
      builder.setId(getTableId());
      builder.setIdField(getIdfield());
      builder.setDataField(getDatafield());
      builder.setCreatedField(getCreatedfield());
      builder.setModifiedField(getModifiedfield());
      builder.setRemovedField(getRemovedfield());
      RepositorySchema schema = builder.build();
      return schema;
   }

   public DocumentRepository<RefCollectionMeta, EditCollectionCommand> buildCollectionRepo() throws RepositoryException
   {
      PsqlJacksonRepoBuilder<RefCollectionMeta, EditCollectionCommand, RefCollectionMeta> repoBuilder =
            new PsqlJacksonRepoBuilder<>();
      repoBuilder.setDbExecutor(sqlExec);
      repoBuilder.setTableName(getRefmancoll());
      repoBuilder.setSchema(buildDefaultSchema(new BasicSchemaBuilder()));
      repoBuilder.setEditCommandFactory(new EditCollectionCmdFactory());
      repoBuilder.setDataAdapter(dto -> dto);
      repoBuilder.setEnableCreation(true);
      repoBuilder.setStorageType(RefCollectionMeta.class);

      docRepoCollection = repoBuilder.build();

      return docRepoCollection;
   }

   public DocumentRepository<BibliographicReference, EditReferenceCommand> buildReferenceRepo() throws RepositoryException
   {
      PsqlJacksonRepoBuilder<BibliographicReference, EditReferenceCommand, BibRefDTO> repoBuilder =
            new PsqlJacksonRepoBuilder<>();
      repoBuilder.setDbExecutor(sqlExec);

      repoBuilder.setTableName(getRefmanrefs());
      repoBuilder.setSchema(buildDefaultSchema(new BasicSchemaBuilder()));
      repoBuilder.setEditCommandFactory(new EditCmdFactoryImpl());
      repoBuilder.setDataAdapter(this::getBibRef);
      repoBuilder.setEnableCreation(true);
      repoBuilder.setStorageType(BibRefDTO.class);

      return repoBuilder.build();
   }

   private BibliographicReference getBibRef(BibRefDTO dto)
   {
      try
      {
         URI collectionId = URI.create(dto.collectionId);
         RefCollectionMeta collection = docRepoCollection.get(dto.collectionId);
         ItemTypeProvider typeProvider = lookupTypeProvider(collection.providerId);
         URI id = URI.create(dto.id);
         ItemType type = typeProvider.getItemType(dto.type);
         List<CreatorRole> creatorRoles = type.getCreatorRoles();
         Map<String, String> values = new HashMap<>(dto.values);

         List<CreatorValue> creatorValues = new ArrayList<>();
         for(CreatorDTO cdto : dto.creators)
         {
            for(CreatorRole role : creatorRoles)
            {
               BasicBibRef bibRef = new BasicBibRef();
               if(role.getId().equals(cdto.role))
                  creatorValues.add(bibRef.new BasicCreatorValue(cdto.firstName, cdto.lastName, role.getId(), cdto.authoritiveId));
            }

         }
         return new BasicBibRef(id, collectionId, type, values, creatorValues);
      }
      catch (RepositoryException e)
      {
         throw new IllegalStateException("" + e);
      }
   }

   public ItemTypeProvider lookupTypeProvider(String providerId)
   {
      if (!providers.containsKey(providerId))
      {
         String message = "No item type provider is defined for provider id {0}";
         throw new IllegalArgumentException(MessageFormat.format(message, providerId));
      }

      return providers.get(providerId);
   }

   public void setCreators(List<CreatorRole> roles)
   {
      for(CreatorRole role : roles)
      {
         CreatorDTO creator = new CreatorDTO();
         creator.firstName = "George";
         creator.lastName = "Lucas";
         creator.authoritiveId = UUID.randomUUID().toString();
         creator.role = role.getId();
         creators.add(creator);
      }
   }

   public List<CreatorDTO> getCreators()
   {
      return creators;
   }

   public Account getAccount()
   {
      return myAccount;
   }

}
