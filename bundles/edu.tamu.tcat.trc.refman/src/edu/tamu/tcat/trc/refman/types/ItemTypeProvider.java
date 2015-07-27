package edu.tamu.tcat.trc.refman.types;

import java.util.Collection;

public interface ItemTypeProvider
{

   Collection<String> listDefinedTypes();

   ItemType getItemType(String typeId);
}
