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

import java.util.ArrayList;
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


      setType(updateArticle.type);
      setTitle(updateArticle.title);
      setPublicationInfo(updateArticle.info);
      setAuthors(updateArticle.authors);
      setAbstract(updateArticle.articleAbstract);
      setBody(updateArticle.body);
      setFootnotes(updateArticle.footnotes);
      setCitations(updateArticle.citation);
      setBibliography(updateArticle.bibliographies);
      setLinks(updateArticle.links);

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
   public void setType(String type)
   {
      article.type = guardNull(type);
   }

   @Override
   public void setPublicationInfo(PublicationDTO pubData)
   {
      article.info = pubData;
   }

   @Override
   public void setBody(String body)
   {
      article.body = guardNull(body);
   }

   @Override
   public void setFootnotes(List<FootnoteDTO> footNotes)
   {
      if (footNotes == null)
         article.footnotes = new ArrayList<>();
      else
         article.footnotes = new ArrayList<>(footNotes);
   }

   @Override
   public void setCitations(List<CitationDTO> citations)
   {
      if (citations == null)
         article.citation = new ArrayList<>();
      else
         article.citation = new ArrayList<>(citations);
   }

   @Override
   public void setBibliography(List<BibliographyDTO> bibliographies)
   {
      if (bibliographies == null)
         article.bibliographies = new ArrayList<>();
      else
         article.bibliographies = new ArrayList<>(bibliographies);
   }

   @Override
   public void setLinks(List<LinkDTO> links)
   {
      if (links == null)
         article.links = new ArrayList<>();
      else
         article.links = new ArrayList<>(links);
   }
}
