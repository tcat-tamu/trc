package edu.tamu.tcat.trc.entries.types.biblio.impl.repo;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import edu.tamu.tcat.trc.entries.common.DateDescription;
import edu.tamu.tcat.trc.entries.types.biblio.AuthorReference;
import edu.tamu.tcat.trc.entries.types.biblio.BibliographicEntry;
import edu.tamu.tcat.trc.entries.types.biblio.CopyReference;
import edu.tamu.tcat.trc.entries.types.biblio.Edition;
import edu.tamu.tcat.trc.entries.types.biblio.PublicationInfo;
import edu.tamu.tcat.trc.entries.types.biblio.Title;
import edu.tamu.tcat.trc.entries.types.biblio.Volume;

public abstract class DataModelV1
{
   // TODO move to model adapter
   public static WorkDTO adapt(BibliographicEntry work)
   {
      WorkDTO dto = new WorkDTO();

      if (work == null)
      {
         return dto;
      }

      dto.id = work.getId();

      dto.type = work.getType();

      dto.authors = StreamSupport.stream(work.getAuthors().spliterator(), false)
            .map(DataModelV1::adapt)
            .collect(Collectors.toList());

      dto.titles = work.getTitle().get().stream()
            .map(DataModelV1::adapt)
            .collect(Collectors.toSet());

      dto.otherAuthors = StreamSupport.stream(work.getOtherAuthors().spliterator(), false)
            .map(DataModelV1::adapt)
            .collect(Collectors.toList());

      dto.editions = work.getEditions().stream()
            .map(DataModelV1::adapt)
            .collect(Collectors.toList());

      dto.series = work.getSeries();

      dto.summary = work.getSummary();

      CopyReference defaultCopyReference = work.getDefaultCopyReference();
      if (defaultCopyReference != null)
      {
         dto.defaultCopyReferenceId = defaultCopyReference.getId();
      }

      dto.copyReferences = work.getCopyReferences().stream()
            .map(DataModelV1::adapt)
            .collect(Collectors.toSet());

      return dto;
   }

   public static DataModelV1.EditionDTO adapt(Edition edition)
   {
      DataModelV1.EditionDTO dto = new DataModelV1.EditionDTO();

      if (edition == null)
      {
         return dto;
      }

      dto.id = edition.getId();

      dto.editionName = edition.getEditionName();

      dto.publicationInfo = DataModelV1.adapt(edition.getPublicationInfo());

      dto.authors = edition.getAuthors().stream()
            .map(DataModelV1::adapt)
            .collect(Collectors.toList());

      dto.titles = edition.getTitles().parallelStream()
            .map(DataModelV1::adapt)
            .collect(Collectors.toSet());

      dto.otherAuthors = edition.getOtherAuthors().stream()
            .map(DataModelV1::adapt)
            .collect(Collectors.toList());

      dto.volumes = edition.getVolumes().stream()
            .map(DataModelV1::adapt)
            .collect(Collectors.toList());

      dto.series = edition.getSeries();

      dto.summary = edition.getSummary();

      CopyReference defaultCopyReference = edition.getDefaultCopyReference();
      if (defaultCopyReference != null)
      {
         dto.defaultCopyReferenceId = defaultCopyReference.getId();
      }

      dto.copyReferences = edition.getCopyReferences().stream()
            .map(DataModelV1::adapt)
            .collect(Collectors.toSet());

      return dto;
   }

   public static VolumeDTO adapt(Volume volume)
   {
      VolumeDTO dto = new VolumeDTO();

      if (volume == null)
      {
         return dto;
      }

      dto.id = volume.getId();

      dto.volumeNumber = volume.getVolumeNumber();

      dto.publicationInfo = DataModelV1.adapt(volume.getPublicationInfo());

      dto.authors = volume.getAuthors().stream()
            .map(DataModelV1::adapt)
            .collect(Collectors.toList());

      dto.titles = volume.getTitles().parallelStream()
            .map(DataModelV1::adapt)
            .collect(Collectors.toSet());

      dto.otherAuthors = volume.getOtherAuthors().stream()
            .map(DataModelV1::adapt)
            .collect(Collectors.toList());

      dto.series = volume.getSeries();

      dto.summary = volume.getSummary();

      CopyReference defaultCopyReference = volume.getDefaultCopyReference();
      if (defaultCopyReference != null)
      {
         dto.defaultCopyReferenceId = defaultCopyReference.getId();
      }

      dto.copyReferences = volume.getCopyReferences().stream()
            .map(DataModelV1::adapt)
            .collect(Collectors.toSet());

      return dto;
   }

   public static DataModelV1.TitleDTO adapt(Title title)
   {
      DataModelV1.TitleDTO dto = new DataModelV1.TitleDTO();
      if (title == null)
         return dto;

      dto.type = title.getType();
      dto.title = title.getTitle();
      dto.subtitle = title.getSubTitle();
      dto.lg = title.getLanguage();

      return dto;
   }


   public static DataModelV1.PublicationInfoDTO adapt(PublicationInfo pubInfo)
   {
      DataModelV1.PublicationInfoDTO dto = new DataModelV1.PublicationInfoDTO();
      if (pubInfo == null)
         return dto;

      dto.place = pubInfo.getLocation();

      dto.publisher = pubInfo.getPublisher();

      edu.tamu.tcat.trc.entries.common.DateDescription date = pubInfo.getPublicationDate();
      if (date != null)
      {
         dto.date = DateDescriptionDTO.create(date);
      }

      return dto;
   }


   public static CopyReferenceDTO adapt(CopyReference ref)
   {
      CopyReferenceDTO dto = new CopyReferenceDTO();

      dto.id = ref.getId();
      dto.type = ref.getType();
      dto.properties = ref.getProperties();

      dto.title = ref.getTitle();
      dto.summary = ref.getSummary();
      dto.rights = ref.getRights();

      return dto;
   }

   public static AuthorReferenceDTO adapt(AuthorReference authorReference)
   {
      AuthorReferenceDTO dto = new AuthorReferenceDTO();
      if (authorReference == null)
         return dto;

      dto.authorId = authorReference.getId();

      String firstName = authorReference.getFirstName();
      String lastName = authorReference.getLastName();

      if (firstName != null && !firstName.trim().isEmpty())
      {
         dto.firstName = firstName.trim();
      }

      if (lastName != null && !lastName.trim().isEmpty())
      {
         dto.lastName = lastName.trim();
      }

      dto.role = authorReference.getRole();

      return dto;
   }

   public static WorkDTO copy(WorkDTO orig)
   {
      WorkDTO dto = new WorkDTO();

      dto.id = orig.id;
      dto.type = orig.type;
      dto.authors = orig.authors.stream().map(DataModelV1::copy).collect(Collectors.toList());
      dto.titles = orig.titles.stream().map(DataModelV1::copy).collect(Collectors.toList());

      dto.editions = orig.editions.stream()
            .map(DataModelV1::copy)
            .collect(Collectors.toList());

      dto.series = orig.series;
      dto.summary = orig.summary;
      dto.defaultCopyReferenceId = orig.defaultCopyReferenceId;
      dto.copyReferences = orig.copyReferences.stream()
            .map(DataModelV1::copy)
            .collect(Collectors.toSet());

      return dto;
   }

   public static EditionDTO copy(DataModelV1.EditionDTO orig)
   {
      DataModelV1.EditionDTO dto = new DataModelV1.EditionDTO();

      dto.id = orig.id;
      dto.editionName = orig.editionName;
      dto.publicationInfo = DataModelV1.copy(orig.publicationInfo);
      dto.authors = orig.authors.stream().map(DataModelV1::copy).collect(Collectors.toList());
      dto.titles = orig.titles.stream().map(DataModelV1::copy).collect(Collectors.toList());

      dto.volumes = orig.volumes.stream().map(DataModelV1::copy).collect(Collectors.toList());
      dto.series = orig.series;
      dto.summary = orig.summary;
      dto.defaultCopyReferenceId = orig.defaultCopyReferenceId;
      dto.copyReferences = orig.copyReferences.stream().map(DataModelV1::copy).collect(Collectors.toSet());

      return dto;
   }

   public static VolumeDTO copy(VolumeDTO orig)
   {
      DataModelV1.VolumeDTO dto = new DataModelV1.VolumeDTO();

      dto.id = orig.id;
      dto.volumeNumber = orig.volumeNumber;
      dto.publicationInfo = DataModelV1.copy(orig.publicationInfo);
      dto.authors = orig.authors.stream().map(DataModelV1::copy).collect(Collectors.toList());
      dto.titles = orig.titles.stream().map(DataModelV1::copy).collect(Collectors.toList());

      dto.series = orig.series;
      dto.summary = orig.summary;
      dto.defaultCopyReferenceId = orig.defaultCopyReferenceId;
      dto.copyReferences = orig.copyReferences.stream().map(DataModelV1::copy).collect(Collectors.toSet());

      return dto;
   }

   public static DataModelV1.TitleDTO copy(TitleDTO orig)
   {
      DataModelV1.TitleDTO dto = new DataModelV1.TitleDTO();

      dto.type = orig.type;
      dto.title = orig.title;
      dto.subtitle = orig.subtitle;
      dto.lg = orig.lg;

      return dto;
   }

   public static DateDescriptionDTO copy (DateDescriptionDTO orig)
   {
      DateDescriptionDTO dto = new DateDescriptionDTO();
      if (orig == null)
         return dto;

      dto.calendar = orig.calendar;
      dto.description = orig.description;

      return dto;
   }


   public static PublicationInfoDTO copy(DataModelV1.PublicationInfoDTO orig)
   {
      DataModelV1.PublicationInfoDTO dto = new DataModelV1.PublicationInfoDTO();
      if (orig == null)
         return null;

      dto.place = orig.place;
      dto.publisher = orig.publisher;
      dto.date = DataModelV1.copy(orig.date);

      return dto;
   }



   public static CopyReferenceDTO copy(CopyReferenceDTO orig)
   {
      CopyReferenceDTO dto = new CopyReferenceDTO();

      dto.id = orig.id;
      dto.type = orig.type;

      if (orig.properties != null) {
         dto.properties = new HashMap<>(orig.properties);
      }

      dto.title = orig.title;
      dto.summary = orig.summary;
      dto.rights = orig.rights;

      return dto;
   }
   public static AuthorReferenceDTO copy(AuthorReferenceDTO orig)
   {
      AuthorReferenceDTO dto = new AuthorReferenceDTO();
      dto.authorId = orig.authorId;
      dto.firstName = orig.firstName;
      dto.lastName = orig.lastName;
      dto.role = orig.role;

      return dto;
   }
   public static class WorkDTO
   {
      public String id;
      public String type;
      public List<AuthorReferenceDTO> authors = new ArrayList<>();
      public Collection<TitleDTO> titles = new ArrayList<>();
      public List<EditionDTO> editions = new ArrayList<>();
      public String series;
      public String summary;
      public String defaultCopyReferenceId;
      public Set<CopyReferenceDTO> copyReferences = new HashSet<>();

      @Deprecated // maintained for legacy data preservation purposes.
      public List<AuthorReferenceDTO> otherAuthors = new ArrayList<>();
   }

   public static class EditionDTO
   {
      public String id;
      public String editionName;
      // TODO: should default publication info be null or an empty object?
      public PublicationInfoDTO publicationInfo;
      public List<AuthorReferenceDTO> authors = new ArrayList<>();
      public Collection<TitleDTO> titles = new ArrayList<>();
      public List<AuthorReferenceDTO> otherAuthors = new ArrayList<>();
      public String series;
      public String summary;
      public List<VolumeDTO> volumes = new ArrayList<>();
      public String defaultCopyReferenceId;
      public Set<CopyReferenceDTO> copyReferences = new HashSet<>();
   }

   public static class VolumeDTO
   {
      public String id;
      public String volumeNumber;
      // TODO: should default publication info be null or an empty object?
      public PublicationInfoDTO publicationInfo;
      public List<AuthorReferenceDTO> authors = new ArrayList<>();
      public Collection<TitleDTO> titles = new ArrayList<>();
      public List<AuthorReferenceDTO> otherAuthors = new ArrayList<>();
      public String series;
      public String summary;
      public String defaultCopyReferenceId;
      public Set<CopyReferenceDTO> copyReferences = new HashSet<>();
   }

   public static class TitleDTO
   {
      public String type;   // short, default, undefined.
      public String title;
      public String subtitle;
      public String lg;
   }

   public static class PublicationInfoDTO
   {
      public String place;
      public String publisher;
      // TODO: should default date description be null or empty object?
      public DateDescriptionDTO date;
   }

   /**
    * A simple representation of historical date information that includes both a calendar
    * data (a Java {@link Instant}) and a description of that date.
    */
   public static class DateDescriptionDTO
   {
      /** ISO 8601 local (YYYY-MM-DD) representation of this date. */
      public String calendar;

      /** A human readable description of this date. */
      public String description;     // NOTE use this to capture intended degree of precision

      @Deprecated
      public static DateDescriptionDTO create(String description, LocalDate calendar)
      {
         DateDescriptionDTO dto = new DateDescriptionDTO();

         dto.description = description;
         dto.calendar = (calendar == null) ? null : DateTimeFormatter.ISO_LOCAL_DATE.format(calendar);

         return dto;
      }

      @Deprecated
      public static DateDescriptionDTO create(DateDescription date)
      {
         DateDescriptionDTO dto = new DateDescriptionDTO();

         LocalDate d = date.getCalendar();
         if (d != null)
         {
            dto.calendar = DateTimeFormatter.ISO_LOCAL_DATE.format(d);
         }

         // TODO convert legacy eventDate into DateDescriptionDTO and set to null
         dto.description = date.getDescription();

         return dto;
      }

//      @Deprecated
//      public static DateDescription convert(DateDescriptionDTO dv)
//      {
//         return dv == null ? new DateDescriptionImpl(new DateDescriptionDTO()) : new DateDescriptionImpl(dv);
//      }

   }

   public static class CopyReferenceDTO
   {
      public String id;
      public String type;
      public Map<String, String> properties = new HashMap<>();
      public String title;
      public String summary;
      public String rights;

   }


   public static class AuthorReferenceDTO
   {
      public String authorId;
      public String firstName;
      public String lastName;
      public String role;
   }


}
