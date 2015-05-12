package edu.tamu.tcat.trc.entries.reln;

import java.net.URI;
import java.util.Collection;

public interface RelationshipQueryCommand
{

   Collection<Relationship> getResults();

   RelationshipQueryCommand forEntity(URI entity, RelationshipDirection direction);

   RelationshipQueryCommand forEntity(URI entity);

   RelationshipQueryCommand byType(String typeId);

   RelationshipQueryCommand setRowLimit(int rows);

   // TODO: decide on the proper way to organize the results
   RelationshipQueryCommand oderBy();

}
