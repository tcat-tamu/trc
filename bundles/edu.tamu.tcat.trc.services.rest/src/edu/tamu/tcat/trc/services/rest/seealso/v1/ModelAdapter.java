package edu.tamu.tcat.trc.services.rest.seealso.v1;

import java.util.Collection;
import java.util.HashMap;
import java.util.TreeSet;

import edu.tamu.tcat.trc.resolver.EntryId;
import edu.tamu.tcat.trc.resolver.EntryReference;
import edu.tamu.tcat.trc.resolver.EntryResolverRegistry;
import edu.tamu.tcat.trc.services.seealso.Link;

public class ModelAdapter
{
   private final EntryResolverRegistry resolvers;

   public ModelAdapter(EntryResolverRegistry resolvers)
   {
      this.resolvers = resolvers;

   }

   public RestApiV1.SeeAlso adapt(EntryId eId, Collection<Link> links)
   {
      RestApiV1.SeeAlso dto = new RestApiV1.SeeAlso();
      dto.root = adapt(eId);
      dto.links = new HashMap<>();

      links.stream()
            .map(link -> getOther(link, dto.root.token))
            .map(resolvers::decodeToken)
            .map(entryId -> adapt(entryId))
            .forEach(link -> {
               dto.links.computeIfAbsent(link.type, key -> new TreeSet<>(ModelAdapter::compare)).add(link);
            });

      return dto;
   }

   private static String getOther(Link link, String token)
   {
      return link.getSource().equals(token)
            ? link.getTarget()
            : link.getSource();
   }

   private static int compare(RestApiV1.LinkTarget a, RestApiV1.LinkTarget b)
   {
      int result = a.label.compareTo(b.label);
      if (result != 0)
         result = a.id.compareTo(b.id);

      return result;
   }

   public RestApiV1.LinkTarget adapt(EntryId eId)
   {
      RestApiV1.LinkTarget dto = new RestApiV1.LinkTarget();
      EntryReference<Object> ref = resolvers.getReference(eId);
      dto.id = ref.getId();
      dto.type = ref.getType();
      dto.token = ref.getToken();
      dto.label = ref.getHtmlLabel();

      return dto;
   }

   public RestApiV1.SeeAlsoLink adapt(Link link)
   {
      RestApiV1.SeeAlsoLink dto = new RestApiV1.SeeAlsoLink();
      dto.source = adapt(resolvers.decodeToken(link.getSource()));
      dto.source = adapt(resolvers.decodeToken(link.getTarget()));

      return dto;
   }

}
