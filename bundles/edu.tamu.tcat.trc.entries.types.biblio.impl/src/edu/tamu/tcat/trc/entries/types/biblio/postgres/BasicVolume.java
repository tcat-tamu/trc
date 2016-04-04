package edu.tamu.tcat.trc.entries.types.biblio.postgres;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import edu.tamu.tcat.trc.entries.types.biblio.AuthorReference;
import edu.tamu.tcat.trc.entries.types.biblio.PublicationInfo;
import edu.tamu.tcat.trc.entries.types.biblio.Title;
import edu.tamu.tcat.trc.entries.types.biblio.Volume;
import edu.tamu.tcat.trc.entries.types.biblio.copies.CopyReference;

public class BasicVolume implements Volume
{
   String id;
   String volumeNumber;
   PublicationInfo publicationInfo;
   List<AuthorReference> authors;
   Collection<Title> titles;
   List<AuthorReference> otherAuthors;
   String series;
   String summary;
   CopyReference defaultCopyReference;
   Set<CopyReference> copyReferences;

   public BasicVolume(String id,
                      String volumeNumber,
                      PublicationInfo publicationInfo,
                      List<AuthorReference> authors,
                      Collection<Title> titles,
                      List<AuthorReference> otherAuthors,
                      String series,
                      String summary,
                      CopyReference defaultCopyReference,
                      Set<CopyReference> copyReferences)
   {
      this.id = id;
      this.volumeNumber = volumeNumber;
      this.publicationInfo = publicationInfo;
      this.authors = authors;
      this.titles = titles;
      this.otherAuthors = otherAuthors;
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