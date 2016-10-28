package edu.tamu.tcat.trc.entries.types.reln.impl.repo;

import static java.text.MessageFormat.format;

import java.util.HashSet;
import java.util.Objects;

import edu.tamu.tcat.trc.entries.types.reln.repo.AnchorMutator;
import edu.tamu.tcat.trc.repo.ChangeSet;

public class AnchorMutatorImpl implements AnchorMutator
{
   private final ChangeSet<DataModelV1.Anchor> changes;

   public AnchorMutatorImpl(ChangeSet<DataModelV1.Anchor> changes)
   {
      this.changes = changes;
   }

   @Override
   public void addProperty(String key, String value)
   {
      Objects.requireNonNull(value);
      changes.add(format("property[{0}] = {1}", key, value.trim()),
            dto -> dto.properties
                     .computeIfAbsent(key, (k) -> new HashSet<>())
                     .add(value.trim()));
   }

   @Override
   public void clearProperty(String key)
   {
      changes.add(format("property[{0}] CLEAR", key),
            dto -> dto.properties.remove(key));
   }

}
