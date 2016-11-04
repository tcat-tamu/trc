package edu.tamu.tcat.trc.entries.types.biblio.rest.v1;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;

import edu.tamu.tcat.trc.entries.types.biblio.BibliographicEntry;
import edu.tamu.tcat.trc.entries.types.biblio.impl.search.WorkSolrQueryCommand;
import edu.tamu.tcat.trc.entries.types.biblio.repo.BibliographicEntryRepository;
import edu.tamu.tcat.trc.entries.types.biblio.repo.EditBibliographicEntryCommand;
import edu.tamu.tcat.trc.entries.types.biblio.rest.EntityPersistenceAdapter;
import edu.tamu.tcat.trc.entries.types.biblio.search.BiblioSearchProxy;
import edu.tamu.tcat.trc.entries.types.biblio.search.SearchWorksResult;
import edu.tamu.tcat.trc.entries.types.biblio.search.WorkQueryCommand;
import edu.tamu.tcat.trc.resolver.EntryIdDto;
import edu.tamu.tcat.trc.resolver.EntryResolverRegistry;
import edu.tamu.tcat.trc.search.solr.QueryService;
import edu.tamu.tcat.trc.search.solr.SearchException;
import edu.tamu.tcat.trc.services.TrcServiceManager;

public class WorkCollectionResource
{
   private static final Logger logger = Logger.getLogger(WorkCollectionResource.class.getName());

   private final BibliographicEntryRepository repo;
   private final QueryService<WorkSolrQueryCommand> queryService;
   private final TrcServiceManager serviceMgr;
   private final EntryResolverRegistry resolvers;

   public WorkCollectionResource(BibliographicEntryRepository repo, QueryService<WorkSolrQueryCommand> queryService, TrcServiceManager serviceMgr, EntryResolverRegistry resolverRegistry)
   {
      this.repo = repo;
      this.queryService = queryService;
      this.serviceMgr = serviceMgr;
      this.resolvers = resolverRegistry;
   }

   /**
    * Perform a "basic" search. This is a search with a single string that is matched
    * against various fields.
    *
    * @param query The basic search criteria.
    * @param authorNames Each "a" argument is an individual literal; items in the list are combined via "OR" criteria
    * @param titles Each "t" argument is an individual literal; items in the list are combined via "OR" criteria
    * @param numResults
    * @return
    */
   @GET
   @Produces(MediaType.APPLICATION_JSON)
   public RestApiV1.WorkSearchResultSet
   searchWorks(@QueryParam(value = "q") String query,
               @QueryParam(value = "a") List<String> authorNames,
               @QueryParam(value = "t") List<String> titles,
               @QueryParam(value = "aid") List<String> authorIds, // specify same param with multiple values to get a list
               @QueryParam(value = "dr") List<RestApiV1.DateRangeParam> dateRanges,
               @QueryParam(value = "type") String type,
               @QueryParam(value = "off") @DefaultValue("0")   int offset,
               @QueryParam(value = "max") @DefaultValue("100") int numResults)
   {
      SearchWorksResult worksResult;

      try
      {
         WorkQueryCommand cmd = queryService.createQuery();

         // First, set query parameters
         //NOTE: a query can work without any parameters, so no need to validate that at least a 'q'
         //      or other advanced criteria was supplied
         if (query != null)
            cmd.query(query);

         if (type != null)
            cmd.queryType(type);

         authorNames.stream().forEach(cmd::queryAuthorName);
         titles.stream().forEach(cmd::queryTitle);

         // now filters/facets
         cmd.addFilterAuthor(authorIds);
         for (RestApiV1.DateRangeParam dr : dateRanges)
         {
            cmd.addFilterDate(dr.start, dr.end);
         }

         // now meta fields
         cmd.setMaxResults(numResults);
         cmd.setOffset(offset);

         // execute query
         worksResult = cmd.execute();
      }
      catch (SearchException e)
      {
         String message = "Unable to search for works.";
         logger.log(Level.SEVERE, message, e);
         throw new InternalServerErrorException(message, e);
      }


      // assemble search response data vehicle
      RestApiV1.WorkSearchResultSet resultSet = new RestApiV1.WorkSearchResultSet();
      List<BiblioSearchProxy> results = worksResult.get();
      resultSet.items = adapt(results);

      QueryStringBuilder qsb = new QueryStringBuilder();

      // query parameters common to current/next/previous queries
      qsb.add("q", query);
      qsb.add("type", type);
      authorNames.stream().forEach(name -> qsb.add("a", name));
      titles.stream().forEach(title -> qsb.add("t", title));
      authorIds.stream().forEach(authorId -> qsb.add("aid", authorId));
      dateRanges.stream().forEach(dateRange -> qsb.add("dr", dateRange.toValue()));
      qsb.add("max", Integer.valueOf(numResults));

      // query parameters specific to the current query
      QueryStringBuilder qsbCurrent = new QueryStringBuilder(qsb);
      qsbCurrent.add("off", Integer.valueOf(offset));
      resultSet.qs = qsbCurrent.toString();

      // query parameters specific to the next query
      QueryStringBuilder qsbNext = new QueryStringBuilder(qsb);
      qsbNext.add("off", Integer.valueOf(offset + numResults));
      resultSet.qsNext = qsbNext.toString();

      // query parameters specific to the previous query
      // only include previous query if available
      if (offset > 0)
      {
         QueryStringBuilder qsbPrev = new QueryStringBuilder(qsb);
         qsbPrev.add("off", Integer.valueOf(Math.max(0, offset - numResults)));
         resultSet.qsPrev = qsbPrev.toString();
      }

      return resultSet;
   }

   /**
    * Saves a new work object
    *
    * @param work
    */
   @POST
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   public RestApiV1.Work createWork(RestApiV1.Work work)
   {
      EditBibliographicEntryCommand command = repo.create();
      RepoAdapter.apply(work, command);

      try
      {
         String id = command.execute().get();
         BibliographicEntry createdWork = repo.get(id);
         return RepoAdapter.toDTO(createdWork, resolvers);
      }
      catch (Exception e)
      {
         String message = "Unable to save new work.";
         logger.log(Level.SEVERE, message, e);
         throw new InternalServerErrorException(message, e);
      }
   }

   /**
    * Perform operations on an individual work
    *
    * @param id
    * @return JAX-RS subresource for the work
    */
   @Path("/{id}")
   public WorkResource getWork(@PathParam("id") String id)
   {
      EntityPersistenceAdapter<BibliographicEntry, EditBibliographicEntryCommand> helper = new WorkPersistenceAdapter(id);
      return new WorkResource(helper, serviceMgr, resolvers);
   }

   private List<RestApiV1.WorkSearchResult> adapt(List<BiblioSearchProxy> origList)
   {
      return origList == null
            ? Collections.emptyList()
            : origList.stream().map(this::adapt).collect(toList());
   }

   private RestApiV1.WorkSearchResult adapt(BiblioSearchProxy orig)
   {
      RestApiV1.WorkSearchResult dto = new RestApiV1.WorkSearchResult();
      dto.id = orig.id;
      dto.ref = orig.token == null ? null : EntryIdDto.adapt(resolvers.getReference(orig.token));
      dto.type = orig.type;
      dto.label = orig.label;
      dto.title = orig.title;
      dto.uri = orig.uri;
      dto.pubYear = orig.pubYear;
      dto.summary = orig.summary;
      if (orig.authors != null)
         dto.authors = orig.authors.stream().map(this::adapt).collect(toList());
      return dto;
   }

   private RestApiV1.AuthorRef adapt(BiblioSearchProxy.AuthorProxy author)
   {
      RestApiV1.AuthorRef dto = new RestApiV1.AuthorRef();
      dto.authorId = author.authorId;
      dto.firstName = author.firstName;
      dto.lastName = author.lastName;
      dto.role = author.role;

      return dto;
   }

   private class WorkPersistenceAdapter implements EntityPersistenceAdapter<BibliographicEntry, EditBibliographicEntryCommand>
   {
      private final String id;

      public WorkPersistenceAdapter(String id)
      {
         this.id = id;
      }

      @Override
      public String getId()
      {
         return id;
      }

      @Override
      public BibliographicEntry get()
      {
         return repo.get(id);
      }

      @Override
      public void edit(Consumer<EditBibliographicEntryCommand> modifier)
      {
         EditBibliographicEntryCommand command;

         try
         {
            command = repo.edit(id);
         }
         catch (IllegalArgumentException e)
         {
            String message = "Unable to update work {" + id + "}";
            logger.log(Level.WARNING, message, e);
            throw new NotFoundException(message, e);
         }
         catch (Exception e)
         {
            String message = "Encountered an unexpected error while attempting to edit work {" + id + "}.";
            logger.log(Level.SEVERE, message, e);
            throw new InternalServerErrorException(message, e);
         }

         try
         {
            modifier.accept(command);
         }
         catch (WebApplicationException e)
         {
            // allow JAX-RS exceptions to be thrown as-is
            throw e;
         }
         catch (Exception e)
         {
            // wrap all other exceptions in a server error
            String message = "Encountered an unexpected error while updating work {" + id + "}.";
            logger.log(Level.SEVERE, message, e);
            throw new InternalServerErrorException(message, e);
         }


         try
         {
            command.execute().get();
         }
         catch (Exception e)
         {
            String message = "Unable to save updated work {" + id + "}.";
            logger.log(Level.SEVERE, message, e);
            throw new InternalServerErrorException(message, e);
         }
      }

      @Override
      public void delete()
      {
         try
         {
            repo.remove(id);
         }
         catch (IllegalArgumentException e)
         {
            String message = "Unable to find work {" + id + "} for deletion.";
            logger.log(Level.WARNING, message, e);
            throw new NotFoundException(message, e);
         }
         catch (Exception e)
         {
            String message = "Unable to delete work {" + id + "}.";
            logger.log(Level.SEVERE, message, e);
            throw new InternalServerErrorException(message, e);
            // TODO: error might be related to something user-related.
         }
      }
   }

   /**
    * Query string builder utility class
    *
    * Should probably be refactored into its own class somewhere
    */
   private static class QueryStringBuilder
   {
      private static final String ENCODING = "UTF-8";

      private List<NameValuePair> params;

      /**
       * Default constructor
       */
      public QueryStringBuilder()
      {
         params = new ArrayList<>();
      }

      /**
       * Copy constructor
       *
       * @param original
       */
      public QueryStringBuilder(QueryStringBuilder original)
      {
         params = new ArrayList<>(original.params);
      }

      /**
       * Adds a param/value entry to the query string.
       * Multiple entries of the same parameter are allowed.
       *
       * @param param
       * @param value
       */
      public void add(String param, Object value)
      {
         if (value != null)
         {
            params.add(new BasicNameValuePair(param, String.valueOf(value)));
         }
      }

      /**
       * Build the final query string
       */
      @Override
      public String toString()
      {
         return URLEncodedUtils.format(params, ENCODING);
      }
   }

}