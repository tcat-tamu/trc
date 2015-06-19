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
package edu.tamu.tcat.trc.entries.types.biblio.dto;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import edu.tamu.tcat.trc.entries.repo.NoSuchCatalogRecordException;
import edu.tamu.tcat.trc.entries.types.biblio.AuthorReference;
import edu.tamu.tcat.trc.entries.types.biblio.Edition;
import edu.tamu.tcat.trc.entries.types.biblio.PublicationInfo;
import edu.tamu.tcat.trc.entries.types.biblio.Title;
import edu.tamu.tcat.trc.entries.types.biblio.Volume;

public class EditionDV
{
   public String id;
   public String editionName;
   public PublicationInfoDV publicationInfo;
   public List<AuthorRefDV> authors;
   public Collection<TitleDV> titles;
   public List<AuthorRefDV> otherAuthors;
   public String summary;
   public String series;
   // Hack: Editions may not contain volumes by default and can be added later.
   public List<VolumeDV> volumes = new ArrayList<VolumeDV>();

   public static EditionDV create(Edition ed)
   {
      EditionDV dto = new EditionDV();
      dto.id = ed.getId();

      dto.editionName = ed.getEditionName();

      dto.publicationInfo = PublicationInfoDV.create(ed.getPublicationInfo());

      dto.volumes = ed.getVolumes().stream()
            .map(VolumeDV::create)
            .collect(Collectors.toList());

      dto.authors = ed.getAuthors().stream()
            .map(AuthorRefDV::create)
            .collect(Collectors.toList());

      dto.titles = ed.getTitles().parallelStream()
            .map(TitleDV::create)
            .collect(Collectors.toSet());

      dto.otherAuthors = ed.getOtherAuthors().stream()
            .map(AuthorRefDV::create)
            .collect(Collectors.toList());

      dto.summary = ed.getSummary();

      dto.series = ed.getSeries();

      return dto;
   }

   public static Edition instantiate(EditionDV dv)
   {
      EditionImpl edition = new EditionImpl();
      edition.id = dv.id;

      edition.delegate = new CommonFieldsDelegate(dv.authors, dv.titles, dv.otherAuthors, dv.summary);

      edition.editionName = dv.editionName;
      edition.publicationInfo = PublicationInfoDV.instantiate(dv.publicationInfo);
      edition.volumes = dv.volumes.stream()
                           .map(VolumeDV::intantiate)
                           .collect(Collectors.toList());
      edition.series = dv.series;

      return edition;
   }

   public static class EditionImpl implements Edition
   {
      private String id;

      private CommonFieldsDelegate delegate = new CommonFieldsDelegate();
      private String editionName;
      private PublicationInfo publicationInfo;
      private List<Volume> volumes;

      // TODO might belong in CommonFieldsDelegate?
      private String series;

      @Override
      public String getId()
      {
         return id;
      }

      @Override
      public List<AuthorReference> getAuthors()
      {
         return delegate.getAuthors();
      }

      @Override
      public Collection<Title> getTitles()
      {
         return delegate.getTitles();
      }

      @Override
      public List<AuthorReference> getOtherAuthors()
      {
         return delegate.getOtherAuthors();
      }

      @Override
      public String getEditionName()
      {
         return editionName;
      }

      @Override
      public PublicationInfo getPublicationInfo()
      {
         return publicationInfo;
      }

      @Override
      public List<Volume> getVolumes()
      {
         return volumes;
      }

      @Override
      public String getSummary()
      {
         return delegate.getSummary();
      }

      @Override
      public String getSeries()
      {
         return series;
      }

      @Override
      public Volume getVolume(String volumeId) throws NoSuchCatalogRecordException
      {
         for (Volume volume : volumes) {
            if (volume.getId().equals(volumeId)) {
               return volume;
            }
         }

         throw new NoSuchCatalogRecordException("Unable to find volume [" + volumeId + "] in edition [" + id + "].");
      }

   }

}