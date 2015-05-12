package edu.tamu.tcat.trc.entries.types.biblio.test.mocks;

import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

import edu.tamu.tcat.catalogentries.NoSuchCatalogRecordException;
import edu.tamu.tcat.trc.entries.bib.EditionMutator;
import edu.tamu.tcat.trc.entries.bib.VolumeMutator;
import edu.tamu.tcat.trc.entries.bib.dto.AuthorRefDV;
import edu.tamu.tcat.trc.entries.bib.dto.EditionDV;
import edu.tamu.tcat.trc.entries.bib.dto.PublicationInfoDV;
import edu.tamu.tcat.trc.entries.bib.dto.TitleDV;

public class MockEditionMutator implements EditionMutator
{
   private EditionDV dto;
   private Supplier<String> volumeIds;

   public MockEditionMutator(EditionDV edition, Supplier<String> volumeIds)
   {
      this.dto = edition;
      this.volumeIds = volumeIds;
   }

   @Override
   public String getId()
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public void setAll(EditionDV edition)
   {
      // TODO Auto-generated method stub

   }

   @Override
   public void setAuthors(List<AuthorRefDV> authors)
   {
      // TODO Auto-generated method stub

   }

   @Override
   public void setTitles(Collection<TitleDV> titles)
   {
      // TODO Auto-generated method stub

   }

   @Override
   public void setOtherAuthors(List<AuthorRefDV> otherAuthors)
   {
      // TODO Auto-generated method stub

   }

   @Override
   public void setEditionName(String editionName)
   {
      // TODO Auto-generated method stub

   }

   @Override
   public void setPublicationInfo(PublicationInfoDV pubInfo)
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
   public VolumeMutator editVolume(String id) throws NoSuchCatalogRecordException
   {
      // TODO Auto-generated method stub
      return null;
   }

}
