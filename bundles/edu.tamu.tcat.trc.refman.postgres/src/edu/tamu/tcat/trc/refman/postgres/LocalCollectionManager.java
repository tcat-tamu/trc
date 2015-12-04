package edu.tamu.tcat.trc.refman.postgres;

import java.net.URI;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import edu.tamu.tcat.account.Account;
import edu.tamu.tcat.db.exec.sql.SqlExecutor;
import edu.tamu.tcat.osgi.config.ConfigurationProperties;
import edu.tamu.tcat.trc.refman.BibliographicReference;
import edu.tamu.tcat.trc.refman.BibliographicReference.CreatorValue;
import edu.tamu.tcat.trc.refman.EditReferenceCommand;
import edu.tamu.tcat.trc.refman.RefManagerException;
import edu.tamu.tcat.trc.refman.ReferenceCollection;
import edu.tamu.tcat.trc.refman.ReferenceCollectionManager;
import edu.tamu.tcat.trc.refman.dto.BibRefDTO;
import edu.tamu.tcat.trc.refman.postgres.EditCollectionCmdFactory.EditCollectionCommand;
import edu.tamu.tcat.trc.refman.types.ItemType;
import edu.tamu.tcat.trc.refman.types.ItemTypeProvider;
import edu.tamu.tcat.trc.refman.types.zotero.ZoteroTypeProvider;
import edu.tamu.tcat.trc.repo.BasicSchemaBuilder;
import edu.tamu.tcat.trc.repo.DocumentRepository;
import edu.tamu.tcat.trc.repo.RepositoryException;
import edu.tamu.tcat.trc.repo.RepositorySchema;
import edu.tamu.tcat.trc.repo.SchemaBuilder;
import edu.tamu.tcat.trc.repo.postgres.PsqlJacksonRepoBuilder;

public class LocalCollectionManager implements ReferenceCollectionManager
{
   // NOTES:
   //        -- intended for use as OSGi service
   //        -- should allow multiple instances on different 'scopes'
   //        -- each scope defines its own tables for collections and references
   //        -- main API should allow binding to multiple manager services
   private SqlExecutor exec;

   private Map<String, ItemTypeProvider> providers = new HashMap<>();

   DocumentRepository<RefCollectionMeta, EditCollectionCommand> collectionRepo;
   DocumentRepository<BibliographicReference, EditReferenceCommand> refRepo;

   private ConfigurationProperties config;

   private String id;
   private String title;

   public LocalCollectionManager()
   {
   }

   @Override
   public String getId()
   {
      return id;
   }

   @Override
   public String getTitle()
   {
      return title;
   }

   public void setConfiguration(ConfigurationProperties config)
   {
      this.config = config;
   }

   public void setSqlExecutor(SqlExecutor exec)
   {
      this.exec = exec;
   }

   public void activate(Map<String, ? extends Object> params)
   {

      // TODO remove magic strings
      // TODO allow optional DB table configuration
      id = (String)params.get("id");
      title = (String)params.get("title");


      Objects.requireNonNull(exec, "No SQL executor provided");
      Objects.requireNonNull(config, "No configuration properties provided");

      try
      {
         collectionRepo = buildCollectionRepo();
         refRepo = buildReferenceRepo();
      }
      catch (Exception ex)
      {
         throw new IllegalStateException("Failed to initialize local collection manager.");
      }

      // TODO make this available as an Ext Point instead of DS pattern.
      ZoteroTypeProvider provider = new ZoteroTypeProvider();
      provider.setConfiguration(config);
      provider.activate();
      providers.put(ZoteroTypeProvider.ZOTERO_PROVIDER_ID, provider);
   }

   private DocumentRepository<BibliographicReference, EditReferenceCommand> buildReferenceRepo() throws RepositoryException
   {
      PsqlJacksonRepoBuilder<BibliographicReference, EditReferenceCommand, BibRefDTO> repoBuilder =
            new PsqlJacksonRepoBuilder<>();
      repoBuilder.setDbExecutor(exec);

      repoBuilder.setTableName("refman_references");
      repoBuilder.setSchema(buildBasicSchema());
      repoBuilder.setEditCommandFactory(new EditCmdFactoryImpl());

      repoBuilder.setDataAdapter(this::instantiate);
      repoBuilder.setEnableCreation(true);
      repoBuilder.setStorageType(BibRefDTO.class);

      return repoBuilder.build();
   }

   private DocumentRepository<RefCollectionMeta, EditCollectionCommand> buildCollectionRepo() throws RepositoryException
   {
      PsqlJacksonRepoBuilder<RefCollectionMeta, EditCollectionCommand, RefCollectionMeta> repoBuilder =
            new PsqlJacksonRepoBuilder<>();
      repoBuilder.setDbExecutor(exec);

      repoBuilder.setTableName("refman_collections");
      repoBuilder.setSchema(buildBasicSchema());
      repoBuilder.setEditCommandFactory(new EditCollectionCmdFactory());

      // NOTE: if we need to at some point, we can coerce this into returning API types
      //       (i.e. ReferenceCollections). For now, this repo is used internally to manage basic meta info about collections
      repoBuilder.setDataAdapter(dto -> dto);
      repoBuilder.setEnableCreation(true);
      repoBuilder.setStorageType(RefCollectionMeta.class);

      return repoBuilder.build();
   }

   private RepositorySchema buildBasicSchema()
   {
      // TODO: move all of these magic variables to ExtPoint based configuration or other external representation.
      // HACK: magic strings -- move to config system
      SchemaBuilder builder = new BasicSchemaBuilder();
      builder.setId("trc.schema.basic");
      builder.setDataField("data");
      builder.setIdField("id");
      builder.setCreatedField("date_created");
      builder.setModifiedField("date_modified");
      builder.setRemovedField("removed");

      return builder.build();
   }

   private BibliographicReference instantiate(BibRefDTO dto)
   {
      try
      {
         URI collectionId = URI.create(dto.collectionId);
         RefCollectionMeta collection = collectionRepo.get(dto.collectionId);
         ItemTypeProvider typeProvider = lookupTypeProvider(collection.providerId);


         URI id = URI.create(dto.id);
         ItemType type = typeProvider.getItemType(dto.type);
         Map<String, String> values = new HashMap<>(dto.values);

         // TODO load these from DTOs
         List<CreatorValue> creatorValues = null;

         return new BasicBibRef(id, collectionId, type, values, creatorValues);
      }
      catch (Exception ex)
      {
         String message = "Failed to instantiate bibliographic reference {0}";
         throw new IllegalStateException(MessageFormat.format(message, dto.id), ex);
      }
   }

   // TODO move to main repo API.
   public ItemTypeProvider lookupTypeProvider(String providerId)
   {
      if (!providers.containsKey(providerId))
      {
         String message = "No item type provider is defined for provider id {0}";
         throw new IllegalArgumentException(MessageFormat.format(message, providerId));
      }

      return providers.get(providerId);
   }

   /**
    * @return A set of all type providers supported by this collection manager.
    */
   @Override
   public Set<String> listTypeProviders()
   {
      return providers.keySet();
   }

   @Override
   public ReferenceCollection get(Account account, URI id) throws RefManagerException
   {
      try
      {
         RefCollectionMeta collection = collectionRepo.get(id.toString());
         ItemTypeProvider typeProvider = lookupTypeProvider(collection.providerId);

         return new LocalReferenceCollection(id, collection.name, account, typeProvider, refRepo);
      }
      catch (Exception ex)
      {
         String message = "Failed to load reference collection {0} for account {1}";
         throw new RefManagerException(MessageFormat.format(message, id, account), ex);
      }
   }

   @Override
   public ReferenceCollection create(Account account, String name, ItemTypeProvider provider) throws RefManagerException
   {
      String providerId = provider.getId();
      if (!this.listTypeProviders().contains(providerId))
      {
         String message = "Unsupported item type provider {0}";
         throw new RefManagerException(MessageFormat.format(message, providerId));
      }

      EditCollectionCommand command = collectionRepo.create(UUID.randomUUID().toString());
      command.setName(name);
      command.setProvider(providerId);

      try
      {
         String collectionId = command.execute().get();
         URI id = URI.create(collectionId);
         return new LocalReferenceCollection(id, name, account, provider, refRepo);
      }
      catch (Exception ex)
      {
         throw new RefManagerException("Failed to create reference collection.", ex);
      }
   }


}
