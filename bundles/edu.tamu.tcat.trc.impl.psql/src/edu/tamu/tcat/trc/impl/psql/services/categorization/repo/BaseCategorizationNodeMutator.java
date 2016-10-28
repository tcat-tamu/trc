package edu.tamu.tcat.trc.impl.psql.services.categorization.repo;

import edu.tamu.tcat.trc.repo.ChangeSet;
import edu.tamu.tcat.trc.resolver.EntryId;
import edu.tamu.tcat.trc.services.categorization.CategorizationNodeMutator;

public class BaseCategorizationNodeMutator implements CategorizationNodeMutator
{
   protected final ChangeSet<PersistenceModelV1.TreeNode> changes;
   protected final String id;

   public BaseCategorizationNodeMutator(String id, ChangeSet<PersistenceModelV1.TreeNode> changes)
   {
      this.id = id;
      this.changes = changes;
   }

   @Override
   public final void setLabel(String label)
   {
      changes.add("label", dto -> dto.label = label);
   }

   @Override
   public final void setDescription(String description)
   {
      changes.add("description", dto -> dto.description = description);
   }

   @Override
   public final void associateEntryRef(EntryId ref)
   {
      changes.add("ref", dto -> dto.ref = ref.toJsonForm());
   }

}