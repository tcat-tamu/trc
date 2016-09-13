package edu.tamu.tcat.trc.entries.types.reln.postgres;

import java.util.Iterator;
import java.util.concurrent.CompletableFuture;

import edu.tamu.tcat.account.Account;
import edu.tamu.tcat.trc.entries.core.repo.BasicRepoDelegate;
import edu.tamu.tcat.trc.entries.core.repo.EntryRepository;
import edu.tamu.tcat.trc.entries.types.reln.Relationship;
import edu.tamu.tcat.trc.entries.types.reln.dto.RelationshipDTO;
import edu.tamu.tcat.trc.entries.types.reln.repo.EditRelationshipCommand;
import edu.tamu.tcat.trc.entries.types.reln.repo.RelationshipRepository;

class RelationshipRepositoryImpl implements RelationshipRepository
{

   private final Account account;
   private final BasicRepoDelegate<Relationship, RelationshipDTO, EditRelationshipCommand> delegate;

   public RelationshipRepositoryImpl(BasicRepoDelegate<Relationship, RelationshipDTO, EditRelationshipCommand> delegate,
                                     Account account)
   {
      this.delegate = delegate;
      this.account = account;
   }

   @Override
   public Relationship get(String id)
   {
      return delegate.get(account, id);
   }

   @Override
   public Iterator<Relationship> getAllRelationships()
   {
      return delegate.listAll();
   }

   @Override
   public EditRelationshipCommand create()
   {
      return delegate.create(account);
   }

   @Override
   public EditRelationshipCommand create(String id)
   {
      return delegate.create(account, id);
   }

   @Override
   public EditRelationshipCommand edit(String id)
   {
      return delegate.edit(account, id);
   }

   @Override
   public CompletableFuture<Boolean> remove(String id)
   {
      return delegate.remove(account, id);
   }

   @Override
   public EntryRepository.ObserverRegistration onUpdate(EntryRepository.UpdateObserver<Relationship> observer)
   {
      return delegate.onUpdate(observer, account);
   }
}