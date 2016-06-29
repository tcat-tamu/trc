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
package edu.tamu.tcat.trc.entries.types.article.dto;

import java.util.ArrayList;
import java.util.List;

import edu.tamu.tcat.trc.entries.types.article.Article;

@Deprecated // this should be an implementation detail
public class ArticleDTO
{
   public String id;
   public String title;
   public String articleType;
   public String contentType;
   public List<ArticleAuthorDTO> authors;
   public PublicationDTO info;
   public String articleAbstract;
   public String body;
   public List<CitationDTO> citation;
   public List<FootnoteDTO> footnotes;
   public List<BibliographyDTO> bibliographies;
   public List<LinkDTO> links;

   public static ArticleDTO create(Article article)
   {
      ArticleDTO dto = new ArticleDTO();

      dto.id = article.getId();
      dto.title = article.getTitle();
      dto.contentType = article.getContentType();
      dto.articleType = article.getArticleType();
      dto.articleAbstract = article.getAbstract();
      dto.body = article.getBody();

      dto.info = PublicationDTO.create(article.getPublicationInfo());

      List<ArticleAuthorDTO> authDTO = new ArrayList<>();
      article.getAuthors().forEach((a) ->{
         authDTO.add(ArticleAuthorDTO.create(a));
      });
      dto.authors = authDTO;

      List<CitationDTO> citations = new ArrayList<>();
      article.getCitations().forEach((c) ->{
         citations.add(CitationDTO.create(c));
      });
      dto.citation = citations;

      List<FootnoteDTO> ftnotes = new ArrayList<>();
      article.getFootnotes().forEach((f) -> {
         ftnotes.add(FootnoteDTO.create(f));
      });
      dto.footnotes = ftnotes;

      List<BibliographyDTO> biblios = new ArrayList<>();
      article.getBibliographies().forEach((b)->{
        biblios.add(BibliographyDTO.create(b));
      });
      dto.bibliographies = biblios;

      List<LinkDTO> links = new ArrayList<>();
      article.getLinks().forEach((l) -> {
         links.add(LinkDTO.create(l));
      });
      dto.links = links;

      return dto;
   }

   public static ArticleDTO copy(ArticleDTO orig)
   {
      ArticleDTO dto = new ArticleDTO();

      dto.id = orig.id;
      dto.title = orig.title;
      dto.articleType = orig.articleType;
      dto.contentType = orig.contentType;
      dto.articleAbstract = orig.articleAbstract;
      dto.body = orig.body;
      dto.info = orig.info;
      dto.authors = new ArrayList<>(orig.authors);
      dto.citation = new ArrayList<>(orig.citation);
      dto.footnotes = new ArrayList<>(orig.footnotes);
      dto.bibliographies = new ArrayList<>(orig.bibliographies);
      dto.links = new ArrayList<>(orig.links);

      return dto;
   }
}
