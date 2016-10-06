package edu.tamu.tcat.trc.impl.psql;

import static java.text.MessageFormat.format;

import java.util.Collection;

import edu.tamu.tcat.account.Account;
import edu.tamu.tcat.trc.EntryFacade;
import edu.tamu.tcat.trc.entries.core.repo.EntryRepositoryRegistry;
import edu.tamu.tcat.trc.resolver.EntryReference;
import edu.tamu.tcat.trc.resolver.EntryResolver;
import edu.tamu.tcat.trc.resolver.EntryResolverRegistry;
import edu.tamu.tcat.trc.resolver.InvalidReferenceException;
import edu.tamu.tcat.trc.services.ServiceContext;
import edu.tamu.tcat.trc.services.TrcServiceManager;
import edu.tamu.tcat.trc.services.bibref.ReferenceCollection;
import edu.tamu.tcat.trc.services.bibref.repo.EditBibliographyCommand;
import edu.tamu.tcat.trc.services.bibref.repo.RefCollectionService;
import edu.tamu.tcat.trc.services.notes.EditNoteCommand;
import edu.tamu.tcat.trc.services.notes.Note;
import edu.tamu.tcat.trc.services.notes.NotesService;

public class EntryFacadeImpl<EntryType> implements EntryFacade<EntryType>
{
   private final TrcServiceManager svcMgr;
   private final EntryReference ref;
   private final Account account;
   private final Class<EntryType> typeToken;

   private final EntryResolverRegistry resolvers;
   private final EntryResolver<Object> resolver;

   private ReferenceServiceDelegate refSvcDelegate;
   private EntryFacadeImpl<EntryType>.NoteServiceDelegate notesSvcDelegate;

   public EntryFacadeImpl(TrcServiceManager svcMgr, EntryRepositoryRegistry repoRegistry, EntryReference ref, Account acct, Class<EntryType> typeToken)
   {
      this.svcMgr = svcMgr;
      this.ref = ref;
      this.account = acct;
      this.typeToken = typeToken;

      resolvers = repoRegistry.getResolverRegistry();
      resolver = resolvers.getResolver(ref);

      Class<Object> refType = resolver.getType();
      if (!typeToken.isAssignableFrom(refType))
      {
         // fail fast if we have a type mismatch
         String string = "The supplied entry reference {0} is not compatible with the expected entry type {1}."
               + "The supplied entry is an instance of {2}";
         throw new InvalidReferenceException(ref, format(string, ref, typeToken, refType));
      }

      // spin up delgates to manage specific services
      refSvcDelegate = new ReferenceServiceDelegate();
      notesSvcDelegate = new NoteServiceDelegate();
   }

   @Override
   public synchronized EntryType getEntry()
   {
      Object entry = resolver.resolve(account, ref);
      return typeToken.cast(entry);
   }

   @Override
   public EntryReference getEntryRef()
   {
      // HACK: need immutable version
      EntryReference copy = new EntryReference();
      copy.id = ref.id;
      copy.type = ref.type;

      return copy;
   }

   @Override
   public ReferenceCollection getReferences()
   {
      return refSvcDelegate.getReferences();
   }

   @Override
   public Collection<Note> getNotes()
   {
      return notesSvcDelegate.getNotes();
   }

   @Override
   public EditNoteCommand addNote()
   {
      return notesSvcDelegate.create();
   }

   @Override
   public EditNoteCommand editNote(String id)
   {
      return notesSvcDelegate.edit(id);
   }

   @Override
   public EditBibliographyCommand editReferences()
   {
      return refSvcDelegate.editReferences();
   }

   private class ReferenceServiceDelegate
   {
      private final String token;
      private final RefCollectionService refSvc;

      public ReferenceServiceDelegate()
      {
         ServiceContext<RefCollectionService> ctx = RefCollectionService.makeContext(account);
         refSvc = svcMgr.getService(ctx);
         token = resolvers.tokenize(ref);
      }

      public ReferenceCollection getReferences()
      {
         return refSvc.get(token);
      }

      public EditBibliographyCommand editReferences()
      {
         return refSvc.edit(token);
      }
   }

   private class NoteServiceDelegate
   {
      private final NotesService notesSvc;

      public NoteServiceDelegate()
      {
         // TODO will need to allow for multiple contexts
         notesSvc = svcMgr.getService(NotesService.makeContext(account));
      }

      public Collection<Note> getNotes()
      {
         return notesSvc.getNotes(ref);
      }

      public EditNoteCommand edit(String id)
      {
         return notesSvc.edit(id);
      }

      public EditNoteCommand create()
      {
         return notesSvc.create();
      }
   }
}
