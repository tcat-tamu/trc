package edu.tamu.tcat.trc.resolver;

/**
 * A 'struct' representation of an EntryId suitable for exposing via a REST API.
 */
public class EntryIdDto
{
   public String id;
   public String type;
   public String token;

   public static EntryIdDto adapt(EntryId orig, EntryResolverRegistry resolvers)
   {
      EntryIdDto dto = new EntryIdDto();
      dto.id = orig.getId();
      dto.type = orig.getType();
      dto.token = resolvers.tokenize(orig);
      return dto;
   }

   public static EntryIdDto adapt(EntryReference<?> orig)
   {
      EntryIdDto dto = new EntryIdDto();
      dto.id = orig.getId();
      dto.type = orig.getType();
      dto.token = orig.getToken();
      return dto;
   }
}
