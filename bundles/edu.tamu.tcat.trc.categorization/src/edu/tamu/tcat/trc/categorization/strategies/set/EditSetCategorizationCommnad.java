package edu.tamu.tcat.trc.categorization.strategies.set;

import edu.tamu.tcat.trc.categorization.EditCategorizationCommand;

public interface EditSetCategorizationCommnad extends EditCategorizationCommand
  {
     SetNodeMutator add();

     SetNodeMutator remove(String id);
  }