package edu.tamu.tcat.trc.entries.types.biblio.postgres;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import edu.tamu.tcat.db.exec.sql.SqlExecutor;
import edu.tamu.tcat.trc.entries.common.DateDescription;
import edu.tamu.tcat.trc.entries.common.dto.DateDescriptionDTO;
import edu.tamu.tcat.trc.entries.types.biblio.AuthorReference;
import edu.tamu.tcat.trc.entries.types.biblio.Edition;
import edu.tamu.tcat.trc.entries.types.biblio.PublicationInfo;
import edu.tamu.tcat.trc.entries.types.biblio.Title;
import edu.tamu.tcat.trc.entries.types.biblio.Volume;
import edu.tamu.tcat.trc.entries.types.biblio.Work;
import edu.tamu.tcat.trc.entries.types.biblio.copies.CopyReference;
import edu.tamu.tcat.trc.entries.types.biblio.dto.AuthorReferenceDTO;
import edu.tamu.tcat.trc.entries.types.biblio.dto.EditionDTO;
import edu.tamu.tcat.trc.entries.types.biblio.dto.PublicationInfoDTO;
import edu.tamu.tcat.trc.entries.types.biblio.dto.TitleDTO;
import edu.tamu.tcat.trc.entries.types.biblio.dto.VolumeDTO;
import edu.tamu.tcat.trc.entries.types.biblio.dto.WorkDTO;
import edu.tamu.tcat.trc.entries.types.biblio.dto.copies.CopyReferenceDTO;
import edu.tamu.tcat.trc.entries.types.biblio.postgres.copies.BasicCopyReference;
import edu.tamu.tcat.trc.entries.types.biblio.repo.EditWorkCommand;
import edu.tamu.tcat.trc.entries.types.biblio.repo.WorkRepository;
import edu.tamu.tcat.trc.entries.types.biblio.repo.WorkRepositoryProvider;
import edu.tamu.tcat.trc.entries.types.bio.postgres.model.DateDescriptionImpl;
import edu.tamu.tcat.trc.repo.BasicSchemaBuilder;
import edu.tamu.tcat.trc.repo.CommitHook;
import edu.tamu.tcat.trc.repo.DocumentRepository;
import edu.tamu.tcat.trc.repo.EditCommandFactory;
import edu.tamu.tcat.trc.repo.IdFactory;
import edu.tamu.tcat.trc.repo.IdFactoryProvider;
import edu.tamu.tcat.trc.repo.RepositoryException;
import edu.tamu.tcat.trc.repo.RepositorySchema;
import edu.tamu.tcat.trc.repo.SchemaBuilder;
import edu.tamu.tcat.trc.repo.postgres.PsqlJacksonRepoBuilder;

public class WorkRepositoryProviderImpl implements WorkRepositoryProvider
{
   private static final Logger logger = Logger.getLogger(WorkRepositoryProviderImpl.class.getName());

   public static final String CONTEXT_WORK = "works";

   private static final String TABLE_NAME = "works";
   private static final String SCHEMA_ID = "trcWork";
   private static final String SCHEMA_DATA_FIELD = "work";

   private SqlExecutor sqlExecutor;
   private IdFactoryProvider idFactoryProvider;

   private WorkRepository repo;

   /**
    * Bind method for SQL executor service dependency (usually called by dependency injection layer)-
    *
    * @param sqlExecutor
    */
   public void setSqlExecutor(SqlExecutor sqlExecutor)
   {
      this.sqlExecutor = sqlExecutor;
   }

   /**
    * Bind method for ID factory provider service dependency (usually called by dependency injection layer)
    *
    * @param idFactory
    */
   public void setIdFactory(IdFactoryProvider idFactoryProvider)
   {
      this.idFactoryProvider = idFactoryProvider;
   }

   /**
    * Lifecycle management method (usually called by framework service layer)
    * Called when all dependencies have been provided and the service is ready to run.
    */
   public void activate()
   {
      Objects.requireNonNull(sqlExecutor, "No SQL Executor provided.");

      DocumentRepository<Work, EditWorkCommand> documentRepository = buildDocumentRepository();
      IdFactory idFactory = idFactoryProvider.getIdFactory(CONTEXT_WORK);
      repo = new WorkRepositoryImpl(documentRepository, idFactory);
   }

   /**
    * Lifecycle management method (usually called by framework service layer)
    * Called when this service is no longer required.
    */
   public void dispose()
   {
      sqlExecutor = null;
      repo = null;
   }

   @Override
   public WorkRepository getRepository()
   {
      return repo;
   }

   /**
    * @return A new document repository instance for persisting and retrieving works
    */
   private DocumentRepository<Work, EditWorkCommand> buildDocumentRepository()
   {
      PsqlJacksonRepoBuilder<Work, EditWorkCommand, WorkDTO> repoBuilder = new PsqlJacksonRepoBuilder<>();

      repoBuilder.setDbExecutor(sqlExecutor);
      repoBuilder.setTableName(TABLE_NAME);
      repoBuilder.setEditCommandFactory(new EditWorkCommandFactoryImpl(idFactoryProvider));
      repoBuilder.setDataAdapter(this::adapt);
      repoBuilder.setSchema(buildSchema());
      repoBuilder.setStorageType(WorkDTO.class);
      repoBuilder.setEnableCreation(true);

      try
      {
         return repoBuilder.build();
      }
      catch (RepositoryException e)
      {
         logger.log(Level.SEVERE, "Failed to construct work repository instance.", e);
      }
      return null;
   }

   /**
    * @return The repository schema
    */
   private RepositorySchema buildSchema()
   {
      SchemaBuilder schemaBuilder = new BasicSchemaBuilder();
      schemaBuilder.setId(SCHEMA_ID);
      schemaBuilder.setDataField(SCHEMA_DATA_FIELD);
      return schemaBuilder.build();
   }

   /**
    * Constructs a {@link Work} instance from a storage data transfer object.
    *
    * @param dto
    * @return
    */
   private Work adapt(WorkDTO dto)
   {
      if (dto == null)
      {
         return null;
      }

      List<AuthorReference> authors = adaptAuthors(dto.authors);
      Collection<Title> titles = adaptTitles(dto.titles);
      List<AuthorReference> otherAuthors = adaptAuthors(dto.otherAuthors);

      Set<CopyReference> copyReferences = adaptCopyReferences(dto.copyReferences);

      CopyReference defaultCopyReference = copyReferences.stream()
            .filter(copyReference -> Objects.equals(copyReference.getId(), dto.defaultCopyReferenceId))
            .findFirst()
            .orElse(null);

      return new BasicWork(dto.id,
            new BasicAuthorList(authors),
            new BasicTitleDefinition(titles),
            new BasicAuthorList(otherAuthors),
            adaptEditions(dto.editions),
            dto.series,
            dto.summary,
            defaultCopyReference,
            copyReferences);
   }

   private List<Edition> adaptEditions(List<EditionDTO> dtos)
   {
      if (dtos == null)
      {
         return null;
      }

      return dtos.stream()
            .map(this::adaptEdition)
            .collect(Collectors.toList());
   }

   private Edition adaptEdition(EditionDTO dto)
   {
      if (dto == null)
      {
         return null;
      }

      Set<CopyReference> copyReferences = adaptCopyReferences(dto.copyReferences);

      CopyReference defaultCopyReference = copyReferences.stream()
            .filter(copyReference -> Objects.equals(copyReference.getId(), dto.defaultCopyReferenceId))
            .findFirst()
            .orElse(null);

      return new BasicEdition(dto.id,
            dto.editionName,
            adaptPublicationInfo(dto.publicationInfo),
            adaptAuthors(dto.authors),
            adaptTitles(dto.titles),
            adaptAuthors(dto.otherAuthors),
            adaptVolumes(dto.volumes),
            dto.series,
            dto.summary,
            defaultCopyReference,
            copyReferences);
   }

   private List<Volume> adaptVolumes(List<VolumeDTO> dtos)
   {
      if (dtos == null)
      {
         return null;
      }

      return dtos.stream()
            .map(this::adaptVolume)
            .collect(Collectors.toList());
   }

   private Volume adaptVolume(VolumeDTO dto)
   {
      if (dto == null)
      {
         return null;
      }

      Set<CopyReference> copyReferences = adaptCopyReferences(dto.copyReferences);

      CopyReference defaultCopyReference = copyReferences.stream()
            .filter(copyReference -> Objects.equals(copyReference.getId(), dto.defaultCopyReferenceId))
            .findFirst()
            .orElse(null);

      return new BasicVolume(dto.id,
            dto.volumeNumber,
            adaptPublicationInfo(dto.publicationInfo),
            adaptAuthors(dto.authors),
            adaptTitles(dto.titles),
            adaptAuthors(dto.otherAuthors),
            dto.series,
            dto.summary,
            defaultCopyReference,
            copyReferences);
   }

   private List<AuthorReference> adaptAuthors(List<AuthorReferenceDTO> dtos)
   {
      if (dtos == null)
      {
         return null;
      }

      return dtos.stream()
            .map(this::adaptAuthorReference)
            .collect(Collectors.toList());
   }

   private AuthorReference adaptAuthorReference(AuthorReferenceDTO dto)
   {
      if (dto == null)
      {
         return null;
      }

      return new BasicAuthorReference(dto.authorId,
            dto.firstName,
            dto.lastName,
            dto.role);
   }

   private List<Title> adaptTitles(Collection<TitleDTO> dtos)
   {
      if (dtos == null)
      {
         return null;
      }

      return dtos.stream()
            .map(this::adaptTitle)
            .collect(Collectors.toList());
   }

   private Title adaptTitle(TitleDTO dto)
   {
      if (dto == null)
      {
         return null;
      }

      return new BasicTitle(dto.type,
            dto.title,
            dto.subtitle,
            dto.lg);
   }

   private PublicationInfo adaptPublicationInfo(PublicationInfoDTO dto)
   {
      if (dto == null)
      {
         return null;
      }

      return new BasicPublicationInfo(dto.place,
            dto.publisher,
            adaptDateDescription(dto.date));
   }

   private DateDescription adaptDateDescription(DateDescriptionDTO dto)
   {
      if (dto == null)
      {
         return null;
      }

      return new DateDescriptionImpl(dto);
   }

   private Set<CopyReference> adaptCopyReferences(Set<CopyReferenceDTO> dtos)
   {
      if (dtos == null)
      {
         return null;
      }

      return dtos.stream()
            .map(this::adaptCopyReference)
            .collect(Collectors.toSet());
   }

   /**
    * Constructs a {@link CopyReference} instance from a storage data transfer object
    * @param dto
    * @return
    */
   private CopyReference adaptCopyReference(CopyReferenceDTO dto)
   {
      return new BasicCopyReference(dto.id,
            dto.type,
            dto.properties,
            dto.title,
            dto.summary,
            dto.rights);
   }

   public static class EditWorkCommandFactoryImpl implements EditCommandFactory<WorkDTO, EditWorkCommand>
   {
      private final IdFactoryProvider idFactoryProvider;

      public EditWorkCommandFactoryImpl(IdFactoryProvider idFactoryProvider)
      {
         this.idFactoryProvider = idFactoryProvider;
      }

      @Override
      public EditWorkCommand create(String id, CommitHook<WorkDTO> commitHook)
      {
         return new EditWorkCommandImpl(id, null, commitHook, idFactoryProvider.extend(CONTEXT_WORK + "/" + id));
      }

      @Override
      public EditWorkCommand edit(String id, Supplier<WorkDTO> currentState, CommitHook<WorkDTO> commitHook)
      {
         return new EditWorkCommandImpl(id, currentState, commitHook, idFactoryProvider.extend(CONTEXT_WORK + "/" + id));
      }
   }
}
