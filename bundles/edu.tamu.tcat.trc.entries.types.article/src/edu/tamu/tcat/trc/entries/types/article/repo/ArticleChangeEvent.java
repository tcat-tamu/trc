/*
 * Copyright 2015 Texas A&M Engineering Experiment Station
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
    * @deprecated See comment on RelationshipUpdateEvent
    */
   @Deprecated
   default Article getArticle() throws CatalogRepoException { return null; }
}
