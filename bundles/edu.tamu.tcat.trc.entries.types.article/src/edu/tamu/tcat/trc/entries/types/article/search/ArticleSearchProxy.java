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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import edu.tamu.tcat.trc.entries.types.article.Article;
import edu.tamu.tcat.trc.entries.types.article.ArticleAuthor;
import edu.tamu.tcat.trc.entries.types.article.ArticleAuthor.ContactInfo;
import edu.tamu.tcat.trc.entries.types.article.ArticleLink;
import edu.tamu.tcat.trc.entries.types.article.ArticlePublication;
import edu.tamu.tcat.trc.entries.types.article.Bibliography;
import edu.tamu.tcat.trc.entries.types.article.Bibliography.BiblioAuthor;
import edu.tamu.tcat.trc.entries.types.article.Bibliography.BiblioTranslator;
import edu.tamu.tcat.trc.entries.types.article.Bibliography.IssuedDate;
import edu.tamu.tcat.trc.entries.types.article.Citation;
import edu.tamu.tcat.trc.entries.types.article.Citation.CitationItem;
import edu.tamu.tcat.trc.entries.types.article.Footnote;
import edu.tamu.tcat.trc.entries.types.article.Theme;
import edu.tamu.tcat.trc.entries.types.article.Theme.ArticleRefs;

public class ArticleSearchProxy
{
   public String id;
   public String title;
   public String type;
   public List<AuthorRef> authors;
   public PublicationRef info;
   public String articleAbstract;
   public String body;
   public List<CitationRef> citations;
   public List<FootnoteRef> footnotes;
   public List<BibliographyRef> bibliographies;
   public List<LinkRef> links;
   public ThemeRef theme;


   public ArticleSearchProxy()
   {
   }

   public ArticleSearchProxy(Article article)
   {
      
      this.id = article.getId().toString();
      this.title = article.getTitle();
      this.type = article.getType();
      
      this.authors = new ArrayList<>();
      article.getAuthors().forEach((auth)->
      {
         this.authors.add(new AuthorRef(auth));
      });
      
      this.info = new PublicationRef(article.getPublicationInfo());
      this.articleAbstract = article.getAbstract();
      this.body = article.getBody();
      
      this.citations = new ArrayList<>();
      article.getCitations().forEach((cite)->
      {
         this.citations.add(new CitationRef(cite));
      });
      
      this.footnotes = new ArrayList<>();
      article.getFootnotes().forEach((note)->
      {
         this.footnotes.add(new FootnoteRef(note));
      });
      
      this.bibliographies = new ArrayList<>();
      article.getBibliographies().forEach((bib)->
      {
         this.bibliographies.add(new BibliographyRef(bib));
      });
      
      this.links = new ArrayList<>();
      article.getLinks().forEach((link)->
      {
         this.links.add(new LinkRef(link));
      });
      
      this.theme = new ThemeRef(article.getTheme());
   }

   public static class ArticleReferenceRef
   {
      public String id;
      public String type;
      public String uri;

      public ArticleReferenceRef(){}
      
      public ArticleReferenceRef(ArticleRefs reference)
      {
         this.id = reference.getId();
         this.type = reference.getType();
         this.uri = reference.getURI();
      }
   }
   
   public static class AuthorRef
   {
      public String id;
      public String name;
      public String affiliation;
      public ContactInfoRef contactinfo;
      
      public AuthorRef(){}
      
      public AuthorRef(ArticleAuthor auth)
      {
         this.id = auth.getId();
         this.name = auth.getName();
         this.affiliation = auth.getAffiliation();
         this.contactinfo = new ContactInfoRef(auth.getContactInfo());
      }
   }
   
   public static class ContactInfoRef
   {
      public String email;
      public String phone;
      
      public ContactInfoRef(){}
      
      public ContactInfoRef(ContactInfo info)
      {
         this.email = info.getEmail();
         this.phone = info.getPhone();
      }
   }
   
   public static class PublicationRef
   {
      public Date created;
      public Date modified;
      
      public PublicationRef(){}
      
      public PublicationRef(ArticlePublication pub)
      {
         this.created = pub.getCreated();
         this.modified = pub.getModified();
      }
   }
   
   public static class CitationRef
   {
      public String id;
      public List<CitationItemRef> items;
      
      public CitationRef(){}
      
      public CitationRef(Citation cite)
      {
         this.items = new ArrayList<>();
         this.id = cite.getId();
         cite.getItems().forEach((item)->
         {
            this.items.add(new CitationItemRef(item));
         });
      }
   }
   
   public static class CitationItemRef
   {
      public String id;
      public String label;
      public String locator;
      public String author;
      
      public CitationItemRef(){}
      
      public CitationItemRef(CitationItem item)
      {
         this.id = item.getId();
         this.label = item.getLabel();
         this.locator = item.getLocator();
         this.author = item.getSuppressAuthor();
      }
   }
   
   public static class FootnoteRef
   {
      public String id;
      public String text;

      public FootnoteRef(){}
      
      public FootnoteRef(Footnote note)
      {
         this.id = note.getId();
         this.text = note.getText();
      }
   }

   public static class BibliographyRef
   {
      public String id;
      public String type;
      public String url;
      public String title;
      public String containerTitle;
      public String edition;
      public String publisher;
      public String location;
      public IssuedDateRef issueDate;
      public List<BiblioAuthorRef> auths;
      public List<BiblioTranslatorRef> translators;
      
      public BibliographyRef(){}
      
      public BibliographyRef(Bibliography biblio)
      {
         this.id = biblio.getId();
         this.type = biblio.getType();
         this.url = biblio.getUrl();
         this.title = biblio.getTitle();
         this.containerTitle = biblio.getContainerTitle();
         this.edition = biblio.getEdition();
         this.publisher = biblio.getPublisher();
         this.location = biblio.getPublishLocation();
         this.issueDate = new IssuedDateRef(biblio.getIssuedDate());
         
         this.auths = new ArrayList<>();
         biblio.getAuthors().forEach((auth)->
         {
            this.auths.add(new BiblioAuthorRef(auth));
         });
         
         
         this.translators = new ArrayList<>();
         biblio.getTranslators().forEach((trans)->
         {
            this.translators.add(new BiblioTranslatorRef(trans));
         });
         
      }
   }
   
   public static class IssuedDateRef
   {
      public List<List<String>> date;

      public IssuedDateRef(){}
      
      public IssuedDateRef(IssuedDate date)
      {
         this.date = new ArrayList<List<String>>(date.getDateParts());
      }
   }
   
   public static class BiblioAuthorRef
   {
      public String family;
      public String given;

      public BiblioAuthorRef(){}
      
      public BiblioAuthorRef(BiblioAuthor author)
      {
         this.family = author.getFamily();
         this.given = author.getGiven();
      }
   }
   
   public static class BiblioTranslatorRef
   {
      public String family;
      public String given;
      public String lit;
      
      public BiblioTranslatorRef(){}
      
      public BiblioTranslatorRef(BiblioTranslator trans)
      {
         this.family = trans.getFamily();
         this.given = trans.getGiven();
         this.lit = trans.getLiteral();
      }
   }
   
   public static class LinkRef
   {
      public String id;
      public String relation;
      public String title;
      public String type;
      public String uri;

      public LinkRef(){}
      
      public LinkRef(ArticleLink link)
      {
         this.id = link.getId();
         this.relation = link.getRel();
         this.title = link.getTitle();
         this.type = link.getType();
         this.uri = link.getUri();
      }
   }
   
   public static class ThemeRef
   {
      public String title;
      public String abs;
      public List<ArticleReferenceRef> refs;
      
      public ThemeRef(){}
      
      public ThemeRef(Theme theme)
      {
         
         this.title = theme.getTitle();
         this.abs = theme.getAbstract();
         this.refs = new ArrayList<>();
         theme.getArticleRefs().forEach((ref)->
         {
            this.refs.add(new ArticleReferenceRef(ref));
         });
      }
   }
}
