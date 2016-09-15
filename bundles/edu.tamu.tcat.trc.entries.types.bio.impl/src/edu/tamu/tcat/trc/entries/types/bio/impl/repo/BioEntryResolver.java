package edu.tamu.tcat.trc.entries.types.bio.impl.repo;

import java.util.concurrent.CompletableFuture;

import edu.tamu.tcat.account.Account;
import edu.tamu.tcat.osgi.config.ConfigurationProperties;
import edu.tamu.tcat.trc.entries.core.InvalidReferenceException;
import edu.tamu.tcat.trc.entries.core.repo.BasicRepoDelegate;
import edu.tamu.tcat.trc.entries.core.repo.UnauthorziedException;
import edu.tamu.tcat.trc.entries.core.resolver.EntryReference;
import edu.tamu.tcat.trc.entries.core.resolver.EntryResolverBase;
import edu.tamu.tcat.trc.entries.types.bio.Person;
import edu.tamu.tcat.trc.entries.types.bio.repo.EditPersonCommand;
import edu.tamu.tcat.trc.entries.types.bio.repo.PeopleRepository;

public class BioEntryResolver extends EntryResolverBase<Person>
{
   private final BasicRepoDelegate<Person, DataModelV1.Person, EditPersonCommand> delegate;

   public BioEntryResolver(ConfigurationProperties config, BasicRepoDelegate<Person, DataModelV1.Person, EditPersonCommand> delegate)
   {
      super(Person.class, config, PeopleRepository.ENTRY_URI_BASE, PeopleRepository.ENTRY_TYPE_ID);
      this.delegate = delegate;
   }

   @Override
   public Person resolve(Account account, EntryReference reference) throws InvalidReferenceException
   {
      if (!accepts(reference))
         throw new InvalidReferenceException(reference, "Unsupported reference type.");

      return delegate.get(account, reference.id);
   }

   @Override
   protected String getId(Person person)
   {
      return person.getId();
   }

   @Override
   public CompletableFuture<Boolean> remove(Account account, EntryReference reference) throws InvalidReferenceException, UnauthorziedException, UnsupportedOperationException
   {
      return delegate.remove(account, reference.id);
   }
}