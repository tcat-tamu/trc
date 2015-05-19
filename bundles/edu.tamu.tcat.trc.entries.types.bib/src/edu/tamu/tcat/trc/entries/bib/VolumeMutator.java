package edu.tamu.tcat.trc.entries.bib;

import java.util.Collection;
import java.util.List;

import edu.tamu.tcat.trc.entries.bib.dto.AuthorRefDV;
import edu.tamu.tcat.trc.entries.bib.dto.PublicationInfoDV;
import edu.tamu.tcat.trc.entries.bib.dto.TitleDV;
import edu.tamu.tcat.trc.entries.bib.dto.VolumeDV;

/**
 * Used to edit the properties of a {@link Volume}. A {@code VolumeMutator} is created
 * within the transactional scope of an {@link EditWorkCommand} via either the
 * {@link EditionMutator#createVolume()} or the {@link EditionMutator#editVolume(String)}
 * method. Changes made to the {@code Volume} modified by this mutator will take effect
 * when the parent {@link EditWorkCommand#execute()} method is invoked. Changes made after
 * this command's {@code execute()} method is called will have indeterminate affects.
 *
 * <p>
 * Implementations are typically not threadsafe.
 */
public interface VolumeMutator
{
   /**
    *
    * @return The unique identifier for the volume that this mutator modifies.
    *         Will not be {@code null}. For newly created volumes, this identifier
    *         will be assigned when the java object is first created rather than when
    *         the volume is committed to the persistence layer.
    */
   String getId();

   /**
    * Sets all information in the supplied data vehicle into this mutator.
    * @param volume the data to set.
    */
   void setAll(VolumeDV volume);

   /**
    * @param volumeNumber The number or other identifier for this volume.
    */
   void setVolumeNumber(String volumeNumber);

   /**
    * @param authors The list of author responsible for creating this volume
    */
   void setAuthors(List<AuthorRefDV> authors);

   /**
    * @param titles The title(s) associated with this volume.
    */
   void setTitles(Collection<TitleDV> titles);

   /**
    * @param otherAuthors The list of other authors who contributed to the creation of this
    *       volume.
    */
   void setOtherAuthors(List<AuthorRefDV> otherAuthors);

   /**
    * @param info The publication info for this volume.
    */
   void setPublicationInfo(PublicationInfoDV info);

   /**
    *
    * @param series The series this volume belongs to.
    */
   void setSeries(String series);

   /**
    *
    * @param summary An editorial summary of this volume.
    */
   void setSummary(String summary);



}
