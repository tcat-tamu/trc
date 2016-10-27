package edu.tamu.tcat.trc.services.bibref.repo;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import edu.tamu.tcat.trc.resolver.EntryId;

public interface EditBibliographyCommand
{
   /**
    * @return A reference to the entry to which the bibliography being edited belongs.
    */
   EntryId getEntryReference();

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
    * Removes all citations from the bibliography
    */
   void removeAllCitations();

   /**
    * Creates a new bibliographic item with the given id and adds it to the bibliography.
    *
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

   /**
    * Removes all items from the bibliography
    */
   void removeAllItems();

   /**
    * Executes this command. All changes made using this command will take effect
    * only upon successful execution.
    *
    * @return A future that resolves to the id of the updated entry. If the execution fails
    *       for any reason, {@link CompletableFuture#get()} will throw an
    *       {@link ExecutionException} that wraps the cause of the failure.
    */
   CompletableFuture<String> execute();
}
