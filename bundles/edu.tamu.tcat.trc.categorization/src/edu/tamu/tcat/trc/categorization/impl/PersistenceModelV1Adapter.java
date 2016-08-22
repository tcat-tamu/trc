package edu.tamu.tcat.trc.categorization.impl;

import edu.tamu.tcat.trc.entries.core.resolver.EntryReference;
import edu.tamu.tcat.trc.entries.core.resolver.EntryResolverRegistry;

public abstract class PersistenceModelV1Adapter
{
   public static EntryReference copy(EntryReference ref)
   {
      if (ref == null)
         return null;

      EntryReference dto = new EntryReference();
      dto.id = ref.id;
      dto.type = ref.type;

      return dto;
   }

   /**
    * Note that the returned {@link TreeCategorizationImpl} MUST have the associated
    * categorization set prior to access.
    *
    * @param registry
    * @param dto
    * @return
    */
   public static TreeCategorizationImpl toDomainModel(EntryResolverRegistry registry,
                                                  PersistenceModelV1.TreeCategorizationStrategy dto)
   {
      return new TreeCategorizationImpl(dto, registry);
   }
}
