package edu.tamu.tcat.trc.entries.types.reln.impl.repo;

import static java.text.MessageFormat.format;

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
   public void setProperty(String key, String value)
   {
      Objects.requireNonNull(value);

      changes.add(format("property[{0}] = {1}", key, value),
            dto -> dto.properties.put(key, value));
   }

   @Override
   public void clearProperty(String key)
   {
      changes.add(format("property[{0}] CLEAR", key),
            dto -> dto.properties.remove(key));
   }

}
