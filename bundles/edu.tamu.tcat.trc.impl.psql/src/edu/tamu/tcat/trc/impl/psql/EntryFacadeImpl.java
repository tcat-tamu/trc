package edu.tamu.tcat.trc.impl.psql;

import static java.text.MessageFormat.format;
import static java.util.stream.Collectors.toList;

import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

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
import edu.tamu.tcat.trc.services.seealso.Link;
import edu.tamu.tcat.trc.services.seealso.SeeAlsoService;

public class EntryFacadeImpl<EntryType> implements EntryFacade<EntryType>
{
   private final TrcServiceManager svcMgr;
   private final EntryReference ref;
   private final Account account;
   private final Class<EntryType> typeToken;

   private final EntryResolverRegistry resolvers;
   private final EntryResolver<Object> resolver;

   private final ReferenceServiceDelegate refSvcDelegate;
   private final NoteServiceDelegate notesSvcDelegate;
   private final SeeAlsoSvcDelegate seeAlsoSvcDelegate;

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
      seeAlsoSvcDelegate = new SeeAlsoSvcDelegate();
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

   @Override
   public Link addLink(EntryReference ref)
   {
      return seeAlsoSvcDelegate.link(ref);
   }

   @Override
   public Collection<EntryReference> getLinks()
   {
      return seeAlsoSvcDelegate.getLinks();
   }

   @Override
   public boolean removeLink(EntryReference ref)
   {
      return seeAlsoSvcDelegate.removeLink(ref);
   }

   @Override
   public CompletableFuture<Void> remove()
   {
      return resolver.remove(account, ref)
                 .thenAccept(flag -> seeAlsoSvcDelegate.remove())
                 .thenAccept(flag -> notesSvcDelegate.remove())
                 .thenAccept(flag -> refSvcDelegate.remove());
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

      public void remove()
      {
         refSvc.delete(token);
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

      public void remove()
      {
         notesSvc.getNotes(ref).stream()
               .forEach(note -> notesSvc.remove(note.getId()));
      }
   }

   private class SeeAlsoSvcDelegate
   {
      private SeeAlsoService service;
      private String source;

      public SeeAlsoSvcDelegate()
      {
         service = svcMgr.getService(SeeAlsoService.makeContext(account));
         source = Objects.requireNonNull(resolvers.tokenize(ref));
      }

      public Collection<EntryReference> getLinks()
      {
         return service.getFor(source).stream()
               .map(link -> source.equals(link.getSource()) ? link.getTarget() : link.getSource())
               .map(resolvers::decodeToken)
               .collect(toList());
      }

      public Link link(EntryReference ref)
      {
         String target = resolvers.tokenize(ref);
         return service.create(source, target);
      }

      public boolean removeLink(EntryReference ref)
      {
         String target = resolvers.tokenize(ref);
         return service.delete(source, target);
      }

      public boolean remove()
      {
         return service.delete(source);
      }


   }
}
