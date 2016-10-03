package edu.tamu.tcat.trc.services.categorization.strategies.set;

import edu.tamu.tcat.trc.services.categorization.EditCategorizationCommand;

public interface EditSetCategorizationCommnad extends EditCategorizationCommand
  {
     SetNodeMutator add();

     SetNodeMutator remove(String id);
  }