package edu.tamu.tcat.trc;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import edu.tamu.tcat.trc.entries.core.repo.EntryRepository;
import edu.tamu.tcat.trc.resolver.EntryId;
import edu.tamu.tcat.trc.resolver.EntryResolver;
import edu.tamu.tcat.trc.services.bibref.ReferenceCollection;
import edu.tamu.tcat.trc.services.bibref.repo.EditBibliographyCommand;
import edu.tamu.tcat.trc.services.notes.EditNoteCommand;
import edu.tamu.tcat.trc.services.notes.Note;
import edu.tamu.tcat.trc.services.seealso.Link;

/**
 * Provides a wrapper around a TRC Entry that can be used to link the entry
 * and a variety of core supporting services including tags, bibliographic references,
 * notes, categorizations and version history.
 *
 * @param <EntryType>
 */
public interface EntryFacade<EntryType>
{
   /**
    * @return The entry represented by this facade.
    */
   Optional<EntryType> getEntry() throws ResourceNotFoundException;

   EntryId getEntryId();

   String getToken();

   EntryResolver<EntryType> getResolver();

   /**
    * @return A collection of bibliographic references and citations that are associated
    *       with this entry.
    */
   ReferenceCollection getReferences();

   /**
    * @return A set of notes associated with this entry.
    */
   Collection<Note> getNotes();

   /**
    * Create a new note that will be associated with this entry.
    *
    * @return A command to edit the newly created note.
    */
   EditNoteCommand addNote();

   EditNoteCommand editNote(String id);

   EditBibliographyCommand editReferences();

   /**
    * Adds a new 'See Also' link from this entry to the referenced entry.
    *
    * @param ref A reference to the entry to link to.
    * @return The created link.
    */
   Link addLink(EntryId ref);

   /**
    * Removes a 'See Also' link from this entry.
    *
    * @param ref The reference whose link should be deleted.
    * @return Indicates whether the underlying data was changed. Will be <code>false</code>
    *       if the referenced entry was not linked to this entry.
    */
   boolean removeLink(EntryId ref);

   /**
    * @return A collection of entries that are linked to this one.
    */
   Collection<EntryId> getLinks();     // TODO do we need to distinguish directionality?

   /**
    * Removes the referenced entry and all associated service content. In general, this method
    * should be preferred to {@link EntryRepository#remove(String)} in order to ensure that
    * linked resources are properly cleaned.
    *
    * @return A future that will pass through any exceptions encountered while
    *       attempting to remove this entry.
    */
   CompletableFuture<Void> remove();


   // TODO versioning,
   //      meta information (removed, author, etc)
   //      ACL
   //      Tags
   //      Publication
   //      Categorizations
   //      TRC Relationships
}
