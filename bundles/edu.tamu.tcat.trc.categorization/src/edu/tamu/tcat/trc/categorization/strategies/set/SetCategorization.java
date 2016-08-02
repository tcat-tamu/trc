package edu.tamu.tcat.trc.categorization.strategies.set;

import java.util.Set;

import edu.tamu.tcat.trc.categorization.CategorizationNode;
import edu.tamu.tcat.trc.categorization.CategorizationScheme;

public interface SetCategorization extends CategorizationScheme
{
   Set<CategorizationNode> getElements();
}