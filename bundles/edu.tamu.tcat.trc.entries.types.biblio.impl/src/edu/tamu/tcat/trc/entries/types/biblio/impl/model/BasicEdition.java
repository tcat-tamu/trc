package edu.tamu.tcat.trc.entries.types.biblio.impl.model;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import edu.tamu.tcat.trc.entries.types.biblio.AuthorReference;
import edu.tamu.tcat.trc.entries.types.biblio.CopyReference;
import edu.tamu.tcat.trc.entries.types.biblio.Edition;
import edu.tamu.tcat.trc.entries.types.biblio.PublicationInfo;
import edu.tamu.tcat.trc.entries.types.biblio.Title;
import edu.tamu.tcat.trc.entries.types.biblio.Volume;

public class BasicEdition implements Edition
{
   private final String id;
   private final String editionName;
   private final PublicationInfo publicationInfo;
   private final List<AuthorReference> authors;
   private final List<Title> titles;
   private final List<AuthorReference> otherAuthors;
   private final List<Volume> volumes;
   private final String series;
   private final String summary;
   private final CopyReference defaultCopyReference;
   private final Set<CopyReference> copyReferences;

   public BasicEdition(String id,
                       String editionName,
                       PublicationInfo publicationInfo,
                       List<AuthorReference> authors,
                       List<Title> titles,
                       List<AuthorReference> otherAuthors,
                       List<Volume> volumes,
                       String series,
                       String summary,
                       CopyReference defaultCopyReference,
                       Set<CopyReference> copyReferences)
   {
      this.id = id;
      this.editionName = editionName;
      this.publicationInfo = publicationInfo;
      this.authors = authors;
      this.titles = titles;
      this.otherAuthors = otherAuthors;
      this.volumes = volumes;
      this.series = series;
      this.summary = summary;
      this.defaultCopyReference = defaultCopyReference;
      this.copyReferences = copyReferences;
   }

   @Override
   public String getId()
   {
      return id;
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
   public List<Volume> getVolumes()
   {
      return volumes;
   }

   @Override
   public Volume getVolume(String volumeId)
   {
      for (Volume volume : volumes) {
         if (volume.getId().equals(volumeId)) {
            return volume;
         }
      }

      return null;
   }

   @Override
   public String getSeries()
   {
      return series;
   }

   @Override
   public String getSummary()
   {
      return summary;
   }

   @Override
   public CopyReference getDefaultCopyReference()
   {
      return defaultCopyReference;
   }

   @Override
   public Set<CopyReference> getCopyReferences()
   {
      return copyReferences;
   }

}