package edu.tamu.tcat.trc.categorization.strategies.list;

import java.util.List;

import edu.tamu.tcat.trc.categorization.CategorizationNode;
import edu.tamu.tcat.trc.categorization.CategorizationScheme;

public interface ListCategorization extends CategorizationScheme
{
   List<CategorizationNode> getElements();
}