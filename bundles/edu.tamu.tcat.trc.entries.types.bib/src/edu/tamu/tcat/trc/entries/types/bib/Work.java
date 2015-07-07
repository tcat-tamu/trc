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
    * Applications may need to distinguish between multiple types of works stored within a
    * single repository for analytical purposes.
    *
    * @return a application-defined type identifier for this work.
    * @deprecated we should probably support the use of multiple WorkRepositories to
    *       represent different types of bibliographic entries. Applications can then look
    *       up the repo that they need.
    */
   @Deprecated  // FIXME we should probably create multiple WorkRepos to represent the
                //       different work types and
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
