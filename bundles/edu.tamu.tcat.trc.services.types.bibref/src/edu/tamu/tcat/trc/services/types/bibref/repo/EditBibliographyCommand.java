package edu.tamu.tcat.trc.services.types.bibref.repo;

import edu.tamu.tcat.trc.entries.core.repo.EditEntryCommand;
import edu.tamu.tcat.trc.entries.core.resolver.EntryReference;
import edu.tamu.tcat.trc.services.types.bibref.ReferenceCollection;

public interface EditBibliographyCommand extends EditEntryCommand<ReferenceCollection>
{
   /**
    * @return A reference to the entry to which the bibliography being edited belongs.
    */
   EntryReference getEntryReference();

   /**
    * Creates a new citation with the given id and adds it to the bibliography.
    * @param citationId The id of the bibliographic item to create.
    * @return A mutator to set values on the newly added citation.
    */
   CitationMutator addCitation(String citationId);

   /**
    * @param citationId The id of the citation to edit
    * @return A mutator to edit the citation.
    */
   CitationMutator editCitation(String citationId);

   /**
    * Removes a citation from the bibliography.
    * @param citationId The id of the citation to remove.
    */
   void removeCitation(String citationId);

   /**
    * Creates a new bibliographic item with the given id and adds it to the bibliography.
    * @param itemId The id of the bibliographic item to create.
    * @return A mutator to set values on the newly created bibliographic item.
    */
   BibliographicItemMutator addItem(String itemId);

   /**
    * @param itemId The id of the bibliographic item to edit.
    * @return A mutator to edit the bibliographic item.
    */
   BibliographicItemMutator editItem(String itemId);

   /**
    * Removes a bibliographic item from the bibliography.
    * @param itemId The id of the bibliographic item to remove.
    */
   void removeItem(String itemId);
}
