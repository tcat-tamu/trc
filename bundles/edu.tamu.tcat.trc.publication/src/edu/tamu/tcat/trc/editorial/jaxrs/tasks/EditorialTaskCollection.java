package edu.tamu.tcat.trc.editorial.jaxrs.tasks;

import static java.text.MessageFormat.format;
import static java.util.stream.Collectors.toList;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;
import java.util.logging.Level;

import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import edu.tamu.tcat.account.jaxrs.bean.ContextBean;
import edu.tamu.tcat.account.jaxrs.bean.TokenSecured;
import edu.tamu.tcat.trc.TrcApplication;
import edu.tamu.tcat.trc.auth.account.TrcAccount;
import edu.tamu.tcat.trc.editorial.api.tasks.EditorialTask;
import edu.tamu.tcat.trc.editorial.api.tasks.EditorialTaskManager;
import edu.tamu.tcat.trc.editorial.api.tasks.EditorialTaskManager.TaskDescription;
import edu.tamu.tcat.trc.editorial.jaxrs.internal.ApiUtils;
import edu.tamu.tcat.trc.entries.core.repo.EntryRepository;
import edu.tamu.tcat.trc.entries.core.repo.RepositoryReference;
import edu.tamu.tcat.trc.resolver.EntryReference;
import edu.tamu.tcat.trc.resolver.EntryResolverRegistry;

public class EditorialTaskCollection
{
   private final TrcApplication trcCtx;

   public EditorialTaskCollection(TrcApplication trcCtx)
   {
      this.trcCtx = trcCtx;
   }

   /**
    * List all defined editorial tasks
    */
   @GET
   @Produces(MediaType.APPLICATION_JSON)
   @TokenSecured(payloadType=TrcAccount.class)
   public List<RestApiV1.EditorialTask> get(@BeanParam ContextBean bean)
   {
      TrcAccount account = bean.get(TrcAccount.class);

      List<EditorialTask> tasks = getManager(account).listTasks();
      return tasks.stream().map(ModelAdapter::adapt).collect(toList());
   }

   @Path("{taskId}")
   public EditorialTaskResource getTask(@PathParam("id") String taskId)
   {
      return new EditorialTaskResource(taskId, trcCtx);
   }

   @POST
   @Produces
   @TokenSecured(payloadType=TrcAccount.class)
   public RestApiV1.EditorialTask create(@BeanParam ContextBean bean,
                                         @QueryParam("entryType") String type,
                                         RestApiV1.EditorialTask defn)
   {
      TrcAccount account = bean.get(TrcAccount.class);

      EditorialTask task = getManager(account).create(adapt(defn));

      Collection<RepositoryReference<?>> repoRefs = trcCtx.getEntryRepositoryManager().listRepositories();
      EntryRepository<?> repo = repoRefs.stream()
         .filter(ref -> ref.getName().equals(type))
         .findFirst()
         .map(ref -> ref.get(account))
         .orElseThrow(() -> ApiUtils.raise(Status.BAD_REQUEST, format("No editorial task defined for entry type {0}", type), Level.WARNING, null));

      // TODO run in the background
      task.addItems(makeEntryRefSupplier(repo), null);
      return ModelAdapter.adapt(task);
   }

   private EditorialTaskManager getManager(TrcAccount account)
   {
      return trcCtx.getService(EditorialTaskManager.makeContext(account));
   }

   private <X> Supplier<EntryReference<X>> makeEntryRefSupplier(EntryRepository<X> repo)
   {
      // TODO why is this duplicated in the EtitorialTaskResource?
      EntryResolverRegistry resolvers = trcCtx.getResolverRegistry();
      Iterator<X> iterator = repo.listAll();
      return () -> {
         while (iterator.hasNext())
         {
            X entry = iterator.next();
            if (entry != null)
               return resolvers.getReference(entry);
         }

         return null;
      };
   }


   public static TaskDescription adapt(RestApiV1.EditorialTask defn)
   {
      TaskDescription result = new TaskDescription();
      result.id = defn.id;
      result.name = defn.name;
      result.description = defn.description;
      result.workflowId = defn.workflowId;

      return result;
   }
}
