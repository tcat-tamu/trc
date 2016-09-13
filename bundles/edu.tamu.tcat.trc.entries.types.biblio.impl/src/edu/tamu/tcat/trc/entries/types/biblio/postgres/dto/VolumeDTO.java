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
package edu.tamu.tcat.trc.entries.types.biblio.postgres.dto;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import edu.tamu.tcat.trc.entries.types.biblio.CopyReference;
import edu.tamu.tcat.trc.entries.types.biblio.Volume;

public class VolumeDTO
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

   public VolumeDTO()
   {

   }

   public VolumeDTO(VolumeDTO orig)
   {
      this.id = orig.id;
      this.volumeNumber = orig.volumeNumber;
      this.publicationInfo = new PublicationInfoDTO(orig.publicationInfo);
      this.authors = orig.authors.stream().map(AuthorReferenceDTO::new).collect(Collectors.toList());
      this.titles = orig.titles.stream().map(TitleDTO::new).collect(Collectors.toList());

      this.series = orig.series;
      this.summary = orig.summary;
      this.defaultCopyReferenceId = orig.defaultCopyReferenceId;
      this.copyReferences = orig.copyReferences.stream().map(CopyReferenceDTO::new).collect(Collectors.toSet());
   }

   public static VolumeDTO create(Volume volume)
   {
      VolumeDTO dto = new VolumeDTO();

      if (volume == null)
      {
         return dto;
      }

      dto.id = volume.getId();

      dto.volumeNumber = volume.getVolumeNumber();

      dto.publicationInfo = PublicationInfoDTO.create(volume.getPublicationInfo());

      dto.authors = volume.getAuthors().stream()
            .map(AuthorReferenceDTO::create)
            .collect(Collectors.toList());

      dto.titles = volume.getTitles().parallelStream()
            .map(TitleDTO::create)
            .collect(Collectors.toSet());

      dto.otherAuthors = volume.getOtherAuthors().stream()
            .map(AuthorReferenceDTO::create)
            .collect(Collectors.toList());

      dto.series = volume.getSeries();

      dto.summary = volume.getSummary();

      CopyReference defaultCopyReference = volume.getDefaultCopyReference();
      if (defaultCopyReference != null)
      {
         dto.defaultCopyReferenceId = defaultCopyReference.getId();
      }

      dto.copyReferences = volume.getCopyReferences().stream()
            .map(CopyReferenceDTO::create)
            .collect(Collectors.toSet());

      return dto;
   }
}
