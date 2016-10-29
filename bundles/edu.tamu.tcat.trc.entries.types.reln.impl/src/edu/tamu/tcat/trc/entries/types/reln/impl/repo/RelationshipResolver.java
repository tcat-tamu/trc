package edu.tamu.tcat.trc.entries.types.reln.impl.repo;

import static java.text.MessageFormat.format;

import java.util.concurrent.CompletableFuture;

import edu.tamu.tcat.account.Account;
import edu.tamu.tcat.osgi.config.ConfigurationProperties;
import edu.tamu.tcat.trc.entries.core.repo.BasicRepoDelegate;
import edu.tamu.tcat.trc.entries.types.reln.Relationship;
import edu.tamu.tcat.trc.entries.types.reln.repo.EditRelationshipCommand;
import edu.tamu.tcat.trc.entries.types.reln.repo.RelationshipRepository;
import edu.tamu.tcat.trc.resolver.EntryId;
import edu.tamu.tcat.trc.resolver.EntryResolverBase;
import edu.tamu.tcat.trc.resolver.InvalidReferenceException;

public class RelationshipResolver extends EntryResolverBase<Relationship>
{
   private BasicRepoDelegate<Relationship, DataModelV1.Relationship, EditRelationshipCommand> delegate;

   public RelationshipResolver(BasicRepoDelegate<Relationship, DataModelV1.Relationship, EditRelationshipCommand> delegate,
                               ConfigurationProperties config)
   {
      super(Relationship.class, config, RelationshipRepository.ENTRY_URI_BASE, RelationshipRepository.ENTRY_TYPE_ID);
      this.delegate = delegate;
   }

   @Override
   public Relationship resolve(Account account, EntryId reference) throws InvalidReferenceException
   {
      if (!accepts(reference))
         throw new InvalidReferenceException(reference, "Unsupported reference type.");

      return delegate.get(account, reference.getId());
   }

   @Override
   protected String getId(Relationship relationship)
   {
      return relationship.getId();
   }

   @Override
   public String getLabel(Relationship instance)
   {
      return format("{0} relationship", instance.getType().getTitle());
   }

   @Override
   public CompletableFuture<Boolean> remove(Account account, EntryId reference)
   {
      return delegate.remove(account, reference.getId());
   }
}