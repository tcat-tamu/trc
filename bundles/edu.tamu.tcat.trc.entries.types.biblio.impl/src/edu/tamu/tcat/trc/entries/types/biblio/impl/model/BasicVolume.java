package edu.tamu.tcat.trc.entries.types.biblio.impl.model;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import edu.tamu.tcat.trc.entries.types.biblio.AuthorReference;
import edu.tamu.tcat.trc.entries.types.biblio.CopyReference;
import edu.tamu.tcat.trc.entries.types.biblio.PublicationInfo;
import edu.tamu.tcat.trc.entries.types.biblio.Title;
import edu.tamu.tcat.trc.entries.types.biblio.Volume;
import edu.tamu.tcat.trc.entries.types.biblio.impl.repo.DataModelV1;

public class BasicVolume implements Volume
{
   private final String id;
   private final String volumeNumber;
   private final PublicationInfo publicationInfo;
   private final List<AuthorReference> authors;
   private final Collection<Title> titles;
   private final List<AuthorReference> otherAuthors;
   private final String series;
   private final String summary;
   private final CopyReference defaultCopyReference;
   private final Set<CopyReference> copyReferences;

   public BasicVolume(DataModelV1.VolumeDTO dto)
   {
      this.id = dto.id;

      this.series = dto.series;
      this.summary = dto.summary;
      this.volumeNumber = dto.volumeNumber;
      this.publicationInfo = new BasicPublicationInfo(dto.publicationInfo);

      this.authors = dto.authors != null
            ? dto.authors.stream().map(BasicAuthorReference::new).collect(toList())
            : Collections.emptyList();

      this.titles = dto.titles != null
            ? dto.titles.stream().map(BasicTitle::new).collect(toList())
            : Collections.emptyList();

      this.otherAuthors = dto.otherAuthors != null
            ? dto.otherAuthors.stream().map(BasicAuthorReference::new).collect(toList())
            : Collections.emptyList();

      this.copyReferences = dto.copyReferences != null
            ? dto.copyReferences.stream().map(BasicCopyReference::new).collect(toSet())
            : Collections.emptySet();

      this.defaultCopyReference = copyReferences.stream()
            .filter(copyReference -> Objects.equals(copyReference.getId(), dto.defaultCopyReferenceId))
            .findFirst()
            .orElse(!copyReferences.isEmpty() ? copyReferences.iterator().next() : null);
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