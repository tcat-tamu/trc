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
package edu.tamu.tcat.trc.entries.types.biblio.test.mocks;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import edu.tamu.tcat.trc.entries.types.biblio.dto.AuthorReferenceDTO;
import edu.tamu.tcat.trc.entries.types.biblio.dto.EditionDTO;
import edu.tamu.tcat.trc.entries.types.biblio.dto.PublicationInfoDTO;
import edu.tamu.tcat.trc.entries.types.biblio.dto.TitleDTO;
import edu.tamu.tcat.trc.entries.types.biblio.repo.EditionMutator;
import edu.tamu.tcat.trc.entries.types.biblio.repo.VolumeMutator;
import edu.tamu.tcat.trc.entries.types.biblio.repo.copies.CopyReferenceMutator;
import edu.tamu.tcat.trc.repo.IdFactoryProvider;

public class MockEditionMutator implements EditionMutator
{
   private final EditionDTO dto;
   private final IdFactoryProvider idFactoryProvider;
   private final Object volumeIdFactory;

   public MockEditionMutator(EditionDTO edition, IdFactoryProvider idFactoryProvider)
   {
      this.dto = edition;
      this.idFactoryProvider = idFactoryProvider;
      this.volumeIdFactory = idFactoryProvider.getIdFactory("volumes");
   }

   @Override
   public String getId()
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public void setAuthors(List<AuthorReferenceDTO> authors)
   {
      // TODO Auto-generated method stub

   }

   @Override
   public void setTitles(Collection<TitleDTO> titles)
   {
      // TODO Auto-generated method stub

   }

   @Override
   public void setOtherAuthors(List<AuthorReferenceDTO> otherAuthors)
   {
      // TODO Auto-generated method stub

   }

   @Override
   public void setEditionName(String editionName)
   {
      // TODO Auto-generated method stub

   }

   @Override
   public void setPublicationInfo(PublicationInfoDTO pubInfo)
   {
      // TODO Auto-generated method stub

   }

   @Override
   public void setSeries(String series)
   {
      // TODO Auto-generated method stub

   }

   @Override
   public void setSummary(String summary)
   {
      // TODO Auto-generated method stub

   }

   @Override
   public VolumeMutator createVolume()
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public VolumeMutator editVolume(String id)
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public void removeVolume(String volumeId)
   {
      // TODO Auto-generated method stub

   }

   @Override
   public void setDefaultCopyReference(String defaultCopyReferenceId)
   {
      // TODO Auto-generated method stub

   }

   @Override
   public CopyReferenceMutator editCopyReference(String id)
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public CopyReferenceMutator createCopyReference()
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public void removeCopyReference(String id)
   {
      // TODO Auto-generated method stub

   }

   @Override
   public void retainAllVolumes(Set<String> volumeIds)
   {
      // TODO Auto-generated method stub

   }

   @Override
   public void retainAllCopyReferences(Set<String> copyReferenceIds)
   {
      // TODO Auto-generated method stub

   }

}
