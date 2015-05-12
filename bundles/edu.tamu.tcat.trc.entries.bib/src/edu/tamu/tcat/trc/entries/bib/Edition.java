package edu.tamu.tcat.trc.entries.bib;

import java.util.Collection;
import java.util.List;

import edu.tamu.tcat.catalogentries.NoSuchCatalogRecordException;

/**
 * Editions are published manifestations of a {@link Work}. An edition adds specific information related to the
 */
public interface Edition
{
   /**
    * @return A unique system identifier for this edition.
    */
   String getId();

   /**
    * The authors or other individuals responsible for the creation of this work. Note that different
    * editions of the same work may have different authors, for example, if an author joined or left
    * a multi-authored work.
    *
    * @return The authors of this edition.
    */
   List<AuthorReference> getAuthors();

   /**
    * The title(s) of this edition. Note that an individual edition may have different titles when,
    * for example, it is referenced using a shorter version of the full title; a title's spelling is
    * normalized or a title is translated. An edition may have a different title from the work it
    * is associated with.
    *
    * @return The titles associated with this edition.
    */
   Collection<Title> getTitles();

   /**
    * Other individuals who played a role in the creation of this work, but who are not primarily
    * responsible for its creation. Translators are a common example.
    *
    * @return The other authors associated with this work.
    */
   List<AuthorReference> getOtherAuthors();

   /**
    * The name or other identifier for this edition of the work. Editions of a work are frequently represented
    * as ordinals (2<sup>nd</sup>, 1<sup>st</sup>) but may use other conventions (e.g. 12 vol. ed; 1910-1915 edition;
    * anniversary edition). This value may be omitted if there is only one edition of the Work.
    *
    * @return The identifier for this edition of the work or {@code null} if no edition identifier is supplied.
    */
   String getEditionName();

   /**
    * Editions are physical manifestations of {@link Work}s.
    *
    * @return
    */
   PublicationInfo getPublicationInfo();

   /**
    * Editions consist of of at least one and possibly multiple {@link Volume}s.
    *
    * @return The volumes in which this work was published.
    */
   List<Volume> getVolumes();

   /**
    * Get volume by its identifier.
    *
    * @param volumeId
    * @return The volume of this edition that corresponds to the given id
    * @throws NoSuchCatalogRecordException
    */
   Volume getVolume(String volumeId) throws NoSuchCatalogRecordException;

   /**
    * Series to which the edition belongs
    *
    * @return The name of the series to which this edition belongs. May be {@code null} or the empty string
    *       if this work is not part of a series.
    */
   String getSeries();

   /**
    * @return An editorial summary of this edition. Typically 150 to 300 words.
    */
   String getSummary();
}
