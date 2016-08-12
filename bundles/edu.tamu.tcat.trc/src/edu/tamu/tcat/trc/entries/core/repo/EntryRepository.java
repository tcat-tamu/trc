package edu.tamu.tcat.trc.entries.core.repo;

import java.util.concurrent.CompletableFuture;

public interface EntryRepository<EntryType>
{
   EntryType get(String id);

   EditEntryCommand<EntryType> create();

   EditEntryCommand<EntryType> create(String id);

   EditEntryCommand<EntryType> edit(String id);

   CompletableFuture<Boolean> remove(String id);

   //
   // void onUpdate(Consumer<String> observer);
}
