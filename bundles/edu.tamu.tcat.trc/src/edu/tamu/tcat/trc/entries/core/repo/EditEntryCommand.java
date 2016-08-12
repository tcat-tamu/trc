package edu.tamu.tcat.trc.entries.core.repo;

import java.util.concurrent.CompletableFuture;

public interface EditEntryCommand<T>
{

   CompletableFuture<String> execute();
}
