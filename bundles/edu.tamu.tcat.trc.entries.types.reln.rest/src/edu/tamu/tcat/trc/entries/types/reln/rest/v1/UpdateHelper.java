package edu.tamu.tcat.trc.entries.types.reln.rest.v1;

import java.util.Set;
import java.util.logging.Level;

import javax.ws.rs.core.Response;

import edu.tamu.tcat.trc.entries.types.reln.RelationshipType;
import edu.tamu.tcat.trc.entries.types.reln.repo.AnchorMutator;
import edu.tamu.tcat.trc.entries.types.reln.repo.EditRelationshipCommand;
import edu.tamu.tcat.trc.entries.types.reln.repo.RelationshipRepository;
import edu.tamu.tcat.trc.entries.types.reln.repo.RelationshipTypeRegistry;
import edu.tamu.tcat.trc.resolver.EntryId;
import edu.tamu.tcat.trc.resolver.EntryResolverRegistry;
import edu.tamu.tcat.trc.services.rest.ApiUtils;

public class UpdateHelper
{
   // TODO merge with RestApiAdapter

   private final EntryResolverRegistry resolvers;
   private final RelationshipTypeRegistry types;

   public UpdateHelper(RelationshipRepository repo, EntryResolverRegistry resolvers)
   {
      this.resolvers = resolvers;
      this.types = repo.getTypeRegistry();
   }

   public static void checkValidity(RestApiV1.SimpleRelationship reln, String id)
   {
      if (!reln.id.equals(id))
      {
         String msg = "The id of the supplied relationship data [" + reln.id + "] does not match the id component of the URI [" + id + "]";
         throw ApiUtils.raise(Response.Status.BAD_REQUEST, msg, Level.INFO, null);
      }

      // TODO need to supply additional checks for constraints on validity.
   }

   public EditRelationshipCommand applyChanges(EditRelationshipCommand cmd, RestApiV1.SimpleRelationship relationship)
   {
      RelationshipType type = types.resolve(relationship.typeId);
      cmd.setType(type);
      cmd.setDescription(relationship.description);

      if (relationship.related != null)
      {
         cmd.clearRelatedEntries();
         relationship.related.stream()
               .forEach(anchor -> {
                  EntryId ref = resolvers.decodeToken(anchor.ref);
                  applyAnchor(cmd.editRelatedEntry(ref), anchor);
               });
      }

      if (relationship.targets != null)
      {
         cmd.clearTargetEntries();
         relationship.targets.stream()
               .forEach(anchor -> {
                  EntryId ref = resolvers.decodeToken(anchor.ref);
                  applyAnchor(cmd.editTargetEntry(ref), anchor);
               });
      }

      return cmd;
   }

   public void applyAnchor(AnchorMutator mutator, RestApiV1.SimpleAnchor anchor)
   {
      if (anchor.label != null)
         mutator.setLabel(anchor.label);

      if (anchor.properties != null)
      {
         // allows partial updates to not supply a properties map and leave
         // existing properties unchanged
         anchor.properties.keySet().forEach(key -> {
            Set<String> values = anchor.properties.get(key);
            values.forEach(value -> mutator.addProperty(key, value));
         });
      }
   }
}