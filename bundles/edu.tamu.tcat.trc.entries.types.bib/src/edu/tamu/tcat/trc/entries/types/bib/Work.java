package edu.tamu.tcat.trc.entries.types.bib;

import java.util.Collection;

import edu.tamu.tcat.trc.entries.repo.NoSuchCatalogRecordException;


/**
 * Bibliographic description for a book, article, journal or other work. This is the main
 * point of entry for working with bibliographic records.
 */
public interface Work
{
   // TODO rename to BibliographicEntry

   /**
    * @return A unique, persistent identifier for this work.
    */
   String getId();      // TODO create named ID type

   /**
    * A system that has multiple types of works in a single repository needs to be able to
    * distinguish between the different types.
    *
    * @return a free-form, machine-readable string that can be used by client software to
    *    distinguish work type.
    */
   String getType();

   /**
    * @return The authors of this work.
    */
   AuthorList getAuthors();

   /**
    * @return The title of this work.
    */
   TitleDefinition getTitle();

   /**
    * @return Secondary authors associated with this work. This corresponds to authors that
    *    would typically be displayed after the title information, such as the translator of a
    *    work. For example, in the entry Spinoza. <em>Tractatus Theologico-Politicus</em>.
    *    Trans by Willis. 1862. Willis would be the 'outher authors'.
    *
    */
   AuthorList getOtherAuthors();

   /**
    * @return Details about when, where and by whom this work was published.
    */
   @Deprecated
   PublicationInfo getPublicationInfo();

   /**
    * @return The editions associated with this work.
    */
   Collection<Edition> getEditions();

   /**
    * Obtain a particular edition of this work by edition ID.
    *
    * @param editionId
    * @return The edition associated with this work that possesses the given ID.
    * @throws NoSuchCatalogRecordException
    */
   Edition getEdition(String editionId) throws NoSuchCatalogRecordException;

   /**
    * @return A defined series of related works, typically published by a single publishers and
    *    issued under the direction of a series editor or editors.
    */
   String getSeries();                    // TODO make series a first-level entity

   /**
    * @return A brief summary of this work.
    */
   String getSummary();
}
