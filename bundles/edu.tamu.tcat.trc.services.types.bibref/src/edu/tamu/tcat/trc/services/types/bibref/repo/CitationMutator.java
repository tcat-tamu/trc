package edu.tamu.tcat.trc.services.types.bibref.repo;

public interface CitationMutator
{
   /**
    * @return The id of the referenced item.
    */
   String getId();

   /**
    * Creates a new bibliographic item reference to the bibliographic item with the given id and adds it to the citation.
    * @param itemId The id of the bibliographic item to reference.
    * @return A mutator to set values on the newly created bibliographic item reference.
    */
   BibliographicItemReferenceMutator addItemRef(String itemId);

   /**
    * @param itemId The id of the bibliographic item whose reference to edit.
    * @return A mutator to edit the bibliographic item reference.
    */
   BibliographicItemReferenceMutator editItemRef(String itemId);

   /**
    * Removes a bibliographic item reference from the citation.
    * @param itemId The id of the bibliographic item reference to remove.
    */
   void removeItemRef(String itemId);

   // TODO declare methods to set the order of item references
}
