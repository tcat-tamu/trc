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
package edu.tamu.tcat.trc.entries.types.biblio.rest.v1;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

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
import edu.tamu.tcat.trc.entries.types.biblio.repo.EditWorkCommand;
import edu.tamu.tcat.trc.entries.types.biblio.repo.EditionMutator;
import edu.tamu.tcat.trc.entries.types.biblio.repo.VolumeMutator;
import edu.tamu.tcat.trc.entries.types.biblio.repo.copies.CopyReferenceMutator;

/**
 * An encapsulation of adapter methods to convert between the repository API and
 * the {@link RestApiV1} schema DTOs.
 */
public class RepoAdapter
{
   public static RestApiV1.Work toDTO(Work work)
   {
      if (work == null)
      {
         return null;
      }

      RestApiV1.Work dto = new RestApiV1.Work();

      dto.id = work.getId();

      dto.authors = StreamSupport.stream(work.getAuthors().spliterator(), false)
            .map(RepoAdapter::toDTO)
            .collect(Collectors.toList());

      dto.titles = work.getTitle().get().stream()
            .map(RepoAdapter::toDTO)
            .collect(Collectors.toList());

      dto.otherAuthors = StreamSupport.stream(work.getOtherAuthors().spliterator(), false)
            .map(RepoAdapter::toDTO)
            .collect(Collectors.toList());

      dto.series = work.getSeries();

      dto.summary = work.getSummary();

      dto.editions = work.getEditions().stream()
            .map(RepoAdapter::toDTO)
            .collect(Collectors.toList());

      // TODO default digital copy

      dto.copies = work.getCopyReferences().stream()
            .map(edu.tamu.tcat.trc.entries.types.biblio.rest.v1.RepoAdapter::toDTO)
            .collect(Collectors.toList());

      return dto;
   }

   public static RestApiV1.AuthorRef toDTO(AuthorReferenceDTO orig)
   {
      if (orig == null)
         return null;

      RestApiV1.AuthorRef dto = new RestApiV1.AuthorRef();
      dto.authorId = orig.authorId;
      dto.lastName = orig.lastName;
      dto.firstName = orig.firstName;
      dto.role = orig.role;

      return dto;
   }

   public static RestApiV1.Edition toDTO(Edition ed)
   {
      RestApiV1.Edition dto = new RestApiV1.Edition();
      dto.id = ed.getId();

      dto.editionName = ed.getEditionName();

      dto.publicationInfo = toDTO(ed.getPublicationInfo());

      dto.authors = ed.getAuthors().stream()
            .map(RepoAdapter::toDTO)
            .collect(Collectors.toList());

      dto.titles = ed.getTitles().parallelStream()
            .map(RepoAdapter::toDTO)
            .collect(Collectors.toSet());

      dto.otherAuthors = ed.getOtherAuthors().stream()
            .map(RepoAdapter::toDTO)
            .collect(Collectors.toList());

      dto.summary = ed.getSummary();

      dto.series = ed.getSeries();

      dto.volumes = ed.getVolumes().stream()
            .map(RepoAdapter::toDTO)
            .collect(Collectors.toList());

      // TODO default copy reference

      dto.copies = ed.getCopyReferences().stream()
            .map(edu.tamu.tcat.trc.entries.types.biblio.rest.v1.RepoAdapter::toDTO)
            .collect(Collectors.toList());

      return dto;
   }

   public static RestApiV1.PublicationInfo toDTO(PublicationInfo orig)
   {
      if (orig == null)
         return null;

      RestApiV1.PublicationInfo dto = new RestApiV1.PublicationInfo();
      dto.publisher = orig.getPublisher();
      dto.place = orig.getLocation();
      dto.date = toDTO(orig.getPublicationDate());
      return dto;
   }

   public static RestApiV1.DateDescription toDTO(DateDescription orig)
   {
      if (orig == null)
         return null;
      RestApiV1.DateDescription dto = new RestApiV1.DateDescription();
      LocalDate d = orig.getCalendar();
      if (d != null)
      {
         dto.calendar = DateTimeFormatter.ISO_LOCAL_DATE.format(d);
      }

      dto.description = orig.getDescription();

      return dto;
   }

   public static RestApiV1.Volume toDTO(Volume vol)
   {
      if (vol == null)
         return null;

      RestApiV1.Volume dto = new RestApiV1.Volume();
      dto.id = vol.getId();

      dto.volumeNumber = vol.getVolumeNumber();

      dto.publicationInfo = toDTO(vol.getPublicationInfo());

      dto.authors = vol.getAuthors().stream()
            .map(RepoAdapter::toDTO)
            .collect(Collectors.toList());

      dto.titles = vol.getTitles().parallelStream()
            .map(RepoAdapter::toDTO)
            .collect(Collectors.toSet());

      dto.otherAuthors = vol.getOtherAuthors().stream()
            .map(RepoAdapter::toDTO)
            .collect(Collectors.toList());

      dto.summary = vol.getSummary();

      dto.series = vol.getSeries();

      // TODO default copy reference

      dto.copies = vol.getCopyReferences().stream()
            .map(edu.tamu.tcat.trc.entries.types.biblio.rest.v1.RepoAdapter::toDTO)
            .collect(Collectors.toList());

      return dto;
   }

   public static RestApiV1.Title toDTO(Title orig)
   {
      if (orig == null)
         return null;

      RestApiV1.Title dto = new RestApiV1.Title();
      dto.type = orig.getType();
      dto.lg = orig.getLanguage();
      dto.title = orig.getTitle();
      dto.subtitle = orig.getSubTitle();
      return dto;
   }

   public static RestApiV1.AuthorRef toDTO(AuthorReference author)
   {
      if (author == null)
         return null;

      RestApiV1.AuthorRef dto = new RestApiV1.AuthorRef();
      dto.authorId = author.getId();

      String fName = author.getFirstName();
      String lName = author.getLastName();

      dto.firstName = ((fName != null) && !fName.trim().isEmpty()) ? fName : dto.firstName;
      dto.lastName = ((lName != null) && !lName.trim().isEmpty()) ? lName : dto.lastName;

      dto.role = author.getRole();
      return dto;
   }

   public static RestApiV1.CopyReference toDTO(CopyReference copyReference)
   {
      if (copyReference == null)
      {
         return null;
      }

      RestApiV1.CopyReference dto = new RestApiV1.CopyReference();

      dto.id = copyReference.getId();
      dto.type = copyReference.getType();
      dto.properties = copyReference.getProperties();
      dto.title = copyReference.getTitle();
      dto.summary = copyReference.getSummary();
      dto.rights = copyReference.getRights();

      return dto;
   }

   public static AuthorReferenceDTO toRepo(RestApiV1.AuthorRef orig)
   {
      if (orig == null)
         return null;

      AuthorReferenceDTO dto = new AuthorReferenceDTO();
      dto.authorId = orig.authorId;
      dto.lastName = orig.lastName;
      dto.firstName = orig.firstName;
      dto.role = orig.role;

      return dto;
   }

   public static WorkDTO toRepo(RestApiV1.Work orig)
   {
      if (orig == null)
         return null;

      WorkDTO dto = new WorkDTO();
      dto.id = orig.id;

      if (orig.authors != null)
      {
         dto.authors = new ArrayList<>();
         for (RestApiV1.AuthorRef auth : orig.authors)
            dto.authors.add(toRepo(auth));
      }
      if (orig.titles != null)
      {
         dto.titles = new ArrayList<>();
         for (RestApiV1.Title title : orig.titles)
            dto.titles.add(toRepo(title));
      }
      if (orig.otherAuthors != null)
      {
         dto.otherAuthors = new ArrayList<>();
         for (RestApiV1.AuthorRef auth : orig.otherAuthors)
            dto.otherAuthors.add(toRepo(auth));
      }
      dto.series = orig.series;
      dto.summary = orig.summary;
      if (orig.editions != null)
      {
         dto.editions = new ArrayList<>();
         for (RestApiV1.Edition ed : orig.editions)
            dto.editions.add(toRepo(ed));
      }

      return dto;
   }

   public static EditionDTO toRepo(RestApiV1.Edition orig)
   {
      if (orig == null)
         return null;

      EditionDTO dto = new EditionDTO();
      dto.id = orig.id;
      dto.editionName = orig.editionName;
      dto.publicationInfo = toRepo(orig.publicationInfo);

      if (orig.volumes != null)
      {
         dto.volumes = new ArrayList<>();
         for (RestApiV1.Volume vol : orig.volumes)
            dto.volumes.add(toRepo(vol));
      }
      if (orig.authors != null)
      {
         dto.authors = new ArrayList<>();
         for (RestApiV1.AuthorRef auth : orig.authors)
            dto.authors.add(toRepo(auth));
      }
      if (orig.titles != null)
      {
         dto.titles = new ArrayList<>();
         for (RestApiV1.Title title : orig.titles)
            dto.titles.add(toRepo(title));
      }
      if (orig.otherAuthors != null)
      {
         dto.otherAuthors = new ArrayList<>();
         for (RestApiV1.AuthorRef auth : orig.otherAuthors)
            dto.otherAuthors.add(toRepo(auth));
      }
      dto.summary = orig.summary;
      dto.series = orig.series;

      return dto;
   }

   public static VolumeDTO toRepo(RestApiV1.Volume orig)
   {
      if (orig == null)
         return null;

      VolumeDTO dto = new VolumeDTO();
      dto.id = orig.id;
      dto.volumeNumber = orig.volumeNumber;
      dto.publicationInfo = toRepo(orig.publicationInfo);

      if (orig.authors != null)
      {
         dto.authors = new ArrayList<>();
         for (RestApiV1.AuthorRef auth : orig.authors)
            dto.authors.add(toRepo(auth));
      }
      if (orig.titles != null)
      {
         dto.titles = new ArrayList<>();
         for (RestApiV1.Title title : orig.titles)
            dto.titles.add(toRepo(title));
      }
      if (orig.otherAuthors != null)
      {
         dto.otherAuthors = new ArrayList<>();
         for (RestApiV1.AuthorRef auth : orig.otherAuthors)
            dto.otherAuthors.add(toRepo(auth));
      }
      dto.summary = orig.summary;
      dto.series = orig.series;

      return dto;
   }

   public static PublicationInfoDTO toRepo(RestApiV1.PublicationInfo orig)
   {
      if (orig == null)
         return null;

      PublicationInfoDTO dto = new PublicationInfoDTO();
      dto.publisher = orig.publisher;
      dto.place = orig.place;
      dto.date = toRepo(orig.date);

      return dto;
   }

   public static DateDescriptionDTO toRepo(RestApiV1.DateDescription orig)
   {
      if (orig == null)
         return null;

      DateDescriptionDTO dto = new DateDescriptionDTO();
      dto.calendar = orig.calendar;
      dto.description = orig.description;

      return dto;
   }

   public static TitleDTO toRepo(RestApiV1.Title orig)
   {
      if (orig == null)
         return null;

      TitleDTO dto = new TitleDTO();
      dto.title = orig.title;
      dto.type = orig.type;
      dto.lg = orig.lg;
      dto.subtitle = orig.subtitle;

      return dto;
   }

   public static CopyReferenceDTO toRepo(RestApiV1.CopyReference restDto)
   {
      if (restDto == null)
      {
         return null;
      }
   
      CopyReferenceDTO repoDto = new CopyReferenceDTO();
   
      repoDto.id = restDto.id;
      repoDto.type = restDto.type;
      repoDto.properties = restDto.properties;
      repoDto.title = restDto.title;
      repoDto.summary = restDto.summary;
      repoDto.rights = restDto.rights;
   
      return repoDto;
   }

   public static void save(RestApiV1.Work work, EditWorkCommand command)
   {
      List<AuthorReferenceDTO> authors = work.authors.stream()
            .map(RepoAdapter::toRepo)
            .collect(Collectors.toList());
      command.setAuthors(authors);

      List<TitleDTO> titles = work.titles.stream()
            .map(RepoAdapter::toRepo)
            .collect(Collectors.toList());
      command.setTitles(titles);

      List<AuthorReferenceDTO> otherAuthors = work.otherAuthors.stream()
            .map(RepoAdapter::toRepo)
            .collect(Collectors.toList());
      command.setOtherAuthors(otherAuthors);

      command.setSeries(work.series);

      command.setSummary(work.summary);

      Set<String> editionIds = work.editions.stream()
            .map(edition -> edition.id)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

      command.retainAllEditions(editionIds);

      work.editions.forEach(edition -> {
         EditionMutator editionMutator = edition.id == null ? command.createEdition() : command.editEdition(edition.id);
         RepoAdapter.save(edition, editionMutator);
      });

      Set<String> copyReferenceIds = work.copies.stream()
            .map(copy -> copy.id)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

      command.retainAllCopyReferences(copyReferenceIds);

      // TODO: default copy reference... how do we want it to be exposed via REST?
   }

   public static void save(RestApiV1.Edition edition, EditionMutator mutator)
   {
      mutator.setEditionName(edition.editionName);

      mutator.setPublicationInfo(toRepo(edition.publicationInfo));

      List<AuthorReferenceDTO> authors = edition.authors.stream()
            .map(RepoAdapter::toRepo)
            .collect(Collectors.toList());
      mutator.setAuthors(authors);

      List<TitleDTO> titles = edition.titles.stream()
            .map(RepoAdapter::toRepo)
            .collect(Collectors.toList());
      mutator.setTitles(titles);

      List<AuthorReferenceDTO> otherAuthors = edition.otherAuthors.stream()
            .map(RepoAdapter::toRepo)
            .collect(Collectors.toList());
      mutator.setOtherAuthors(otherAuthors);

      mutator.setSeries(edition.series);

      mutator.setSummary(edition.summary);

      Set<String> volumeIds = edition.volumes.stream()
            .map(volume -> volume.id)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

      mutator.retainAllVolumes(volumeIds);

      edition.volumes.forEach(volume -> {
         VolumeMutator volumeMutator = volume.id == null ? mutator.createVolume() : mutator.editVolume(volume.id);
         RepoAdapter.save(volume, volumeMutator);
      });

      Set<String> copyReferenceIds = edition.copies.stream()
            .map(copy -> copy.id)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

      mutator.retainAllCopyReferences(copyReferenceIds);

      // TODO: default copy reference... how do we want it to be exposed via REST?
   }

   public static void save(RestApiV1.Volume volume, VolumeMutator mutator)
   {
      mutator.setVolumeNumber(volume.volumeNumber);

      mutator.setPublicationInfo(toRepo(volume.publicationInfo));

      List<AuthorReferenceDTO> authors = volume.authors.stream()
            .map(RepoAdapter::toRepo)
            .collect(Collectors.toList());
      mutator.setAuthors(authors);

      List<TitleDTO> titles = volume.titles.stream()
            .map(RepoAdapter::toRepo)
            .collect(Collectors.toList());
      mutator.setTitles(titles);

      List<AuthorReferenceDTO> otherAuthors = volume.otherAuthors.stream()
            .map(RepoAdapter::toRepo)
            .collect(Collectors.toList());
      mutator.setOtherAuthors(otherAuthors);

      mutator.setSeries(volume.series);

      mutator.setSummary(volume.summary);

      Set<String> copyReferenceIds = volume.copies.stream()
            .map(copy -> copy.id)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

      mutator.retainAllCopyReferences(copyReferenceIds);

      // TODO: default copy reference... how do we want it to be exposed via REST?
   }

   // CODE INSERT HERE
   public static void save(RestApiV1.CopyReference dto, CopyReferenceMutator mutator)
   {
      mutator.setType(dto.type);
      mutator.setProperties(dto.properties);
      mutator.setTitle(dto.title);
      mutator.setSummary(dto.summary);
      mutator.setRights(dto.rights);
   }
}
