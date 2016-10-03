package edu.tamu.tcat.trc.entries.types.bio.impl.repo;

import java.util.concurrent.CompletableFuture;

import edu.tamu.tcat.account.Account;
import edu.tamu.tcat.osgi.config.ConfigurationProperties;
import edu.tamu.tcat.trc.entries.core.repo.BasicRepoDelegate;
import edu.tamu.tcat.trc.entries.core.resolver.EntryReference;
import edu.tamu.tcat.trc.entries.core.resolver.EntryResolverBase;
import edu.tamu.tcat.trc.entries.core.resolver.InvalidReferenceException;
import edu.tamu.tcat.trc.entries.types.bio.BiographicalEntry;
import edu.tamu.tcat.trc.entries.types.bio.repo.BiographicalEntryRepository;
import edu.tamu.tcat.trc.entries.types.bio.repo.EditBiographicalEntryCommand;

public class BioEntryResolver extends EntryResolverBase<BiographicalEntry>
{
   private final BasicRepoDelegate<BiographicalEntry, DataModelV1.Person, EditBiographicalEntryCommand> delegate;

   public BioEntryResolver(ConfigurationProperties config, BasicRepoDelegate<BiographicalEntry, DataModelV1.Person, EditBiographicalEntryCommand> delegate)
   {
      super(BiographicalEntry.class, config, BiographicalEntryRepository.ENTRY_URI_BASE, BiographicalEntryRepository.ENTRY_TYPE_ID);
      this.delegate = delegate;
   }

   @Override
   public BiographicalEntry resolve(Account account, EntryReference reference) throws InvalidReferenceException
   {
      if (!accepts(reference))
         throw new InvalidReferenceException(reference, "Unsupported reference type.");

      return delegate.get(account, reference.id);
   }

   @Override
   protected String getId(BiographicalEntry person)
   {
      return person.getId();
   }

   @Override
   public CompletableFuture<Boolean> remove(Account account, EntryReference reference)
   {
      return delegate.remove(account, reference.id);
   }
}