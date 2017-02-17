package edu.tamu.tcat.trc.editorial.jaxrs.tasks;

import java.util.Optional;
import java.util.logging.Level;

import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import edu.tamu.tcat.account.jaxrs.bean.ContextBean;
import edu.tamu.tcat.account.jaxrs.bean.TokenSecured;
import edu.tamu.tcat.trc.TrcApplication;
import edu.tamu.tcat.trc.auth.account.TrcAccount;
import edu.tamu.tcat.trc.editorial.api.tasks.EditorialTask;
import edu.tamu.tcat.trc.editorial.api.tasks.EditorialTaskManager;
import edu.tamu.tcat.trc.editorial.api.workflow.Workflow;
import edu.tamu.tcat.trc.editorial.jaxrs.internal.ApiUtils;
import edu.tamu.tcat.trc.entries.core.repo.EntryRepositoryRegistry;

/**
 * Represents a single editorial task.
 */
public class EditorialTaskResource
{
   private final String taskId;
   private final EntryRepositoryRegistry repositories;

   private final TrcApplication trcCtx;

   public EditorialTaskResource(String id, TrcApplication trcCtx)
   {
      this.taskId = id;
      this.trcCtx = trcCtx;
      this.repositories = trcCtx.getEntryRepositoryManager();
   }

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   @TokenSecured(payloadType=TrcAccount.class)
   public RestApiV1.EditorialTask get(@BeanParam ContextBean bean)
   {
      TrcAccount account = bean.get(TrcAccount.class);

      EditorialTask task = loadTask(account);
      return ModelAdapter.adapt(task);
   }

   @GET
   @Path("workflow")
   @Produces(MediaType.APPLICATION_JSON)
   @TokenSecured(payloadType=TrcAccount.class)
   public RestApiV1.Workflow getWorkflow(@BeanParam ContextBean bean)
   {
      TrcAccount account = bean.get(TrcAccount.class);
      Workflow workflow = loadTask(account).getWorkflow();

      return ModelAdapter.adapt(workflow);
   }

   /**
    * @return No Content if the specified task exists, Not Found if it does not exist.
    */
   @GET
   @Path("exists")
   @Produces(MediaType.APPLICATION_JSON)
   public Response exists()
   {
      return getManager(null).exists(taskId)
            ? Response.noContent().build()
            : Response.status(Status.NOT_FOUND).build();
   }

   @GET
   @Path("items")
   public void getItems()
   {
      // TODO support to query/filter and return work itmes
   }

   // TODO ability to create new work items ??
   // TODO ability to edit state of work items
   private EditorialTaskManager getManager(TrcAccount account)
   {
      return trcCtx.getService(EditorialTaskManager.makeContext(account));
   }

   private EditorialTask loadTask(TrcAccount account)
   {
      EditorialTaskManager manager = trcCtx.getService(EditorialTaskManager.makeContext(account));

      Optional<EditorialTask> task = manager.get(this.taskId);
      return task.orElseThrow(() -> ApiUtils.raise(Status.NOT_FOUND, "", Level.INFO, null));
   }

}
