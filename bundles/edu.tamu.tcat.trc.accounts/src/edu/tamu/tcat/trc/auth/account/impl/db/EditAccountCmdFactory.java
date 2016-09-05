package edu.tamu.tcat.trc.auth.account.impl.db;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import edu.tamu.tcat.account.login.LoginData;
import edu.tamu.tcat.trc.auth.account.EditTrcAccountCommand;
import edu.tamu.tcat.trc.auth.account.TrcAccount;
import edu.tamu.tcat.trc.repo.BasicChangeSet;
import edu.tamu.tcat.trc.repo.ChangeSet.ApplicableChangeSet;
import edu.tamu.tcat.trc.repo.EditCommandFactory;
import edu.tamu.tcat.trc.repo.UpdateContext;

public class EditAccountCmdFactory implements EditCommandFactory<DataModelV1.AccountData, EditTrcAccountCommand>
{

   @Override
   public EditTrcAccountCommand create(String id, EditCommandFactory.UpdateStrategy<DataModelV1.AccountData> strategy)
   {
      return new EditCommand(id, strategy);
   }

   @Override
   public EditTrcAccountCommand edit(String id, EditCommandFactory.UpdateStrategy<DataModelV1.AccountData> strategy)
   {
      return new EditCommand(id, strategy);
   }

   public static class EditCommand implements EditTrcAccountCommand
   {
      private final String id;
      private final UpdateStrategy<DataModelV1.AccountData> exec;
      private final ApplicableChangeSet<DataModelV1.AccountData> changes = new BasicChangeSet<>();

      private LoginData login;
      private DbAcctDataStore store;

      public EditCommand(String id, UpdateStrategy<DataModelV1.AccountData> exec)
      {
         this.id = id;
         this.exec = exec;
      }

      public void linkLogin(LoginData login, DbAcctDataStore store)
      {
         this.login = login;
         this.store = store;
      }

      @Override
      public CompletableFuture<TrcAccount> execute()
      {
         CompletableFuture<DataModelV1.AccountData> modified = exec.update(ctx -> {
            DataModelV1.AccountData dto = prepModifiedData(ctx);
            return this.changes.apply(dto);
         });

         if (login != null)
         {
            modified = modified.thenApply(dto -> {
               store.doLink(dto.uuid, login);
               return dto;
            });
         }

         return modified.thenApply(dto -> new DbTrcAccount(dto));
      }


      private DataModelV1.AccountData prepModifiedData(UpdateContext<DataModelV1.AccountData> ctx)
      {
         DataModelV1.AccountData dto = null;
         DataModelV1.AccountData original = ctx.getOriginal();
         if (original == null)
         {
            dto = new DataModelV1.AccountData();
            dto.uuid = UUID.fromString(this.id);
            dto.active = true;
         }
         else
         {
            dto = DataModelV1.AccountData.copy(original);
         }

         return dto;
      }
   }
}
