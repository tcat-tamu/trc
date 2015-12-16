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

import java.util.List;
import java.util.UUID;
import java.util.concurrent.Future;

import edu.tamu.tcat.trc.entries.types.article.dto.ArticleAuthorDTO;
import edu.tamu.tcat.trc.entries.types.article.dto.ArticleDTO;
import edu.tamu.tcat.trc.entries.types.article.dto.BibliographyDTO;
import edu.tamu.tcat.trc.entries.types.article.dto.CitationDTO;
import edu.tamu.tcat.trc.entries.types.article.dto.FootnoteDTO;
import edu.tamu.tcat.trc.entries.types.article.dto.LinkDTO;
import edu.tamu.tcat.trc.entries.types.article.dto.PublicationDTO;

/**
 *  Used to update properties of an article.
 */
public interface EditArticleCommand
{
   /**
    * @return The unique id for the article that is being edited. Will not be {@code null}
    */
   UUID getId();


   /**
    * Applies all values from a supplied data vehicle to the article being edited.
    *
    * @param article The changes to apply.
    */
   void setAll(ArticleDTO article);

   /**
    * @param mimeType The mime type of the content supplied for this article.
    */
   void setType(String Type);
   
   /**
    * @param title The title of the article.
    */
   void setTitle(String title);
   
   /**
    * 
    * @param pubData The publication and modification dates of the article
    */
   void setPublicationInfo(PublicationDTO pubData );

   /**
    * 
    * @param authors The author(s) of the article.
    */
   void setAuthors(List<ArticleAuthorDTO> authors);
   
   /**
    * 
    * @param abs The abstract of the article.
    */
   void setAbstract(String abs);
   
   /**
    * 
    * @param body The main text of the article.
    */
   void setBody(String body);
   
   /**
    * 
    * @param ftNotes Footnotes added to the article
    */
   void setFootnotes(List<FootnoteDTO> ftNotes);
   
   void setCitations(List<CitationDTO> citations);
   
   /**
    * 
    * @param bibliographies Articles or books referenced in the article
    */
   void setBibliography(List<BibliographyDTO> bibliographies);
   
   
   void setLinks(List<LinkDTO> links);
   
   void setTheme(ThemeDTO theme);


   /**
    * Executes these updates within the repository.
    *
    * @return The id of the resulting article. If the underlying update fails, the
    *       {@link Future#get()} method will propagate that exception.
    */
   Future<UUID> execute();
}
