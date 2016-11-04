package edu.tamu.tcat.trc.resolver;

import static java.text.MessageFormat.format;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Defines a simple typed-identifier for use in uniquely identifying a given entity.
 * This assumes that some underlying system, identified by a semantic type, supplies
 * identifiers for entities that are unique within its scope, but that may or may not be
 * globally unique. This allows for a simple, easily serializable representation of an entry
 * that can be interpreted appropriately by any service that recognized the semantic type
 * of the entry.
 *
 * Notably, entry ids can be generated and resolved via a simple entry resolution system.
 */
public class EntryId
{
   // TODO what about sub-parts, eg. a Volume of a work?

   private static final String TYPE = "type";
   private static final String ID = "id";

   /** Defines the type of entry this reference pertains to. */
   private final String type;

   /** The unique identifier for a particular entry. */
   private final String id;

   public EntryId(String id, String type)
   {
      checkParams(id, type);

      this.id = id;
      this.type = type;
   }

   public String getId()
   {
      return id;
   }

   /**
    * @return The semantic type of the entity identified by this id.
    */
   public String getType()
   {
      return type;
   }

   /**
    * @return A map-based representation of this id that is easy to serialized into JSON.
    *    This process may be reversed using {@link #fromMap(Map)}.
    * @deprecated Use EntryIdDto.adapt instead
    */
   @Deprecated
   public Map<String, String> toJsonForm()
   {
      HashMap<String, String> map = new HashMap<>();
      map.put(ID, id);
      map.put(TYPE, type);

      return map;
   }

   /**
    * @param dto The map-based representation of this id.
    * @return An entry id as defined by the map.
    * @throws IllegalArgumentException if the supplied dto was not generated by
    * @deprecated Use EntryId.fromDto instead
    */
   @Deprecated
   public static EntryId fromMap(Map<String, String> dto)
   {
      if (dto == null)
      {
         return null;
      }

      String id = dto.get(ID);
      String type = dto.get(TYPE);

      checkParams(id, type);

      return new EntryId(id, type);
   }

   /**
    * @param dto The map-based representation of this id.
    * @return An entry id as defined by the DTO.
    */
   public static EntryId fromDto(EntryIdDto dto)
   {
      return new EntryId(dto.id, dto.type);
   }

   private static void checkParams(String id, String type)
   {
      if (id == null || id.trim().isEmpty())
         throw new IllegalArgumentException("No entry id supplied.");
      if (type == null || type.trim().isEmpty())
         throw new IllegalArgumentException("No entry type supplied.");
   }

   @Override
   public String toString()
   {
      return format("Entry Reference: \n\ttype={0}\n\tid={1}", type, id);
   }

   @Override
   public boolean equals(Object obj)
   {
      if (!EntryId.class.isInstance(obj))
         return false;

      EntryId ref = EntryId.class.cast(obj);
      return Objects.equals(ref.id, id) && Objects.equals(ref.type, type);
   }

   @Override
   public int hashCode()
   {
      return Objects.hash(id, type);
   }
}
