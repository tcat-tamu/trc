package edu.tamu.tcat.trc.services.categorization.strategies.set;

import java.util.Set;

import edu.tamu.tcat.trc.services.categorization.CategorizationNode;
import edu.tamu.tcat.trc.services.categorization.CategorizationScheme;

public interface SetCategorization extends CategorizationScheme
{
   Set<CategorizationNode> getElements();
}