package edu.tamu.tcat.trc.entries.types.biblio.impl.repo;

import static java.text.MessageFormat.format;

import java.util.Iterator;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import edu.tamu.tcat.account.Account;
import edu.tamu.tcat.trc.entries.core.repo.BasicRepoDelegate;
import edu.tamu.tcat.trc.entries.core.repo.EntryRepository;
import edu.tamu.tcat.trc.entries.core.repo.NoSuchEntryException;
import edu.tamu.tcat.trc.entries.types.biblio.BibliographicEntry;
import edu.tamu.tcat.trc.entries.types.biblio.Edition;
import edu.tamu.tcat.trc.entries.types.biblio.Volume;
import edu.tamu.tcat.trc.entries.types.biblio.repo.BibliographicEntryRepository;
import edu.tamu.tcat.trc.entries.types.biblio.repo.EditBibliographicEntryCommand;

public class BiblioRepoImpl implements BibliographicEntryRepository
{
   private final Account account;
   private final BasicRepoDelegate<BibliographicEntry, DataModelV1.WorkDTO, EditBibliographicEntryCommand> delegate;

   public BiblioRepoImpl(BasicRepoDelegate<BibliographicEntry, DataModelV1.WorkDTO, EditBibliographicEntryCommand> delegate, Account account)
   {
      this.delegate = delegate;
      this.account = account;
   }

   @Override
   public Iterator<BibliographicEntry> listAll()
   {
      return delegate.listAll();
   }

   @Override
   public BibliographicEntry get(String id)
   {
      String notFound = "No record found for bibliographic entry id={1}.";
      return getOptionally(id).orElseThrow(() -> new NoSuchEntryException(format(notFound, id)));
   }

   @Override
   public Optional<BibliographicEntry> getOptionally(String id)
   {
      return delegate.getOptionally(account, id);
   }

   @Override
   public EditBibliographicEntryCommand create()
   {
      return delegate.create(account);
   }

   @Override
   public EditBibliographicEntryCommand create(String id)
   {
      return delegate.create(account, id);
   }

   @Override
   public EditBibliographicEntryCommand edit(String id)
   {
      return delegate.edit(account, id);
   }

   @Override
   public CompletableFuture<Boolean> remove(String id)
   {
      return delegate.remove(account, id);
   }

   @Override
   public Edition getEdition(String workId, String editionId)
   {
      String msg = "Unable to find edition with id [{0}] on work [{1}].";

      BibliographicEntry work = get(workId);
      Edition edition = work.getEdition(editionId);
      if (edition == null)
         throw new IllegalArgumentException(format(msg, editionId, workId));

      return edition;
   }

   @Override
   public Volume getVolume(String workId, String editionId, String volumeId)
   {
      Edition edition = getEdition(workId, editionId);
      Volume volume = edition.getVolume(volumeId);
      if (volume == null)
      {
         throw new IllegalArgumentException("Unable to find volume with id {" + volumeId + "} on edition {" + editionId + "} on work {" + workId + "}.");
      }
      return volume;
   }

   @Override
   public EntryRepository.ObserverRegistration onUpdate(EntryRepository.UpdateObserver<BibliographicEntry> observer)
   {
      return delegate.onUpdate(observer, account);
   }
}