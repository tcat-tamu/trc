package edu.tamu.tcat.trc.entries.types.reln;

import edu.tamu.tcat.trc.resolver.EntryReference;

public interface RelationshipProvider
{
   void register(RelationshipInferenceStrategy strategy);

   GroupedRelationshipSet getRelationships(EntryReference<?> ref);
}
