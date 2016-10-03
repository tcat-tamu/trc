package edu.tamu.tcat.trc.services.categorization.strategies.list;

import java.util.List;

import edu.tamu.tcat.trc.services.categorization.CategorizationNode;
import edu.tamu.tcat.trc.services.categorization.CategorizationScheme;

public interface ListCategorization extends CategorizationScheme
{
   List<CategorizationNode> getElements();
}