package edu.tamu.tcat.trc.entries.types.article;

import edu.tamu.tcat.account.Account;
import edu.tamu.tcat.trc.entries.types.article.repo.ArticleRepository;
import edu.tamu.tcat.trc.entries.types.article.search.ArticleSearchService;

/**
 *  Provides access to the repositories and other core backend services associated
 *  with the article TRC Entry
 */
@Deprecated
public interface ArticleRepoFacade
{
   /**
    * @param account The user account requesting access.
    * @return The {@code ArticleRepository} scoped relative to the supplied user account.
    */
   ArticleRepository getArticleRepo(Account account);

   /**
    * @return The {@link ArticleSearchService}
    */
   ArticleSearchService getSearchService();

}
