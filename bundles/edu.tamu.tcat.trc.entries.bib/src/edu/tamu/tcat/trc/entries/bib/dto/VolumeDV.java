package edu.tamu.tcat.trc.entries.bib.dto;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import edu.tamu.tcat.trc.entries.bib.AuthorReference;
import edu.tamu.tcat.trc.entries.bib.PublicationInfo;
import edu.tamu.tcat.trc.entries.bib.Title;
import edu.tamu.tcat.trc.entries.bib.Volume;

public class VolumeDV
{
   public String id;
   public String volumeNumber;
   public PublicationInfoDV publicationInfo;
   public List<AuthorRefDV> authors;
   public Collection<TitleDV> titles;
   public List<AuthorRefDV> otherAuthors;
   public String summary;
   public String series;

   public static VolumeDV create(Volume vol)
   {
      VolumeDV dto = new VolumeDV();
      dto.id = vol.getId();

      dto.volumeNumber = vol.getVolumeNumber();

      dto.publicationInfo = PublicationInfoDV.create(vol.getPublicationInfo());

      dto.authors = vol.getAuthors().stream()
            .map((ref) -> AuthorRefDV.create(ref))
            .collect(Collectors.toList());

      dto.titles = vol.getTitles().parallelStream()
            .map(TitleDV::create)
            .collect(Collectors.toSet());

      dto.otherAuthors = vol.getOtherAuthors().stream()
            .map((ref) -> AuthorRefDV.create(ref))
            .collect(Collectors.toList());

      dto.summary = vol.getSummary();

      dto.series = vol.getSeries();
      return dto;
   }

   public static VolumeImpl intantiate(VolumeDV dv)
   {
      VolumeImpl volume = new VolumeImpl();
      volume.id = dv.id;

      volume.volumeNumber = dv.volumeNumber;

      volume.publicationInfo = dv.publicationInfo == null
            ? PublicationInfoDV.instantiate(new PublicationInfoDV())
            : PublicationInfoDV.instantiate(dv.publicationInfo);

      volume.authors = dv.authors.stream()
            .map(AuthorRefDV::instantiate)
            .collect(Collectors.toList());

      volume.titles = dv.titles.parallelStream()
            .map(TitleDV::instantiate)
            .collect(Collectors.toSet());

      volume.otherAuthors = dv.otherAuthors.stream()
            .map(AuthorRefDV::instantiate)
            .collect(Collectors.toList());

      volume.summary = dv.summary;

      volume.series = dv.series;

      return volume;
   }

   public static class VolumeImpl implements Volume
   {
      private String id;
      private String volumeNumber;
      private PublicationInfo publicationInfo;
      private List<AuthorReference> authors;
      private Collection<Title> titles;
      private List<AuthorReference> otherAuthors;
      private String summary;
      private String series;

      @Override
      public String getId()
      {
         return id;
      }

      @Override
      public String getVolumeNumber()
      {
         return volumeNumber;
      }

      @Override
      public PublicationInfo getPublicationInfo()
      {
         return publicationInfo;
      }

      @Override
      public List<AuthorReference> getAuthors()
      {
         return authors;
      }

      @Override
      public Collection<Title> getTitles()
      {
         return titles;
      }

      @Override
      public List<AuthorReference> getOtherAuthors()
      {
         return otherAuthors;
      }

      @Override
      public String getSummary()
      {
         return summary;
      }

      @Override
      public String getSeries()
      {
         return series;
      }
   }
}
