package edu.tamu.tcat.trc;

import java.util.Collection;

import edu.tamu.tcat.trc.resolver.EntryReference;
import edu.tamu.tcat.trc.services.bibref.ReferenceCollection;
import edu.tamu.tcat.trc.services.bibref.repo.EditBibliographyCommand;
import edu.tamu.tcat.trc.services.notes.EditNoteCommand;
import edu.tamu.tcat.trc.services.notes.Note;

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
   EntryType getEntry();

   EntryReference getEntryRef();

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
    * @return A command to edit the newly created note.
    */
   EditNoteCommand addNote();

   EditNoteCommand editNote(String id);

   EditBibliographyCommand editReferences();

   // TODO versioning,
   //      meta information (removed, author, etc)
   //      ACL
   //      Tags
   //      SeeAlso
   //      Publication
   //      Categorizations
}
