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
package edu.tamu.tcat.trc.entries.types.biblio.postgres;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import edu.tamu.tcat.trc.entries.types.biblio.dto.AuthorRefDV;
import edu.tamu.tcat.trc.entries.types.biblio.dto.PublicationInfoDV;
import edu.tamu.tcat.trc.entries.types.biblio.dto.TitleDV;
import edu.tamu.tcat.trc.entries.types.biblio.dto.VolumeDV;
import edu.tamu.tcat.trc.entries.types.biblio.repo.VolumeMutator;

public class VolumeMutatorImpl implements VolumeMutator
{
   private final VolumeDV volume;


   VolumeMutatorImpl(VolumeDV volume)
   {
      this.volume = volume;
   }

   @Override
   public String getId()
   {
      return volume.id;
   }

   @Override
   public void setAll(VolumeDV volume)
   {
      setVolumeNumber(volume.volumeNumber);
      setAuthors(volume.authors);
      setTitles(volume.titles);
      setOtherAuthors(volume.otherAuthors);
      setPublicationInfo(volume.publicationInfo);
      setSummary(volume.summary);
      setSeries(volume.series);
   }

   @Override
   public void setPublicationInfo(PublicationInfoDV info)
   {
      this.volume.publicationInfo = info;
   }

   @Override
   public void setVolumeNumber(String volumeNumber)
   {
      this.volume.volumeNumber = volumeNumber;
   }

   @Override
   public void setAuthors(List<AuthorRefDV> authors)
   {
      volume.authors = new ArrayList<>(authors);
   }

   @Override
   public void setTitles(Collection<TitleDV> titles)
   {
      volume.titles = new HashSet<>(titles);
   }

   @Override
   public void setOtherAuthors(List<AuthorRefDV> otherAuthors)
   {
      volume.otherAuthors = new ArrayList<>(otherAuthors);
   }

   @Override
   public void setSummary(String summary)
   {
      volume.summary = summary;
   }

   @Override
   public void setSeries(String series)
   {
      volume.series = series;
   }
}
