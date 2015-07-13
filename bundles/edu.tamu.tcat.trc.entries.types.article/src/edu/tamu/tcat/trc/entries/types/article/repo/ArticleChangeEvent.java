package edu.tamu.tcat.trc.entries.types.article.repo;

import edu.tamu.tcat.trc.entries.notification.UpdateEvent;
import edu.tamu.tcat.trc.entries.repo.CatalogRepoException;
import edu.tamu.tcat.trc.entries.types.article.Article;

/**
 * An event notification sent from a {@link ArticleRepository} due to a data change.
 */
public interface ArticleChangeEvent extends UpdateEvent
{
   /**
    * Retrieves the articles that changed.
    *
    * @return the articles that changed.
    * @throws CatalogRepoException If the articles cannot be retrieved (for example,
    *       if the record was deleted).
    */
   /*
    * See the note on RelationshipChangeEvent
    */
   Article getArticle() throws CatalogRepoException;
}
