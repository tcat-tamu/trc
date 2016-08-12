package edu.tamu.tcat.trc.entries.core.repo;

public interface EntryRepository<EntryType>
{
   EntryType get(String id);

   EditEntryCommand<EntryType> create();

   EditEntryCommand<EntryType> edit(String id);

   void remove(String id);
}
