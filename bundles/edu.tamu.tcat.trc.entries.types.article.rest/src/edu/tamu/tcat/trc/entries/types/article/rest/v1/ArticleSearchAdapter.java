package edu.tamu.tcat.trc.entries.types.article.rest.v1;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.ws.rs.core.UriBuilder;

import edu.tamu.tcat.trc.entries.types.article.Article;
import edu.tamu.tcat.trc.entries.types.article.ArticleAuthor;
import edu.tamu.tcat.trc.entries.types.article.ArticleAuthor.ContactInfo;
import edu.tamu.tcat.trc.entries.types.article.ArticleLink;
import edu.tamu.tcat.trc.entries.types.article.ArticlePublication;
import edu.tamu.tcat.trc.entries.types.article.Bibliography;
import edu.tamu.tcat.trc.entries.types.article.Bibliography.IssuedDate;
import edu.tamu.tcat.trc.entries.types.article.Citation;
import edu.tamu.tcat.trc.entries.types.article.Footnote;
import edu.tamu.tcat.trc.entries.types.article.Theme;
import edu.tamu.tcat.trc.entries.types.article.rest.v1.RestApiV1.Link;
import edu.tamu.tcat.trc.entries.types.article.search.ArticleQuery;
import edu.tamu.tcat.trc.entries.types.article.search.ArticleSearchProxy;
import edu.tamu.tcat.trc.entries.types.article.search.ArticleSearchResult;

/**
 * @since 1.1
 */
public class ArticleSearchAdapter
{
   public static List<RestApiV1.ArticleSearchResult> toDTO(ArticleSearchResult results)
   {
      List<ArticleSearchProxy> proxies = results.getResults();
      if (proxies == null)
         return new ArrayList<>();

      return proxies.stream()
                    .map(ArticleSearchAdapter::toArticleDTO)
                    .collect(Collectors.toList());
   }

   private static RestApiV1.ArticleSearchResult toArticleDTO(ArticleSearchProxy article)
   {
      RestApiV1.ArticleSearchResult dto = new RestApiV1.ArticleSearchResult();
      dto.id = article.id;
      dto.authorId = article.authorId;
      dto.associatedEntity = article.associatedEntity;
      dto.mimeType = article.mimeType;
      dto.title = article.title;
      dto.content = article.content;

      return dto;
   }

   public static  RestApiV1.QueryDetail toQueryDetail(URI baseUri, ArticleSearchResult result)
   {
      ArticleQuery query = result.getQuery();

      RestApiV1.QueryDetail detail = new RestApiV1.QueryDetail();
      detail.q = query.q == null ? "" : query.q.trim();
      detail.highlight = query.highlighting;
      detail.offset = query.offset;
      detail.max = query.max;
      detail.totalResults = (int)result.getNumberMatched();
      detail.pg = (detail.offset / query.max) + 1;
      detail.numPages = (detail.totalResults / query.max) + 1;

      UriBuilder pathBuilder = UriBuilder.fromUri(baseUri);
      pathBuilder.queryParam("q", "{q}");
      pathBuilder.queryParam("hi", "{highlight}");
      pathBuilder.queryParam("offset", "{offset}");
      pathBuilder.queryParam("max", "{max}");

      Map<String, String> params = new HashMap<>();
      params.put("q", detail.q);
      params.put("highlight", Boolean.toString(detail.highlight));
      params.put("max", Integer.toString(detail.max));

      params.put("offset", Integer.toString(detail.offset));
      detail.self = makeLink(pathBuilder.buildFromEncodedMap(params), "self", "Self");

      if (detail.pg < detail.numPages)
      {
         params.put("offset", Integer.toString(detail.pg * detail.max));
         detail.next = makeLink(pathBuilder.buildFromEncodedMap(params), "next", "Next results");
      }

      if (detail.pg > 1)
      {
         params.put("offset", Integer.toString((detail.pg - 2) * detail.max));
         detail.previous = makeLink(pathBuilder.buildFromEncodedMap(params), "prev", "Previous results");
      }

      params.put("offset", Integer.toString(0));
      detail.first = makeLink(pathBuilder.buildFromEncodedMap(params), "first", "First results");

      params.put("offset", Integer.toString((detail.numPages - 1) * detail.max));
      detail.last = makeLink(pathBuilder.buildFromEncodedMap(params), "last", "Last results");

      return detail;
   }

   public static Link makeLink(URI uri, String rel, String title)
   {
      RestApiV1.Link link = new RestApiV1.Link();
      link.uri = uri;
      link.rel = rel;
      link.title = title;

      return link;
   }

   public static RestApiV1.Article toDTO(Article article)
   {
      RestApiV1.Article dto = new RestApiV1.Article();
      dto.id = article.getId().toString();
      dto.type = article.getType();
      dto.title = article.getTitle();
      dto.pubInfo = convertPubInfo(article.getPublicationInfo());
      dto.authors = convertAuthors(article.getAuthors());
      dto.articleAbstract = article.getAbstract();
      dto.body = article.getBody();
      dto.footnotes = convertFootnotes(article.getFootnotes());
      dto.citations = convertCitations(article.getCitations());
      dto.bibliography = convertBiblios(article.getBibliographies());
      dto.links = convertLinks(article.getLinks());
      dto.theme = convertTheme(article.getTheme());

      return dto;
   }
   
   private static RestApiV1.Publication convertPubInfo(ArticlePublication pubInfo)
   {
      RestApiV1.Publication pub = new RestApiV1.Publication();
      pub.dateCreated = pubInfo.getCreated();
      pub.dateModified = pubInfo.getModified();
      return pub;
   }

   private static RestApiV1.Theme convertTheme(Theme theme)
   {
      RestApiV1.Theme t = new RestApiV1.Theme();
      List<RestApiV1.Articles> articleList = new ArrayList<>();
      t.title = theme.getTitle();
      t.themeAbstract = theme.getAbstract();
      theme.getArticleRefs().forEach((treat)->
      {
         RestApiV1.Articles article = new RestApiV1.Articles();
         article.id = treat.getId();
         article.type = treat.getType();
         article.uri = treat.getURI();
         articleList.add(article);
      });
      t.articles = new ArrayList<>(articleList);
      return t;
   }

   private static List<RestApiV1.ArticleLink> convertLinks(List<ArticleLink> links)
   {
      List<RestApiV1.ArticleLink> articleLinks = new ArrayList<>();
      links.forEach((l)->
      {
         RestApiV1.ArticleLink link = new RestApiV1.ArticleLink();
         link.id = l.getId();
         link.title = l.getTitle();
         link.type = l.getType();
         link.uri = l.getUri();
         link.rel = l.getRel();
         articleLinks.add(link);
      });
      return articleLinks;
   }

   private static List<RestApiV1.Bibliography> convertBiblios(List<Bibliography> bibliographies)
   {
      List<RestApiV1.Bibliography> biblios = new ArrayList<>();
      bibliographies.forEach((bib)->
      {
         RestApiV1.Bibliography biblio = new RestApiV1.Bibliography();
         biblio.id = bib.getId();
         biblio.title = bib.getTitle();
         biblio.edition = bib.getEdition();
         biblio.publisher = bib.getPublisher();
         biblio.publisherPlace = bib.getPublishLocation();
         biblio.containerTitle = bib.getContainerTitle();
         biblio.type = bib.getType();
         biblio.URL = bib.getUrl();
         
         IssuedDate issuedDate = bib.getIssuedDate();
         RestApiV1.Issued issued = new RestApiV1.Issued();
         issued.dateParts = new ArrayList<>(issuedDate.getDateParts());
         biblio.issued = issued;
         
         List<RestApiV1.Author> authors = new ArrayList<>();
         bib.getAuthors().forEach((a)->
         {
            RestApiV1.Author author = new RestApiV1.Author();
            author.family = a.getFamily();
            author.given = a.getGiven();
            authors.add(author);
         });
         biblio.author = new ArrayList<>(authors);
         
         List<RestApiV1.Translator> translators = new ArrayList<>();
         bib.getTranslators().forEach((t)->
         {
            RestApiV1.Translator trans = new RestApiV1.Translator();
            trans.family = t.getFamily();
            trans.given = t.getGiven();
            trans.literal = t.getLiteral();
            translators.add(trans);
         });
         biblio.translator = new ArrayList<>(translators);
         biblios.add(biblio);
      });
      return biblios;
   }

   private static List<RestApiV1.Citation> convertCitations(List<Citation> citations)
   {
      List<RestApiV1.Citation> citeDTOs = new ArrayList<>();
      if (citations != null)
      {
         citations.forEach((cite) ->
         {
            RestApiV1.Citation dto = new RestApiV1.Citation();
            dto.citationID = cite.getId();
            dto.properties = new RestApiV1.ArticleProperties();
            dto.citationItems = new ArrayList<>();
            cite.getItems().forEach((i)->
            {
               RestApiV1.CitationItem cItem = new RestApiV1.CitationItem();
               cItem.id = i.getId();
               cItem.label = i.getLabel();
               cItem.locator = i.getLocator();
               cItem.suppressAuthor = i.getSuppressAuthor();
               dto.citationItems.add(cItem);
               
            });
         
            citeDTOs.add(dto);
         });
      }
      return citeDTOs;
   }

   private static List<RestApiV1.FootNote> convertFootnotes(List<Footnote> footnotes)
   {

      List<RestApiV1.FootNote> footnoteDTOs = new ArrayList<>();
      if (footnotes != null)
      {
         footnotes.forEach((ftn) ->
         {
            RestApiV1.FootNote dto = new RestApiV1.FootNote();
            dto.id = ftn.getId();
            dto.text = ftn.getText();
            
            footnoteDTOs.add(dto);
         });
      }
      return footnoteDTOs;
   }

   private static List<RestApiV1.ArticleAuthor> convertAuthors(List<ArticleAuthor> authors)
   {
      List<RestApiV1.ArticleAuthor> auths = new ArrayList<>();
      
      authors.forEach((a) ->
      {
         RestApiV1.ArticleAuthor authDto = new RestApiV1.ArticleAuthor();
         authDto.id = a.getId();
         authDto.name = a.getName();
         authDto.affiliation = a.getAffiliation();
         ContactInfo contactInfo = a.getContactInfo();
         RestApiV1.Contact contact = new RestApiV1.Contact();
         contact.email = contactInfo.getEmail();
         contact.phone = contactInfo.getPhone();
         authDto.contact = contact;
         auths.add(authDto);
      });
      
      return auths;
   }
}
