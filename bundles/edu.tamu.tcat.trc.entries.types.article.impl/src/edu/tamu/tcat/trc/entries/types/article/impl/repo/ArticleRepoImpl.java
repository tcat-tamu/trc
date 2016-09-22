package edu.tamu.tcat.trc.entries.types.article.impl.repo;

import java.net.URI;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import edu.tamu.tcat.account.Account;
import edu.tamu.tcat.trc.entries.core.repo.BasicRepoDelegate;
import edu.tamu.tcat.trc.entries.core.repo.EntryRepository;
import edu.tamu.tcat.trc.entries.types.article.Article;
import edu.tamu.tcat.trc.entries.types.article.repo.ArticleRepository;
import edu.tamu.tcat.trc.entries.types.article.repo.EditArticleCommand;
import edu.tamu.tcat.trc.repo.DocumentNotFoundException;

public class ArticleRepoImpl implements ArticleRepository
{
   private final Account account;
   private final BasicRepoDelegate<Article, DataModelV1.Article, EditArticleCommand> delegate;

   public ArticleRepoImpl(BasicRepoDelegate<Article, DataModelV1.Article, EditArticleCommand> delegate, Account account)
   {
      this.delegate = delegate;
      this.account = account;
   }

   @Override
   public Iterator<Article> listAll()
   {
      return delegate.listAll();
   }

   @Override
   public Article get(String id)
   {
      return delegate.get(account, id);
   }

   @Override
   public List<Article> getArticles(URI entityURI) throws DocumentNotFoundException
   {
   // This seems like a query rather than part of the article repo impl.
      throw new UnsupportedOperationException();
   }

   @Override
   public EditArticleCommand create()
   {
      return delegate.create(account);
   }

   @Override
   public EditArticleCommand create(String id)
   {
      return delegate.create(account, id);
   }

   @Override
   public EditArticleCommand edit(String id)
   {
      return delegate.edit(account, id);

   }

   @Override
   public CompletableFuture<Boolean> remove(String id)
   {
      return delegate.remove(account, id);
   }


   @Override
   public EntryRepository.ObserverRegistration onUpdate(EntryRepository.UpdateObserver<Article> observer)
   {
      return delegate.onUpdate(observer, account);
   }
}