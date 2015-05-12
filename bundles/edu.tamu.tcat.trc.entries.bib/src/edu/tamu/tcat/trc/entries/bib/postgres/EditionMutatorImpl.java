package edu.tamu.tcat.trc.entries.bib.postgres;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import edu.tamu.tcat.catalogentries.InvalidDataException;
import edu.tamu.tcat.catalogentries.NoSuchCatalogRecordException;
import edu.tamu.tcat.trc.entries.bib.EditionMutator;
import edu.tamu.tcat.trc.entries.bib.VolumeMutator;
import edu.tamu.tcat.trc.entries.bib.dto.AuthorRefDV;
import edu.tamu.tcat.trc.entries.bib.dto.EditionDV;
import edu.tamu.tcat.trc.entries.bib.dto.PublicationInfoDV;
import edu.tamu.tcat.trc.entries.bib.dto.TitleDV;
import edu.tamu.tcat.trc.entries.bib.dto.VolumeDV;

public class EditionMutatorImpl implements EditionMutator
{
   private final EditionDV edition;
   private Supplier<String> volumeIdSupplier;


   /**
    * @param edition
    * @param volumeIdSupplier Supplier to generate IDs for volumes.
    */
   EditionMutatorImpl(EditionDV edition, Supplier<String> volumeIdSupplier)
   {
      this.edition = edition;
      this.volumeIdSupplier = volumeIdSupplier;
   }


   @Override
   public String getId()
   {
      return edition.id;
   }


   @Override
   public void setAll(EditionDV edition)
   {
      setAuthors(edition.authors);
      setTitles(edition.titles);
      setOtherAuthors(edition.otherAuthors);
      setEditionName(edition.editionName);
      setPublicationInfo(edition.publicationInfo);
      setSummary(edition.summary);
      setSeries(edition.series);

      setVolumes(edition.volumes);
   }


   private void setVolumes(List<VolumeDV> volumes)
   {
      // get IDs supplied by the client; after the update, these IDs should be the only ones in the database
      Set<String> clientIds = volumes.parallelStream()
            .map(v -> v.id)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

      // remove any volumes that were removed by the client
      edition.volumes.removeIf(v -> !clientIds.contains(v.id));

      // create or update client-supplied volumes
      for (VolumeDV volume : volumes) {
         VolumeMutator mutator;

         try {
            mutator = (null == volume.id) ? createVolume() : editVolume(volume.id);
         }
         catch (NoSuchCatalogRecordException e) {
            throw new InvalidDataException("Failed to edit existing volume. A supplied volume contains an id [" + volume.id + "], "
                  + "but the identified volume cannot be retrieved for editing.", e);

         }

         mutator.setAll(volume);
      }
   }

   @Override
   public void setAuthors(List<AuthorRefDV> authors)
   {
      edition.authors = new ArrayList<>(authors);
   }

   @Override
   public void setTitles(Collection<TitleDV> titles)
   {
      edition.titles = new HashSet<>(titles);
   }

   @Override
   public void setOtherAuthors(List<AuthorRefDV> otherAuthors)
   {
      edition.otherAuthors = new ArrayList<>(otherAuthors);
   }

   @Override
   public void setEditionName(String editionName)
   {
      this.edition.editionName = editionName;
   }

   @Override
   public void setPublicationInfo(PublicationInfoDV pubInfo)
   {
      edition.publicationInfo = pubInfo;
   }

   @Override
   public void setSummary(String summary)
   {
      edition.summary = summary;
   }

   @Override
   public void setSeries(String series)
   {
      edition.series = series;
   }

   @Override
   public VolumeMutator createVolume()
   {
      VolumeDV volume = new VolumeDV();
      volume.id = volumeIdSupplier.get();
      edition.volumes.add(volume);
      return new VolumeMutatorImpl(volume);
   }

   @Override
   public VolumeMutator editVolume(String id) throws NoSuchCatalogRecordException
   {
      for (VolumeDV volume : edition.volumes) {
         if (volume.id.equals(id)) {
            return new VolumeMutatorImpl(volume);
         }
      }
      throw new NoSuchCatalogRecordException("Unable to find volume with id [" + id + "].");
   }

}
