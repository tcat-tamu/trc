package edu.tamu.tcat.trc.services;

import java.util.Collection;

import edu.tamu.tcat.trc.services.bibref.ReferenceCollection;
import edu.tamu.tcat.trc.services.bibref.repo.EditBibliographyCommand;
import edu.tamu.tcat.trc.services.notes.Note;

/**
 * Provides a wrapper around a TRC Entry that can be used to link the entry
 * and a variety of core supporting services including tags, bibliographic references,
 * notes, categorizations and version history.
 *
 * @param <EntryType>
 */
public interface EntryMediator<EntryType>
{
   EntryType getEntry();

   Collection<Note> getNotes();

   ReferenceCollection getReferences();

   EditBibliographyCommand editReferences();
}
