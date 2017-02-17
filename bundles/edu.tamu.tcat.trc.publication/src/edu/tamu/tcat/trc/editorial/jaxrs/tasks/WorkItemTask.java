package edu.tamu.tcat.trc.editorial.jaxrs.tasks;

import static java.text.MessageFormat.format;

import java.util.logging.Level;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import edu.tamu.tcat.trc.editorial.api.tasks.EditorialTask;
import edu.tamu.tcat.trc.editorial.api.tasks.WorkItem;
import edu.tamu.tcat.trc.editorial.jaxrs.internal.ApiUtils;
import edu.tamu.tcat.trc.resolver.EntryResolverRegistry;

public class WorkItemTask
{

   private final EditorialTask task;
   private final String itemId;
   private final EntryResolverRegistry resolvers;

   public WorkItemTask(EntryResolverRegistry resolvers, EditorialTask task, String itemId)
   {
      this.resolvers = resolvers;
      this.task = task;
      this.itemId = itemId;
   }

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   public RestApiV1.WorkItem getItem()
   {
      return ModelAdapter.adapt(resolvers, loadItem());
   }

   @POST
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   public RestApiV1.WorkItem transition()
   {
      return null;
   }

   private WorkItem loadItem()
   {
      String msg = "No such work item {0}";
      return task.getItem(itemId)
            .orElseThrow(() -> ApiUtils.raise(Status.NOT_FOUND, format(msg, itemId), Level.FINE, null));
   }
}
