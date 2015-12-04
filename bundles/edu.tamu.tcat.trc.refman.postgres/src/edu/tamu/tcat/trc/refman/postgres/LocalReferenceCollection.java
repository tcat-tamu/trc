package edu.tamu.tcat.trc.refman.postgres;

import java.net.URI;
import java.text.MessageFormat;

import edu.tamu.tcat.account.Account;
import edu.tamu.tcat.trc.refman.BibliographicReference;
import edu.tamu.tcat.trc.refman.EditReferenceCommand;
import edu.tamu.tcat.trc.refman.ReferenceCollection;
import edu.tamu.tcat.trc.refman.postgres.EditCmdFactoryImpl.EditRefCommand;
import edu.tamu.tcat.trc.refman.search.BibItemSearchCommand;
import edu.tamu.tcat.trc.refman.types.ItemType;
import edu.tamu.tcat.trc.refman.types.ItemTypeProvider;
import edu.tamu.tcat.trc.repo.DocumentRepository;
import edu.tamu.tcat.trc.repo.RepositoryException;

/**
 *  A reference collection backed by a local data store.
 *
 */
public class LocalReferenceCollection implements ReferenceCollection
{
   private final URI id;
   private final String name;
   private final Account account;
   private final ItemTypeProvider itemTypeProvider;
   private final DocumentRepository<BibliographicReference, EditReferenceCommand> repo;

   public LocalReferenceCollection(URI id, String name, Account account, ItemTypeProvider provider,
                                   DocumentRepository<BibliographicReference, EditReferenceCommand> repo)
   {
      this.id = id;
      this.name = name;
      this.account = account;
      itemTypeProvider = provider;
      this.repo = repo;
   }

   @Override
   public String getId()
   {
      return id.toString();
   }

   @Override
   public String getName()
   {
      return name;
   }

   @Override
   public ItemTypeProvider getTypeProvider()
   {
      return itemTypeProvider;
   }

   @Override
   public BibItemSearchCommand createSearchCommand()
   {
      throw new UnsupportedOperationException();
   }

   @Override
   public BibliographicReference get(URI id)
   {
      try
      {
         BibliographicReference ref = repo.get(id.toString());
         checkCollection((BasicBibRef)ref);     // TODO add check for type safety and throw illegal state on miss match
         return ref;
      }
      catch (RepositoryException e)
      {
         String message = MessageFormat.format("Failed to retrieve reference. id: {0}", id);
         throw new IllegalStateException(message, e);
      }
   }

   @Override
   public EditReferenceCommand create(ItemType type)
   {
      checkWriteAccess();

      EditReferenceCommand createCmd = repo.create();
      ((EditRefCommand)createCmd).setReferenceContext(this, this.account);
      createCmd.setType(type);

      return createCmd;
   }

   @Override
   public EditReferenceCommand edit(URI id)
   {
      checkWriteAccess();

      try
      {
         EditReferenceCommand createCmd = repo.edit(id.toString());
         ((EditRefCommand)createCmd).setReferenceContext(this, this.account);

         return createCmd;
      }
      catch (RepositoryException e)
      {
         String message = MessageFormat.format("Cannot edit bibliographic reference {0}", id.toString());
         throw new IllegalStateException(message, e);
      }
   }

   @Override
   public void delete(URI id)
   {
      checkWriteAccess();
      BibliographicReference ref = get(id);     // HACK: ensures that the ref belongs to this collection
      if (ref != null)
         return;

      try
      {
         repo.delete(id.toString()).get();
      }
      catch (Exception e)
      {
         String message = MessageFormat.format("Failed to delete bibliographic reference {0}", id.toString());
         throw new IllegalStateException(message, e);
      }
   }

   @Override
   public boolean isWritable()
   {
      return true;
   }

   private void checkCollection(BasicBibRef ref)
   {
      if (!ref.getCollectionId().equals(this.id))
         throw new IllegalStateException("The requested reference {0} is not a part of this collection {0}");
   }

   private void checkWriteAccess()
   {
      if (!isWritable()) {
         String message = "The account {0} does not have permission to edit this collection.";
         message = MessageFormat.format(message, account.getTitle());
         throw new UnsupportedOperationException(message);
      }
   }

   @Override
   public String toString()
   {
      return MessageFormat.format("{0} [id: {1}]", name, id);
   }

   @Override
   public void close() throws Exception
   {
      // TODO Auto-generated method stub

   }
}
