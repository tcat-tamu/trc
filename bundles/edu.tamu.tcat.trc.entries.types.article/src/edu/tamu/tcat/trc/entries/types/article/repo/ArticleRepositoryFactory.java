package edu.tamu.tcat.trc.entries.types.article.repo;

import edu.tamu.tcat.account.Account;
import edu.tamu.tcat.trc.entries.core.repo.EntryRepositoryRegistry;

/**
 *  Provides access to repository instances for a given account. Note that in general, it is
 *  preferable to use the framework supplied {@link EntryRepositoryRegistry}.
 */
public interface ArticleRepositoryFactory
{
   // NOTE a major purpose for this interface is to ensure that components such as the REST
   //      interface can bind to the main framework stitching logic and ensure that the article
   //      repos and related services have been properly connected to the core TRC Repo framework.

   /**
    * Provides {@link ArticleRepository} instances.
    *
    * @param account The account to bind to the returned repository.
    * @return A repository bound to the associated account.
    */
   ArticleRepository getArticleRepository(Account account);
}
