package edu.tamu.tcat.trc.entries.types.article.rest.v1;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.ws.rs.core.UriBuilder;

import edu.tamu.tcat.trc.entries.types.article.Article;
import edu.tamu.tcat.trc.entries.types.article.ArticleAuthor;
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
      dto.title = article.getTitle();
      dto.content = article.getContent();
      dto.authors = convertAuthors(article.getAuthors());
      dto.articleAbstract = article.getAbstract();
      dto.publication = article.getPublishedDate();
      dto.lastModified = article.getLastModified();
      dto.mimeType = article.getMimeType();
      dto.associatedEntity = article.getEntity();
      UUID authorId = article.getAuthorId();
      dto.authorId = authorId == null ? null : authorId.toString();

      return dto;
   }
   
   private static List<RestApiV1.ArticleAuthor> convertAuthors(List<ArticleAuthor> authors)
   {
      List<RestApiV1.ArticleAuthor> auths = new ArrayList<>();
      
      authors.forEach((a) ->
      {
         RestApiV1.ArticleAuthor authDto = new RestApiV1.ArticleAuthor();
         authDto.id = a.getId();
         authDto.label = a.getLabel();
         auths.add(authDto);
      });
      
      return auths;
   }
}
