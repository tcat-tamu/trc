package edu.tamu.tcat.trc.services.rest.notes;

import java.util.Objects;

import javax.ws.rs.Path;

import edu.tamu.tcat.trc.entries.core.repo.EntryRepositoryRegistry;
import edu.tamu.tcat.trc.impl.psql.services.notes.NotesServiceFactory;
import edu.tamu.tcat.trc.resolver.EntryResolverRegistry;
import edu.tamu.tcat.trc.services.ServiceContext;
import edu.tamu.tcat.trc.services.TrcServiceManager;
import edu.tamu.tcat.trc.services.notes.NotesService;
import edu.tamu.tcat.trc.services.rest.notes.v1.NotesCollectionResource;


@Path("/")
public class NotesApiService
{
   private NotesServiceFactory notesSvc;

   private EntryResolverRegistry resolvers;

   private TrcServiceManager svcManager;

   public void bind(TrcServiceManager svcManager)
   {
      this.svcManager = svcManager;
   }

   public void bind(EntryRepositoryRegistry repoRegistry)
   {
      this.resolvers = repoRegistry.getResolverRegistry();
   }

   public void setResolvers(EntryResolverRegistry resolvers)
   {
      this.resolvers = resolvers;
   }

   public void activate()
   {
      // FIXME
      Objects.requireNonNull(notesSvc, "Notes Repsoitory was not setup correctly.");
   }

   public void dispose()
   {
      notesSvc = null;
   }

   @Path("services/notes")
   public NotesCollectionResource getNotes()
   {
      ServiceContext<NotesService> ctx = NotesService.makeContext(null, "sda");
      NotesService service = svcManager.getService(ctx);
      return new NotesCollectionResource(service, resolvers, null);
   }
}
