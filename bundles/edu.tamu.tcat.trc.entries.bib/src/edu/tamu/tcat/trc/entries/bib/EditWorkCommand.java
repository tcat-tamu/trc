package edu.tamu.tcat.trc.entries.bib;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Future;

import edu.tamu.tcat.catalogentries.NoSuchCatalogRecordException;
import edu.tamu.tcat.trc.entries.bib.dto.AuthorRefDV;
import edu.tamu.tcat.trc.entries.bib.dto.TitleDV;
import edu.tamu.tcat.trc.entries.bib.dto.WorkDV;

/**
 * Used to edit a {@link Work}. This class allows clients to make updates to a {@link Work}
 * instance and its component elements (e.g., {@link Edition}s and {@link Volume}s) in a multi-step
 * transaction and to commit those changes to the persistence layer via its
 * {@link #execute()} method.
 *
 * <p>Note that implementations typically are not thread safe.
 *
 * @see WorkRepository#create()
 * @see WorkRepository#edit(String)
 */
public interface EditWorkCommand
{
   // TODO: Should these methods take in full models or data vehicles?
   //       Should there be methods to handle both data types?

   /**
    * Sets all properties defined in the supplied {@link WorkDV} (i.e., non-null values).
    *
    * @param work The data vehicle to be used to update the work being edited.
    */
   void setAll(WorkDV work);

   // TODO: Any field that is a collection of models should eventually use mutators.

   /**
    * Updates the list of authors.
    * @param authors
    */
   void setAuthors(List<AuthorRefDV> authors);

   /**
    *
    * @param titles
    */
   void setTitles(Collection<TitleDV> titles);

   /**
    *
    * @param authors
    */
   void setOtherAuthors(List<AuthorRefDV> authors);

   /**
    *
    * @param series
    */
   void setSeries(String series);

   /**
    *
    * @param summary
    */
   void setSummary(String summary);

   /**
    * Creates an edition mutator to update fields on an existing edition of this work.
    *
    * @param id The ID of a contained edition.
    * @return A mutator for the given edition ID.
    */
   EditionMutator editEdition(String id) throws NoSuchCatalogRecordException;

   /**
    * Creates an edition mutator for a new edition of this work.
    *
    * @return
    */
   EditionMutator createEdition();

   /**
    * Removes the specified edition from the work.
    */
   void removeEdition(String editionId) throws NoSuchCatalogRecordException;

   /**
    * Removed the specified volume from the work.
    */
   void removeVolume(String volumeId) throws NoSuchCatalogRecordException;

   /**
    *
    * @return The id of the created or edited work.
    */
   Future<String> execute();
}
