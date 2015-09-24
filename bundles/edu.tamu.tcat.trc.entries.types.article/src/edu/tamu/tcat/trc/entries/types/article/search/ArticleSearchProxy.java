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
package edu.tamu.tcat.trc.entries.types.article.search;

import edu.tamu.tcat.trc.entries.types.article.Article;

public class ArticleSearchProxy
{
   public String id;
   public String title;
   public String authorId;
   public String associatedEntity;

   @Deprecated
   public String content;
   public String mimeType;


   public ArticleSearchProxy()
   {
   }

   public ArticleSearchProxy(Article article)
   {
      this.id = article.getId().toString();
      this.title = article.getTitle();
      this.authorId = article.getAuthorId() != null ? article.getAuthorId().toString() : "";
      this.associatedEntity = article.getEntity() != null ? article.getEntity().toString() : "";
      this.content = article.getContent();
      this.mimeType = article.getMimeType();
   }
}
