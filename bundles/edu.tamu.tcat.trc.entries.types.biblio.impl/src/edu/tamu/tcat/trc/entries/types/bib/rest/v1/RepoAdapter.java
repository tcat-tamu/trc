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
package edu.tamu.tcat.trc.entries.types.bib.rest.v1;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.stream.Collectors;

import edu.tamu.tcat.trc.entries.common.DateDescription;
import edu.tamu.tcat.trc.entries.common.dto.DateDescriptionDTO;
import edu.tamu.tcat.trc.entries.types.biblio.AuthorReference;
import edu.tamu.tcat.trc.entries.types.biblio.Edition;
import edu.tamu.tcat.trc.entries.types.biblio.PublicationInfo;
import edu.tamu.tcat.trc.entries.types.biblio.Title;
import edu.tamu.tcat.trc.entries.types.biblio.Volume;
import edu.tamu.tcat.trc.entries.types.biblio.dto.AuthorRefDV;
import edu.tamu.tcat.trc.entries.types.biblio.dto.EditionDV;
import edu.tamu.tcat.trc.entries.types.biblio.dto.PublicationInfoDV;
import edu.tamu.tcat.trc.entries.types.biblio.dto.TitleDV;
import edu.tamu.tcat.trc.entries.types.biblio.dto.VolumeDV;
import edu.tamu.tcat.trc.entries.types.biblio.dto.WorkDV;

/**
 * An encapsulation of adapter methods to convert between the repository API and
 * the {@link RestApiV1} schema DTOs.
 */
public class RepoAdapter
{
   public static RestApiV1.AuthorRef toDTO(AuthorRefDV orig)
   {
      if (orig == null)
         return null;
      
      RestApiV1.AuthorRef dto = new RestApiV1.AuthorRef();
      dto.authorId = orig.authorId;
      dto.name = orig.name;
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

      dto.volumes = ed.getVolumes().stream()
            .map(RepoAdapter::toDTO)
            .collect(Collectors.toList());

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
   
   public static RestApiV1.Volume toDTO(Volume ed)
   {
      if (ed == null)
         return null;
      
      RestApiV1.Volume dto = new RestApiV1.Volume();
      dto.id = ed.getId();

      dto.volumeNumber = ed.getVolumeNumber();

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
      dto.name = author.getName();
      if (dto.name != null)
         parseLegacyName(dto);

      String fName = author.getFirstName();
      String lName = author.getLastName();

      dto.firstName = ((fName != null) && !fName.trim().isEmpty()) ? fName : dto.firstName;
      dto.lastName = ((lName != null) && !lName.trim().isEmpty()) ? lName : dto.lastName;

      dto.role = author.getRole();
      return dto;
   }
   
   private static void parseLegacyName(RestApiV1.AuthorRef author)
   {
      // HACK for legacy entries, try to split out first and last names.
      // TODO remove once data in DB has been converted.
      author.name = author.name.trim();
      int ix = author.name.lastIndexOf(",");
      ix = ix > 0 ? ix : author.name.lastIndexOf(";");
      if (ix > 0)
      {
         author.firstName = author.name.substring(ix + 1).trim();
         author.lastName = author.name.substring(0, ix).trim();
      } else {
         ix = author.name.lastIndexOf(" ");
         if (ix > 0)
         {
            author.lastName = author.name.substring(ix + 1).trim();
            author.firstName = author.name.substring(0, ix).trim();
         }
      }
   }

   public static AuthorRefDV toRepo(RestApiV1.AuthorRef orig)
   {
      if (orig == null)
         return null;
      
      AuthorRefDV dto = new AuthorRefDV();
      dto.authorId = orig.authorId;
      dto.name = orig.name;
      dto.lastName = orig.lastName;
      dto.firstName = orig.firstName;
      dto.role = orig.role;
      
      return dto;
   }
   
   public static WorkDV toRepo(RestApiV1.Work orig)
   {
      if (orig == null)
         return null;
      
      WorkDV dto = new WorkDV();
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

   public static EditionDV toRepo(RestApiV1.Edition orig)
   {
      if (orig == null)
         return null;
      
      EditionDV dto = new EditionDV();
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

   public static VolumeDV toRepo(RestApiV1.Volume orig)
   {
      if (orig == null)
         return null;
      
      VolumeDV dto = new VolumeDV();
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

   public static PublicationInfoDV toRepo(RestApiV1.PublicationInfo orig)
   {
      if (orig == null)
         return null;
      
      PublicationInfoDV dto = new PublicationInfoDV();
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

   public static TitleDV toRepo(RestApiV1.Title orig)
   {
      if (orig == null)
         return null;
      
      TitleDV dto = new TitleDV();
      dto.title = orig.title;
      dto.type = orig.type;
      dto.lg = orig.lg;
      dto.subtitle = orig.subtitle;
      
      return dto;
   }
}
