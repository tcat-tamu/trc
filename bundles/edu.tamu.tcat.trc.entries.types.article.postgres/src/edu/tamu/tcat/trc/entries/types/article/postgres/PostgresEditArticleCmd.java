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
package edu.tamu.tcat.trc.entries.types.article.postgres;

import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import edu.tamu.tcat.trc.entries.types.article.dto.ArticleAuthorDTO;
import edu.tamu.tcat.trc.entries.types.article.dto.ArticleDTO;
import edu.tamu.tcat.trc.entries.types.article.dto.BibliographyDTO;
import edu.tamu.tcat.trc.entries.types.article.dto.CitationDTO;
import edu.tamu.tcat.trc.entries.types.article.dto.FootnoteDTO;
import edu.tamu.tcat.trc.entries.types.article.dto.LinkDTO;
import edu.tamu.tcat.trc.entries.types.article.dto.PublicationDTO;
import edu.tamu.tcat.trc.entries.types.article.repo.EditArticleCommand;
import edu.tamu.tcat.trc.entries.types.article.repo.ThemeDTO;

public class PostgresEditArticleCmd implements EditArticleCommand
{

   private final ArticleDTO article;
   private final AtomicBoolean executed = new AtomicBoolean(false);

   private Function<ArticleDTO, Future<UUID>> commitHook;

   public PostgresEditArticleCmd(ArticleDTO note)
   {
      this.article = note;
   }

   public void setCommitHook(Function<ArticleDTO, Future<UUID>> hook)
   {
      commitHook = hook;
   }

   @Override
   public UUID getId()
   {
      return article.id;
   }

   @Override
   public void setAll(ArticleDTO updateArticle)
   {
      if (updateArticle.id != null && !updateArticle.id.equals(article.id))
         throw new IllegalArgumentException("The supplied article ");

      
      setTitle(updateArticle.title);
      setAuthors(updateArticle.authors);
      setAbstract(updateArticle.articleAbstract);

   }

   @Override
   public void setTitle(String title)
   {
      article.title = guardNull(title);
   }

   @Override
   public Future<UUID> execute()
   {
      Objects.requireNonNull(commitHook, "No commit hook supplied.");
      if (!executed.compareAndSet(false, true))
         throw new IllegalStateException("This edit copy command has already been invoked.");

      return commitHook.apply(article);
   }
   
   private String guardNull(String value)
   {
      return value != null ? value : "";
   }

   @Override
   public void setAuthors(List<ArticleAuthorDTO> authors)
   {
      if (authors == null)
         article.authors = new ArrayList<>();
      else
         article.authors = new ArrayList<>(authors);
   }

   @Override
   public void setAbstract(String abs)
   {
      article.articleAbstract = guardNull(abs);
   }

   @Override
   public void setType(String Type)
   {
      // TODO Auto-generated method stub
      
   }

   @Override
   public void setPublicationInfo(PublicationDTO pubData)
   {
      // TODO Auto-generated method stub
      
   }

   @Override
   public void setBody(String body)
   {
      // TODO Auto-generated method stub
      
   }

   @Override
   public void setFootnotes(List<FootnoteDTO> ftNotes)
   {
      // TODO Auto-generated method stub
      
   }

   @Override
   public void setCitations(List<CitationDTO> citations)
   {
      // TODO Auto-generated method stub
      
   }

   @Override
   public void setBibliography(List<BibliographyDTO> bibliographies)
   {
      // TODO Auto-generated method stub
      
   }

   @Override
   public void setLinks(List<LinkDTO> links)
   {
      // TODO Auto-generated method stub
      
   }

   @Override
   public void setTheme(ThemeDTO theme)
   {
      // TODO Auto-generated method stub
      
   }


}
