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
package edu.tamu.tcat.trc.digires.rest.books.v1;

import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import edu.tamu.tcat.trc.digires.books.discovery.ContentQuery;
import edu.tamu.tcat.trc.digires.books.discovery.CopySearchResult;
import edu.tamu.tcat.trc.digires.books.discovery.CopySearchService;
import edu.tamu.tcat.trc.digires.books.hathitrust.HTFilesSearchService;


@Path("/resources/books/search")
public class CopySearchServiceResource
{
   private CopySearchService searchService;

   // called by DS
   public void setRepository(CopySearchService svc)
   {
      this.searchService = svc;
   }

   // called by DS
   public void activate()
   {
      if (this.searchService == null)
      {
         // HACK: auto generate HathiTrust search service
         this.searchService = new HTFilesSearchService();
      }
   }

   // called by DS
   public void dispose()
   {
   }

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   public SearchResult search(@QueryParam(value = "q") String q,
                              @QueryParam(value = "author") String author,
                              @DefaultValue("-9999") @QueryParam(value = "after") int after,
                              @DefaultValue("-9999") @QueryParam(value = "before") int before,
                              @DefaultValue("0") @QueryParam(value = "offset") int offset,
                              @DefaultValue("25") @QueryParam(value = "limit") int limit
                              )
   {
      Year beforeYr = (before != -9999) ?  Year.of(before) : null;
      Year afterYr = (after != -9999) ?  Year.of(after) : null;
      CopyQueryImpl query = new CopyQueryImpl(q, author, afterYr, beforeYr, offset, limit);

      return executeQuery(query);
   }

   private SearchResult executeQuery(CopyQueryImpl query)
   {
      try
      {
         CopySearchResult result = searchService.find(query);

         CopyQueryDTO qdto = CopyQueryDTO.create(query, DateTimeFormatter.ofPattern("yyyy"));
         return new SearchResult(result, qdto);
      }
      catch (Exception ex)
      {
         throw new IllegalStateException("Failed to execute query [" + query + "]", ex);
      }
   }

   public static class CopyQueryImpl implements ContentQuery
   {
      public String q;
      public String author;
      public Year after;
      public Year before;
      public int offset;
      public int limit;

      public CopyQueryImpl(String keyWords, String author, Year after, Year before, int offset, int limit)
      {
         this.q = keyWords;
         this.author = author;
         this.before = before;
         this.after = after;
         this.offset = offset;
         this.limit = limit;
      }

      @Override
      public String getKeyWordQuery()
      {
         return q;
      }

      @Override
      public String getAuthorQuery()
      {
         return author;
      }

      @Override
      public TemporalAccessor getDateRangeStart()
      {
         return after;
      }

      @Override
      public TemporalAccessor getDateRangeEnd()
      {
         return before;
      }

      @Override
      public int getOffset()
      {
         return offset;
      }

      @Override
      public int getLimit()
      {
         return limit;
      }
   }
}
