package edu.tamu.tcat.trc.entries.types.reln.impl.repo;

import java.util.concurrent.CompletableFuture;

import edu.tamu.tcat.account.Account;
import edu.tamu.tcat.osgi.config.ConfigurationProperties;
import edu.tamu.tcat.trc.entries.core.repo.BasicRepoDelegate;
import edu.tamu.tcat.trc.entries.core.resolver.EntryReference;
import edu.tamu.tcat.trc.entries.core.resolver.EntryResolverBase;
import edu.tamu.tcat.trc.entries.core.resolver.InvalidReferenceException;
import edu.tamu.tcat.trc.entries.types.reln.Relationship;
import edu.tamu.tcat.trc.entries.types.reln.dto.RelationshipDTO;
import edu.tamu.tcat.trc.entries.types.reln.repo.EditRelationshipCommand;
import edu.tamu.tcat.trc.entries.types.reln.repo.RelationshipRepository;

public class RelationshipResolver extends EntryResolverBase<Relationship>
{
   private BasicRepoDelegate<Relationship, RelationshipDTO, EditRelationshipCommand> delegate;

   public RelationshipResolver(BasicRepoDelegate<Relationship, RelationshipDTO, EditRelationshipCommand> delegate,
                               ConfigurationProperties config)
   {
      super(Relationship.class, config, RelationshipRepository.ENTRY_URI_BASE, RelationshipRepository.ENTRY_TYPE_ID);
      this.delegate = delegate;
   }

   @Override
   public Relationship resolve(Account account, EntryReference reference) throws InvalidReferenceException
   {
      if (!accepts(reference))
         throw new InvalidReferenceException(reference, "Unsupported reference type.");

      return delegate.get(account, reference.id);
   }

   @Override
   protected String getId(Relationship relationship)
   {
      return relationship.getId();
   }

   @Override
   public CompletableFuture<Boolean> remove(Account account, EntryReference reference)
   {
      return delegate.remove(account, reference.id);
   }
}