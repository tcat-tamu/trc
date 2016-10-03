package edu.tamu.tcat.trc.auth.account;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public interface EditTrcAccountCommand
{

   /**
    * Executes this command. All changes made using this command will take effect
    * only upon successful execution.
    *
    * @return A future that resolves to the updated account. If the execution fails
    *       for any reason, {@link CompletableFuture#get()} will throw an
    *       {@link ExecutionException} that wraps the cause of the failure.
    */
   CompletableFuture<TrcAccount> execute();
}
