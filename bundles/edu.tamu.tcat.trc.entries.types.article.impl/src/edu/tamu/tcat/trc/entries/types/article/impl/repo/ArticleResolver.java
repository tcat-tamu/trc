package edu.tamu.tcat.trc.entries.types.article.impl.repo;

import java.util.concurrent.CompletableFuture;

import edu.tamu.tcat.account.Account;
import edu.tamu.tcat.osgi.config.ConfigurationProperties;
import edu.tamu.tcat.trc.entries.core.InvalidReferenceException;
import edu.tamu.tcat.trc.entries.core.repo.BasicRepoDelegate;
import edu.tamu.tcat.trc.entries.core.repo.UnauthorziedException;
import edu.tamu.tcat.trc.entries.core.resolver.EntryReference;
import edu.tamu.tcat.trc.entries.core.resolver.EntryResolverBase;
import edu.tamu.tcat.trc.entries.types.article.Article;
import edu.tamu.tcat.trc.entries.types.article.repo.ArticleRepository;
import edu.tamu.tcat.trc.entries.types.article.repo.EditArticleCommand;

public class ArticleResolver extends EntryResolverBase<Article>
{
   private BasicRepoDelegate<Article, DataModelV1.Article, EditArticleCommand> delegate;

   public ArticleResolver(ConfigurationProperties config,
                          BasicRepoDelegate<Article, DataModelV1.Article, EditArticleCommand> delegate)
   {
      super(Article.class, config, ArticleRepository.ENTRY_URI_BASE, ArticleRepository.ENTRY_TYPE_ID);

      this.delegate = delegate;
   }

   @Override
   public Article resolve(Account account, EntryReference reference) throws InvalidReferenceException
   {
      if (!accepts(reference))
         throw new InvalidReferenceException(reference, "Unsupported reference type.");

      return delegate.get(account, reference.id);
   }

   @Override
   protected String getId(Article article)
   {
      return article.getId();
   }

   @Override
   public CompletableFuture<Boolean> remove(Account account, EntryReference reference) throws InvalidReferenceException, UnauthorziedException, UnsupportedOperationException
   {
      return delegate.remove(account, reference.id);
   }
}