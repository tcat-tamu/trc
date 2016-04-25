package edu.tamu.tcat.trc.entries.types.biblio.postgres;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import edu.tamu.tcat.trc.entries.common.DateDescription;
import edu.tamu.tcat.trc.entries.common.dto.DateDescriptionDTO;
import edu.tamu.tcat.trc.entries.types.biblio.AuthorReference;
import edu.tamu.tcat.trc.entries.types.biblio.CopyReference;
import edu.tamu.tcat.trc.entries.types.biblio.Edition;
import edu.tamu.tcat.trc.entries.types.biblio.PublicationInfo;
import edu.tamu.tcat.trc.entries.types.biblio.Title;
import edu.tamu.tcat.trc.entries.types.biblio.Volume;
import edu.tamu.tcat.trc.entries.types.biblio.Work;
import edu.tamu.tcat.trc.entries.types.biblio.dto.AuthorReferenceDTO;
import edu.tamu.tcat.trc.entries.types.biblio.dto.EditionDTO;
import edu.tamu.tcat.trc.entries.types.biblio.dto.PublicationInfoDTO;
import edu.tamu.tcat.trc.entries.types.biblio.dto.TitleDTO;
import edu.tamu.tcat.trc.entries.types.biblio.dto.VolumeDTO;
import edu.tamu.tcat.trc.entries.types.biblio.dto.WorkDTO;
import edu.tamu.tcat.trc.entries.types.biblio.dto.copies.CopyReferenceDTO;
import edu.tamu.tcat.trc.entries.types.biblio.postgres.copies.BasicCopyReference;
import edu.tamu.tcat.trc.entries.types.bio.postgres.model.DateDescriptionImpl;

public class ModelAdapter
{
   /**
    * Constructs a {@link Work} instance from a storage data transfer object.
    *
    * @param dto
    * @return
    */
   public static Work adapt(WorkDTO dto)
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
            dto.type,
            new BasicAuthorList(authors),
            new BasicTitleDefinition(titles),
            new BasicAuthorList(otherAuthors),
            adaptEditions(dto.editions),
            dto.series,
            dto.summary,
            defaultCopyReference,
            copyReferences);
   }

   public static List<Edition> adaptEditions(List<EditionDTO> dtos)
   {
      if (dtos == null)
      {
         return null;
      }

      return dtos.stream()
            .map(ModelAdapter::adaptEdition)
            .collect(Collectors.toList());
   }

   public static Edition adaptEdition(EditionDTO dto)
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

   public static List<Volume> adaptVolumes(List<VolumeDTO> dtos)
   {
      if (dtos == null)
      {
         return null;
      }

      return dtos.stream()
            .map(ModelAdapter::adaptVolume)
            .collect(Collectors.toList());
   }

   public static Volume adaptVolume(VolumeDTO dto)
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

   public static List<AuthorReference> adaptAuthors(List<AuthorReferenceDTO> dtos)
   {
      if (dtos == null)
      {
         return null;
      }

      return dtos.stream()
            .map(ModelAdapter::adaptAuthorReference)
            .collect(Collectors.toList());
   }

   public static AuthorReference adaptAuthorReference(AuthorReferenceDTO dto)
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

   public static List<Title> adaptTitles(Collection<TitleDTO> dtos)
   {
      if (dtos == null)
      {
         return null;
      }

      return dtos.stream()
            .map(ModelAdapter::adaptTitle)
            .collect(Collectors.toList());
   }

   public static Title adaptTitle(TitleDTO dto)
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

   public static PublicationInfo adaptPublicationInfo(PublicationInfoDTO dto)
   {
      if (dto == null)
      {
         return null;
      }

      return new BasicPublicationInfo(dto.place,
            dto.publisher,
            adaptDateDescription(dto.date));
   }

   public static DateDescription adaptDateDescription(DateDescriptionDTO dto)
   {
      if (dto == null)
      {
         return null;
      }

      return new DateDescriptionImpl(dto);
   }

   public static Set<CopyReference> adaptCopyReferences(Set<CopyReferenceDTO> dtos)
   {
      if (dtos == null)
      {
         return null;
      }

      return dtos.stream()
            .map(ModelAdapter::adaptCopyReference)
            .collect(Collectors.toSet());
   }

   /**
    * Constructs a {@link CopyReference} instance from a storage data transfer object
    * @param dto
    * @return
    */
   public static CopyReference adaptCopyReference(CopyReferenceDTO dto)
   {
      return new BasicCopyReference(dto.id,
            dto.type,
            dto.properties,
            dto.title,
            dto.summary,
            dto.rights);
   }
}
