package edu.tamu.tcat.trc.impl.psql.account;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import edu.tamu.tcat.account.login.LoginData;
import edu.tamu.tcat.trc.auth.account.EditTrcAccountCommand;
import edu.tamu.tcat.trc.auth.account.TrcAccount;
import edu.tamu.tcat.trc.impl.psql.account.DataModelV1.AccountData;
import edu.tamu.tcat.trc.repo.BasicChangeSet;
import edu.tamu.tcat.trc.repo.ChangeSet.ApplicableChangeSet;
import edu.tamu.tcat.trc.repo.EditCommandFactory;
import edu.tamu.tcat.trc.repo.ExecutableUpdateContext;

public class EditAccountCmdFactory implements EditCommandFactory<DataModelV1.AccountData, EditTrcAccountCommand>
{

   @Override
   public EditTrcAccountCommand create(ExecutableUpdateContext<DataModelV1.AccountData> ctx)
   {
      return new EditCommand(ctx);
   }

   @Override
   public AccountData initialize(String id, Optional<DataModelV1.AccountData> original)
   {
      return original.map(DataModelV1.AccountData::copy)
              .orElseGet(() -> {
                 DataModelV1.AccountData dto = new DataModelV1.AccountData();
                 dto.uuid = UUID.fromString(id);
                 dto.active = true;
                 return dto;
              });
   }

   public static class EditCommand implements EditTrcAccountCommand
   {
      private final ApplicableChangeSet<DataModelV1.AccountData> changes = new BasicChangeSet<>();
      private final ExecutableUpdateContext<AccountData> ctx;

      private LoginData login;
      private DbAcctDataStore store;

      public EditCommand(ExecutableUpdateContext<DataModelV1.AccountData> ctx)
      {
         this.ctx = ctx;
      }

      public void linkLogin(LoginData login, DbAcctDataStore store)
      {
         this.login = login;
         this.store = store;
      }

      @Override
      public CompletableFuture<TrcAccount> execute()
      {
         CompletableFuture<DataModelV1.AccountData> modified = ctx.update(changes::apply);
         if (login != null)
         {
            modified = modified.thenApply(dto -> {
               store.doLink(dto.uuid, login);
               return dto;
            });
         }

         return modified.thenApply(dto -> new DbTrcAccount(dto));
      }
   }
}
