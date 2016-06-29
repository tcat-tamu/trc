package edu.tamu.tcat.trc.entries.types.article.docrepo;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

public class EntryField<EntryType, FieldType>
{
   public final Class<EntryType> entryType;
   public final Class<FieldType> fieldType;

   private final String field;
   private final Function<EntryType, FieldType> accessor;
   private final BiFunction<EntryType, FieldType, EntryType> mutator;

   public EntryField(String name,
                     Class<EntryType> entryType,
                     Class<FieldType> fieldType,
                     Function<EntryType, FieldType> accessor,
                     BiFunction<EntryType, FieldType, EntryType> mutator)
   {
      this.field = name;
      this.entryType = entryType;
      this.fieldType = fieldType;
      this.accessor = accessor;
      this.mutator = mutator;
   }

   public class EntryFieldValue
   {
      private boolean isSet = false;
      private FieldType value;

      public synchronized void set(FieldType value)
      {
         this.value = value;
         this.isSet = true;
      }

      public EntryType apply(EntryType entry)
      {
         if (!isSet)
            return entry;

         FieldType original = accessor.apply(entry);
         if (Objects.equals(original, value))
            return entry;

         return mutator.apply(entry, value);
      }
   }

}
