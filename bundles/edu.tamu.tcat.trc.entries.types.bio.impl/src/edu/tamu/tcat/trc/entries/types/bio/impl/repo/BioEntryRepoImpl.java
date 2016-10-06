package edu.tamu.tcat.trc.entries.types.bio.impl.repo;

import java.util.Iterator;
import java.util.concurrent.CompletableFuture;

import edu.tamu.tcat.account.Account;
import edu.tamu.tcat.trc.entries.core.repo.BasicRepoDelegate;
import edu.tamu.tcat.trc.entries.core.repo.EntryRepository;
import edu.tamu.tcat.trc.entries.types.bio.BiographicalEntry;
import edu.tamu.tcat.trc.entries.types.bio.repo.BiographicalEntryRepository;
import edu.tamu.tcat.trc.entries.types.bio.repo.EditBiographicalEntryCommand;
import edu.tamu.tcat.trc.repo.RepositoryException;

public class BioEntryRepoImpl implements BiographicalEntryRepository
{
   private final Account account;
   private final BasicRepoDelegate<BiographicalEntry, DataModelV1.Person, EditBiographicalEntryCommand> delegate;

   public BioEntryRepoImpl(BasicRepoDelegate<BiographicalEntry, DataModelV1.Person, EditBiographicalEntryCommand> delegate, Account account)
   {
      this.delegate = delegate;
      this.account = account;
   }

   @Override
   public BiographicalEntry get(String id)
   {
      return delegate.get(account, id);
   }

   @Override
   public Iterator<BiographicalEntry> listAll() throws RepositoryException
   {
      return delegate.listAll();
   }

   @Override
   public EditBiographicalEntryCommand create()
   {
      return delegate.create(account);
   }

   @Override
   public EditBiographicalEntryCommand create(String id)
   {
      return delegate.create(account, id);
   }

   @Override
   public EditBiographicalEntryCommand edit(String id)
   {
      return delegate.edit(account, id);
   }

   @Override
   public CompletableFuture<Boolean> remove(String id)
   {
      return delegate.remove(account, id);
   }

   @Override
   public EntryRepository.ObserverRegistration onUpdate(EntryRepository.UpdateObserver<BiographicalEntry> observer)
   {
      return delegate.onUpdate(observer, account);
   }
}