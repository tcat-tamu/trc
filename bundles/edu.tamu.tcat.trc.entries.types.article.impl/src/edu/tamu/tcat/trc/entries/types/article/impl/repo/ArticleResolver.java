package edu.tamu.tcat.trc.entries.types.article.impl.repo;

import static java.text.MessageFormat.format;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import edu.tamu.tcat.account.Account;
import edu.tamu.tcat.osgi.config.ConfigurationProperties;
import edu.tamu.tcat.trc.entries.core.repo.BasicRepoDelegate;
import edu.tamu.tcat.trc.entries.types.article.Article;
import edu.tamu.tcat.trc.entries.types.article.repo.ArticleRepository;
import edu.tamu.tcat.trc.entries.types.article.repo.EditArticleCommand;
import edu.tamu.tcat.trc.resolver.EntryId;
import edu.tamu.tcat.trc.resolver.EntryResolverBase;
import edu.tamu.tcat.trc.resolver.InvalidReferenceException;

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
   public Optional<Article> resolve(Account account, EntryId reference) throws InvalidReferenceException
   {
      if (!accepts(reference))
         throw new InvalidReferenceException(reference, "Unsupported reference type.");

      return delegate.getOptionally(account, reference.getId());
   }

   @Override
   protected String getId(Article article)
   {
      return article.getId();
   }

   @Override
   public String getLabel(Article instance)
   {
      return instance.getTitle();
   }

   @Override
   public String getHtmlLabel(Article instance)
   {
      return format("<span class='article-title'>{0}</span>", instance.getTitle());
   }

   @Override
   public CompletableFuture<Boolean> remove(Account account, EntryId reference) throws InvalidReferenceException
   {
      return delegate.remove(account, reference.getId());
   }
}