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
package edu.tamu.tcat.trc.entries.types.bio.rest.v1.internal;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

import edu.tamu.tcat.trc.entries.common.DateDescription;
import edu.tamu.tcat.trc.entries.common.HistoricalEvent;
import edu.tamu.tcat.trc.entries.types.bio.BiographicalEntry;
import edu.tamu.tcat.trc.entries.types.bio.PersonName;
import edu.tamu.tcat.trc.entries.types.bio.rest.v1.RestApiV1;
import edu.tamu.tcat.trc.resolver.EntryIdDto;
import edu.tamu.tcat.trc.resolver.EntryResolverRegistry;

/**
 * An encapsulation of adapter methods to convert between the repository API and
 * the {@link RestApiV1} schema DTOs.
 */
public class RepoAdapter
{
   public static RestApiV1.Person toDTO(BiographicalEntry orig, EntryResolverRegistry resolvers)
   {
      if (orig == null)
         return null;
      RestApiV1.Person dto = new RestApiV1.Person();
      dto.id = orig.getId();
      dto.ref = EntryIdDto.adapt(resolvers.getResolver(orig).makeReference(orig), resolvers);

      PersonName canonicalName = orig.getCanonicalName();
      if (canonicalName != null) {
         dto.name = toDTO(canonicalName);
      }

      dto.altNames = orig.getAlternativeNames().stream()
                     .map(RepoAdapter::toDTO)
                     .collect(Collectors.toSet());
      // remove name from altNames - for legacy reasons, the list of altnNames
      //    originally included all names associated with this person.
      if (dto.altNames.contains(dto.name))
         dto.altNames.remove(dto.name);

      dto.birth = toDTO(orig.getBirth());
      dto.death = toDTO(orig.getDeath());
      dto.summary = orig.getSummary();

      return dto;
   }

   public static RestApiV1.PersonName toDTO(PersonName orig)
   {
      if (orig == null)
         return null;
      RestApiV1.PersonName dto = new RestApiV1.PersonName();

      dto.title = orig.getTitle();
      dto.givenName = orig.getGivenName();
      dto.middleName = orig.getMiddleName();
      dto.familyName = orig.getFamilyName();
      dto.suffix = orig.getSuffix();

      dto.label = orig.getDisplayName();

      return dto;
   }

   public static RestApiV1.HistoricalEvent toDTO(HistoricalEvent orig)
   {
      if (orig == null)
         return null;
      RestApiV1.HistoricalEvent dto = new RestApiV1.HistoricalEvent();
      dto.id = orig.getId();
      dto.title = orig.getTitle();
      dto.description = orig.getDescription();
      dto.location = orig.getLocation();
      dto.date = toDTO(orig.getDate());
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
}

