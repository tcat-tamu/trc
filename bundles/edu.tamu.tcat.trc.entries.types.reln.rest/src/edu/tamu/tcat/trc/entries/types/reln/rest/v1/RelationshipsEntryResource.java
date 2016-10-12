package edu.tamu.tcat.trc.entries.types.reln.rest.v1;

import java.net.URI;
import java.text.MessageFormat;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;

import edu.tamu.tcat.trc.entries.types.reln.RelationshipType;
import edu.tamu.tcat.trc.entries.types.reln.repo.RelationshipException;
import edu.tamu.tcat.trc.entries.types.reln.repo.RelationshipTypeRegistry;
import edu.tamu.tcat.trc.entries.types.reln.search.RelationshipDirection;
import edu.tamu.tcat.trc.entries.types.reln.search.RelationshipQueryCommand;
import edu.tamu.tcat.trc.entries.types.reln.search.RelationshipSearchResult;
import edu.tamu.tcat.trc.entries.types.reln.search.RelationshipSearchService;
import edu.tamu.tcat.trc.search.solr.SearchException;

/**
 * Sub-resource to obtain relationships associated with a given entry (the referent entity).
 * For example, to obtain all relationships associated with a given book, person or event.
 */
public class RelationshipsEntryResource
{
   // TODO currently unused. Need to stitch in if desired.
   // TODO extend to allow filtering by relationship type, type of entry referenced by the relationship, etc.

   private static final Logger logger = Logger.getLogger(RelationshipsEntryResource.class.getName());
   /**
    * URI of the entry to which this resource has been attached.
    */
   private final URI baseEntryUri;

   private final RelationshipSearchService searchService;
   private final RelationshipTypeRegistry typeRegistry;

   public RelationshipsEntryResource(URI baseEntryUri, RelationshipSearchService searchService, RelationshipTypeRegistry typeRegistry)
   {
      this.baseEntryUri = baseEntryUri;
      this.searchService = searchService;
      this.typeRegistry = typeRegistry;
   }

   /**
    * @return all relationships for the base entry grouped by type.
    */
   @GET
   public RestApiV1.GroupedSearchResultSet getRelationships()
   {
      try
      {
         RelationshipQueryCommand command = searchService.createQueryCommand();
         command.forEntity(baseEntryUri, RelationshipDirection.any);
         RelationshipSearchResult result = command.execute();

         // TODO simply apply groupByType adapter -- this is a fundamentally different concept than a
         //      general search.
         List<RestApiV1.RelationshipSearchResult> dtos = SearchAdapter.toDTO(result.get());

         return SearchAdapter.groupByType(baseEntryUri, dtos, this::lookupRelnTypeById);
      }
      catch (SearchException e)
      {
         String message = MessageFormat.format("Unable to search for relationships related to {}.", baseEntryUri.toString());
         logger.log(Level.WARNING, message, e);
         throw new InternalServerErrorException(message, e);
      }
   }

   /**
    * Guards against checked exceptions when looking up a {@link RelationshipType} by id.
    * @param id
    * @return
    */
   private RelationshipType lookupRelnTypeById(String id)
   {
      try
      {
         return typeRegistry.resolve(id);
      }
      catch (RelationshipException e)
      {
         String message = MessageFormat.format("Unable to find relationship type {}", id);
         throw new IllegalArgumentException(message, e);
      }
   }
}
