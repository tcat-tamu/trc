package edu.tamu.tcat.trc.impl.psql.services.bibref.repo;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import edu.tamu.tcat.account.Account;
import edu.tamu.tcat.trc.entries.core.resolver.EntryReference;
import edu.tamu.tcat.trc.entries.core.resolver.EntryResolverRegistry;
import edu.tamu.tcat.trc.impl.psql.services.bibref.model.ReferenceCollectionImpl;
import edu.tamu.tcat.trc.repo.DocumentRepository;
import edu.tamu.tcat.trc.repo.RepositoryException;
import edu.tamu.tcat.trc.services.bibref.ReferenceCollection;
import edu.tamu.tcat.trc.services.bibref.repo.EditBibliographyCommand;
import edu.tamu.tcat.trc.services.bibref.repo.ReferenceRepository;

public class ReferenceRepositoryImpl implements ReferenceRepository
{

   private final DocumentRepository<ReferenceCollection, DataModelV1.ReferenceCollection, EditBibliographyCommand> docRepo;
   private final EntryResolverRegistry resolverRegistry;
   private final Account account;

   public ReferenceRepositoryImpl(DocumentRepository<ReferenceCollection, DataModelV1.ReferenceCollection, EditBibliographyCommand> docRepo, EntryResolverRegistry resolverRegistry, Account account)
   {
      this.docRepo = docRepo;
      this.resolverRegistry = resolverRegistry;
      this.account = account;
   }

   @Override
   public ReferenceCollection get(EntryReference ref)
   {
      String id = resolverRegistry.tokenize(ref);
      return getDoc(id).orElseGet(ReferenceCollectionImpl::new);
   }

   @Override
   public EditBibliographyCommand edit(EntryReference ref)
   {
      String id = resolverRegistry.tokenize(ref);
      return getDoc(id)
            .map(o -> docRepo.edit(account, id))
            .orElseGet(() -> docRepo.create(account, id));
   }

   @Override
   public CompletableFuture<Boolean> delete(EntryReference ref)
   {
      String id = resolverRegistry.tokenize(ref);
      return docRepo.delete(account, id);
   }

   /**
    * @param id
    * @return the document repo identified by the given id or umpty if the document does not exist
    */
   private Optional<ReferenceCollection> getDoc(String id)
   {
      try
      {
         ReferenceCollection doc = docRepo.get(id);
         return Optional.of(doc);
      }
      catch(RepositoryException e)
      {
         return Optional.empty();
      }
   }

}
