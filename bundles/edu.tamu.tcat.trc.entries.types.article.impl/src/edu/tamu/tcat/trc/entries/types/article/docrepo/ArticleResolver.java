package edu.tamu.tcat.trc.entries.types.article.docrepo;

import java.util.concurrent.CompletableFuture;

import edu.tamu.tcat.account.Account;
import edu.tamu.tcat.osgi.config.ConfigurationProperties;
import edu.tamu.tcat.trc.entries.core.InvalidReferenceException;
import edu.tamu.tcat.trc.entries.core.repo.UnauthorziedException;
import edu.tamu.tcat.trc.entries.core.resolver.EntryReference;
import edu.tamu.tcat.trc.entries.core.resolver.EntryResolverBase;
import edu.tamu.tcat.trc.entries.types.article.Article;
import edu.tamu.tcat.trc.entries.types.article.repo.ArticleRepository;

public class ArticleResolver extends EntryResolverBase<Article>
{
   private final ArticleRepoService articleSvc;

   public ArticleResolver(ArticleRepoService articleSvc, ConfigurationProperties config)
   {
      super(Article.class, config, ArticleRepository.ENTRY_URI_BASE, ArticleRepository.ENTRY_TYPE_ID);
      this.articleSvc = articleSvc;
   }

   @Override
   protected String getId(Article instance)
   {
      return instance.getId();
   }

   @Override
   public Article resolve(Account account, EntryReference reference) throws InvalidReferenceException
   {
      if (!accepts(reference))
         throw new InvalidReferenceException(reference, "Unsupported reference type.");

      ArticleRepository repo = articleSvc.getArticleRepo(account);
      return repo.get(reference.id);
   }

   @Override
   public CompletableFuture<Boolean> remove(Account account, EntryReference reference)
         throws InvalidReferenceException, UnauthorziedException
   {
      if (!accepts(reference))
         throw new InvalidReferenceException(reference, "Unsupported reference type.");

      ArticleRepository repo = articleSvc.getArticleRepo(account);
      return repo.remove(reference.id);
   }

}