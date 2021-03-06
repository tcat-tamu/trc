package edu.tamu.tcat.trc.entries.types.article.rest.v1;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.ws.rs.core.UriBuilder;

import edu.tamu.tcat.trc.TrcApplication;
import edu.tamu.tcat.trc.entries.types.article.Article;
import edu.tamu.tcat.trc.entries.types.article.ArticleAuthor;
import edu.tamu.tcat.trc.entries.types.article.Footnote;
import edu.tamu.tcat.trc.entries.types.article.search.ArticleQuery;
import edu.tamu.tcat.trc.entries.types.article.search.ArticleSearchProxy;
import edu.tamu.tcat.trc.entries.types.article.search.ArticleSearchResult;
import edu.tamu.tcat.trc.resolver.EntryId;
import edu.tamu.tcat.trc.resolver.EntryIdDto;
import edu.tamu.tcat.trc.resolver.EntryResolverRegistry;

/**
 * @since 1.1
 */
public class RestApiV1Adapter
{
   private EntryResolverRegistry resolvers;

   public RestApiV1Adapter(TrcApplication trcCtx)
   {
      resolvers = trcCtx.getResolverRegistry();
   }

   public List<RestApiV1.ArticleSearchResult> toDTO(ArticleSearchResult results)
   {
      List<ArticleSearchProxy> proxies = results.getResults();
      Map<String, Map<String, List<String>>> hits = results.getHits();
      if (proxies == null)
         return new ArrayList<>();

      List<RestApiV1.ArticleSearchResult> compiledResults = proxies.stream()
                    .map(this::toArticleDTO)
                    .collect(Collectors.toList());

      compiledResults.forEach((article)->
      {
         if (hits.containsKey(article.id))
         {
            Map<String, List<String>> map = hits.get(article.id);
            article.absHL = map.get("article_abstract");
            article.contentHL = map.get("article_content");
         }
      });

      return compiledResults;
   }

   public RestApiV1.Article adapt(Article article)
   {
      RestApiV1.Article dto = new RestApiV1.Article();
      dto.id = article.getId().toString();
      EntryId entryId = resolvers.getResolver(article).makeReference(article);
      dto.ref = EntryIdDto.adapt(entryId, resolvers);
      dto.articleType = article.getArticleType();

      dto.title = article.getTitle();
      dto.authors = convertAuthors(article.getAuthors());
      dto.articleAbstract = article.getAbstract();
      dto.body = article.getBody();

      dto.footnotes = convertFootnotes(article.getFootnotes());

      return dto;
   }

   public RestApiV1.Link makeLink(URI uri, String rel, String title)
   {
      RestApiV1.Link link = new RestApiV1.Link();
      link.uri = uri;
      link.rel = rel;
      link.title = title;

      return link;
   }

   public RestApiV1.QueryDetail toQueryDetail(URI baseUri, ArticleSearchResult result)
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

   private RestApiV1.ArticleSearchResult toArticleDTO(ArticleSearchProxy article)
   {
      RestApiV1.ArticleSearchResult dto = new RestApiV1.ArticleSearchResult();
      dto.id = article.id;
      dto.ref = article.token == null ? null : EntryIdDto.adapt(resolvers.getReference(article.token));
      dto.title = article.title;

      dto.authors = article.authors == null ? new ArrayList<>() : new ArrayList<>(article.authors);

      return dto;
   }

   private Map<String, RestApiV1.Footnote> convertFootnotes(Collection<Footnote> footnotes)
   {

      Map<String, RestApiV1.Footnote> footnoteDTOs = new HashMap<>();
      if (footnotes != null)
      {
         footnotes.forEach((ftn) ->
         {
            RestApiV1.Footnote dto = new RestApiV1.Footnote();
            dto.id = ftn.getId();
            dto.backlinkId = ftn.getBacklinkId();
            dto.content = ftn.getContent();
            dto.mimeType = ftn.getMimeType();

            footnoteDTOs.put(dto.id, dto);
         });
      }
      return footnoteDTOs;
   }

   private List<RestApiV1.ArticleAuthor> convertAuthors(List<ArticleAuthor> authors)
   {
      return (authors == null)
            ? Collections.emptyList()
            : authors.stream().map(this::adapt).collect(Collectors.toList());
   }

   private RestApiV1.ArticleAuthor adapt(ArticleAuthor author)
   {
      RestApiV1.ArticleAuthor dto = new RestApiV1.ArticleAuthor();
      dto.id = author.getId();
      dto.name = author.getDisplayName();
      dto.lastname = author.getLastname();
      dto.firstname = author.getFirstname();

      dto.properties = author.getProperties().stream()
            .collect(Collectors.toMap(
                  Function.identity(),
                  key -> author.getProperty(key).orElse("")
            ));

      return dto;
   }
}
