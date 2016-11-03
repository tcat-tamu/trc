package edu.tamu.tcat.trc.impl.psql;

import static java.text.MessageFormat.format;
import static java.util.stream.Collectors.toList;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import edu.tamu.tcat.account.Account;
import edu.tamu.tcat.trc.EntryFacade;
import edu.tamu.tcat.trc.TrcApplication;
import edu.tamu.tcat.trc.resolver.EntryId;
import edu.tamu.tcat.trc.resolver.EntryResolver;
import edu.tamu.tcat.trc.resolver.EntryResolverRegistry;
import edu.tamu.tcat.trc.resolver.InvalidReferenceException;
import edu.tamu.tcat.trc.services.ServiceContext;
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
   private final EntryId ref;
   private final Account account;
   private final Class<EntryType> typeToken;

   private final ReferenceServiceDelegate refSvcDelegate;
   private final NoteServiceDelegate notesSvcDelegate;
   private final SeeAlsoSvcDelegate seeAlsoSvcDelegate;

   private Optional<EntryType> entry = null;
   private TrcApplication trcCtx;

   public EntryFacadeImpl(EntryId ref, Class<EntryType> typeToken, Account acct, TrcApplication trcCtx)
   {
      this.trcCtx = trcCtx;

      this.ref = ref;
      this.account = acct;
      this.typeToken = typeToken;

      checkTypeCompatibility(ref, typeToken, trcCtx);

      // spin up delgates to manage specific services
      refSvcDelegate = new ReferenceServiceDelegate();
      notesSvcDelegate = new NoteServiceDelegate();
      seeAlsoSvcDelegate = new SeeAlsoSvcDelegate();
   }

   public EntryFacadeImpl(EntryType instance, Class<EntryType> typeToken, Account acct, TrcApplication trcCtx)
   {
      this.trcCtx = trcCtx;

      this.entry = Optional.of(instance);
      this.ref = trcCtx.getResolverRegistry().getResolver(instance).makeReference(instance);

      this.account = acct;
      this.typeToken = typeToken;

      checkTypeCompatibility(ref, typeToken, trcCtx);

      // spin up delgates to manage specific services
      refSvcDelegate = new ReferenceServiceDelegate();
      notesSvcDelegate = new NoteServiceDelegate();
      seeAlsoSvcDelegate = new SeeAlsoSvcDelegate();
   }

   private static void checkTypeCompatibility(EntryId ref, Class<?> typeToken, TrcApplication trcCtx)
   {
      EntryResolver<?> resolver = trcCtx.getResolverRegistry().getResolver(ref);
      Class<?> refType = resolver.getType();
      if (!typeToken.isAssignableFrom(refType))
      {
         // fail fast if we have a type mismatch
         String string = "The supplied entry reference {0} is not compatible with the expected entry type {1}."
               + "The supplied entry is an instance of {2}";
         throw new InvalidReferenceException(ref, format(string, ref, typeToken, refType));
      }
   }

   @Override
   public synchronized Optional<EntryType> getEntry()
   {
      if (entry == null)
      {
         // HACK this is awkward
         EntryResolver<Object> resolver = trcCtx.getResolverRegistry().getResolver(ref);
         entry = resolver.resolve(account, ref).map(typeToken::cast);
      }

      return entry;
   }


   @Override
   public EntryId getEntryId()
   {
      return ref;
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
   public Link addLink(EntryId ref)
   {
      return seeAlsoSvcDelegate.link(ref);
   }

   @Override
   public Collection<EntryId> getLinks()
   {
      return seeAlsoSvcDelegate.getLinks();
   }

   @Override
   public boolean removeLink(EntryId ref)
   {
      return seeAlsoSvcDelegate.removeLink(ref);
   }

   @Override
   public CompletableFuture<Void> remove()
   {
      EntryResolver<Object> resolver = trcCtx.getResolverRegistry().getResolver(ref);
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
         refSvc = trcCtx.getService(ctx);
         token = trcCtx.getResolverRegistry().tokenize(ref);
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
         notesSvc = trcCtx.getService(NotesService.makeContext(account));
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
         service = trcCtx.getService(SeeAlsoService.makeContext(account));
         source = Objects.requireNonNull(trcCtx.getResolverRegistry().tokenize(ref));
      }

      public Collection<EntryId> getLinks()
      {
         EntryResolverRegistry resolvers = trcCtx.getResolverRegistry();
         return service.getFor(source).stream()
               .map(link -> source.equals(link.getSource()) ? link.getTarget() : link.getSource())
               .map(resolvers::decodeToken)
               .collect(toList());
      }

      public Link link(EntryId ref)
      {
         String target = trcCtx.getResolverRegistry().tokenize(ref);
         return service.create(source, target);
      }

      public boolean removeLink(EntryId ref)
      {
         String target = trcCtx.getResolverRegistry().tokenize(ref);
         return service.delete(source, target);
      }

      public boolean remove()
      {
         return service.delete(source);
      }


   }
}
