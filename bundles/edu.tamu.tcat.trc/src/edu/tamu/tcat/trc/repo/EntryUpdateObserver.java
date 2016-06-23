package edu.tamu.tcat.trc.repo;

public interface EntryUpdateObserver<StorageType>
{
   String notify(UpdateContext<StorageType> context);
}