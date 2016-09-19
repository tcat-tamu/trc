package edu.tamu.tcat.trc.entries.types.biblio.impl.repo;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import edu.tamu.tcat.trc.entries.common.DateDescription;
import edu.tamu.tcat.trc.entries.types.biblio.AuthorReference;
import edu.tamu.tcat.trc.entries.types.biblio.CopyReference;
import edu.tamu.tcat.trc.entries.types.biblio.Edition;
import edu.tamu.tcat.trc.entries.types.biblio.PublicationInfo;
import edu.tamu.tcat.trc.entries.types.biblio.Title;
import edu.tamu.tcat.trc.entries.types.biblio.Volume;
import edu.tamu.tcat.trc.entries.types.biblio.BibliographicEntry;
import edu.tamu.tcat.trc.entries.types.biblio.dto.AuthorReferenceDTO;
import edu.tamu.tcat.trc.entries.types.biblio.dto.CopyReferenceDTO;
import edu.tamu.tcat.trc.entries.types.biblio.dto.DateDescriptionDTO;
import edu.tamu.tcat.trc.entries.types.biblio.dto.EditionDTO;
import edu.tamu.tcat.trc.entries.types.biblio.dto.PublicationInfoDTO;
import edu.tamu.tcat.trc.entries.types.biblio.dto.TitleDTO;
import edu.tamu.tcat.trc.entries.types.biblio.dto.VolumeDTO;
import edu.tamu.tcat.trc.entries.types.biblio.dto.WorkDTO;
import edu.tamu.tcat.trc.entries.types.biblio.impl.model.BasicAuthorList;
import edu.tamu.tcat.trc.entries.types.biblio.impl.model.BasicAuthorReference;
import edu.tamu.tcat.trc.entries.types.biblio.impl.model.BasicCopyReference;
import edu.tamu.tcat.trc.entries.types.biblio.impl.model.BasicEdition;
import edu.tamu.tcat.trc.entries.types.biblio.impl.model.BasicPublicationInfo;
import edu.tamu.tcat.trc.entries.types.biblio.impl.model.BasicTitle;
import edu.tamu.tcat.trc.entries.types.biblio.impl.model.BasicTitleDefinition;
import edu.tamu.tcat.trc.entries.types.biblio.impl.model.BasicVolume;
import edu.tamu.tcat.trc.entries.types.biblio.impl.model.BasicWork;

public class ModelAdapter
{
   /**
    * Constructs a {@link BibliographicEntry} instance from a storage data transfer object.
    *
    * @param dto
    * @return
    */
   public static BibliographicEntry adapt(WorkDTO dto)
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

   public static class DateDescriptionImpl implements DateDescription
   {
      private static final Logger logger = Logger.getLogger(DateDescriptionDTO.class.getName());

      public static java.time.format.DateTimeFormatter Iso8601Formatter = DateTimeFormatter.ISO_LOCAL_DATE;

      private final String description;
      private final LocalDate value;

      public DateDescriptionImpl(DateDescriptionDTO dv)
      {
         if (dv == null)
            dv = new DateDescriptionDTO();

         this.description = dv.description;
         this.value = extractCalendarDate(dv);
      }

      private static LocalDate extractCalendarDate(DateDescriptionDTO dv)
      {
         try
         {
            return (dv.calendar != null && !dv.calendar.trim().isEmpty())
                  ? LocalDate.parse(dv.calendar, Iso8601Formatter) : null;
         }
         catch (Exception ex)
         {
            logger.info("Invalid date supplied [" + dv.calendar + "]. Converting to null.");
            return null;
         }
      }

      @Override
      public String getDescription()
      {
         return description;
      }

      @Override
      public LocalDate getCalendar()
      {
         return value;
      }
   }
}
