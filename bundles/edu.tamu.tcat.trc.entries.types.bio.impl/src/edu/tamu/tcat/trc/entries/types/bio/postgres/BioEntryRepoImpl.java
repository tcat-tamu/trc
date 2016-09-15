package edu.tamu.tcat.trc.entries.types.bio.postgres;

import java.util.Iterator;
import java.util.concurrent.CompletableFuture;

import edu.tamu.tcat.account.Account;
import edu.tamu.tcat.trc.entries.core.repo.BasicRepoDelegate;
import edu.tamu.tcat.trc.entries.core.repo.EntryRepository;
import edu.tamu.tcat.trc.entries.types.bio.Person;
import edu.tamu.tcat.trc.entries.types.bio.repo.EditPersonCommand;
import edu.tamu.tcat.trc.entries.types.bio.repo.PeopleRepository;
import edu.tamu.tcat.trc.repo.NoSuchEntryException;
import edu.tamu.tcat.trc.repo.RepositoryException;

public class BioEntryRepoImpl implements PeopleRepository
{
   private final Account account;
   private final BasicRepoDelegate<Person, DataModelV1.Person, EditPersonCommand> delegate;

   public BioEntryRepoImpl(BasicRepoDelegate<Person, DataModelV1.Person, EditPersonCommand> delegate, Account account)
   {
      this.delegate = delegate;
      this.account = account;
   }

   @Override
   public Person get(String id) throws NoSuchEntryException
   {
      return delegate.get(account, id);
   }

   @Override
   public Iterator<Person> listAll() throws RepositoryException
   {
      return delegate.listAll();
   }

   @Override
   public EditPersonCommand create()
   {
      return delegate.create(account);
   }

   @Override
   public EditPersonCommand create(String id)
   {
      return delegate.create(account, id);
   }

   @Override
   public EditPersonCommand edit(String id) throws NoSuchEntryException
   {
      return delegate.edit(account, id);
   }

   @Override
   public CompletableFuture<Boolean> remove(String id) throws NoSuchEntryException
   {
      return delegate.remove(account, id);
   }

   @Override
   public EntryRepository.ObserverRegistration onUpdate(EntryRepository.UpdateObserver<Person> observer)
   {
      return delegate.onUpdate(observer, account);
   }
}