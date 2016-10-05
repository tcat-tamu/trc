package edu.tamu.tcat.trc.impl.psql.services.categorization.repo;

import static java.text.MessageFormat.format;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import edu.tamu.tcat.trc.repo.BasicChangeSet;
import edu.tamu.tcat.trc.repo.ChangeSet.ApplicableChangeSet;
import edu.tamu.tcat.trc.repo.EditCommandFactory;
import edu.tamu.tcat.trc.repo.IdFactory;
import edu.tamu.tcat.trc.repo.UpdateContext;
import edu.tamu.tcat.trc.services.ServiceContext;
import edu.tamu.tcat.trc.services.categorization.CategorizationService;
import edu.tamu.tcat.trc.services.categorization.EditCategorizationCommand;

public abstract class BaseEditCommand<StorageType extends PersistenceModelV1.CategorizationScheme>
implements EditCategorizationCommand
{
   EditCommandFactory.UpdateStrategy<StorageType> context;

   protected final String categorizationId;
   protected final IdFactory nodeIds;
   protected final ApplicableChangeSet<StorageType> changes = new BasicChangeSet<>();

   protected String scopeId;

   protected ServiceContext<CategorizationService> svcContext;


   public BaseEditCommand(String id, IdFactory nodeIds, EditCommandFactory.UpdateStrategy<StorageType> context)
   {
      // Add root here
      this.categorizationId = id;
      this.nodeIds = nodeIds;
      this.context = context;
   }

   public final String getId()
   {
      return categorizationId;
   }

   public final void setContext(ServiceContext<CategorizationService> ctx)
   {
      this.svcContext = ctx;
      this.scopeId = (String)ctx.getProperty(CategorizationService.CTX_SCOPE_ID);
   }

   @Override
   public final void setKey(String key)
   {
      changes.add("key", dto -> dto.key = key);
   }

   @Override
   public final void setLabel(String label)
   {
      changes.add("label", dto -> dto.title = label);
   }

   @Override
   public final void setDescription(String description)
   {
      changes.add("description", dto -> dto.description = description);
   }

   @Override
   public final CompletableFuture<String> execute()
   {
      return context.update(this::apply)
                    .thenApply(dto -> dto.id);
   }

   private StorageType apply(UpdateContext<StorageType> ctx)
   {
      String ERR_UNDEFINED_SCOPE = "No categorization scope has been configured for this command.";
      String ERR_SCOPE_MISMATCH = "Scope id of categorization to be edited ({0} does not match the scope of the command ({1}).";

      if (this.scopeId == null)
         throw new IllegalStateException(ERR_UNDEFINED_SCOPE);

      StorageType data = prepareData(ctx);
      if (data.scopeId == null)
         data.scopeId = scopeId;

      if (!Objects.equals(scopeId, data.scopeId))
         throw new IllegalStateException(format(ERR_SCOPE_MISMATCH, data.scopeId, scopeId));

      return changes.apply(data);
   }

   protected abstract StorageType prepareData(UpdateContext<StorageType> ctx);

}
