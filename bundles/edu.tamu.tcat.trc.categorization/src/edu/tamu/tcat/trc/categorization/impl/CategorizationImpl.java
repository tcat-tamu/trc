package edu.tamu.tcat.trc.categorization.impl;

import static java.text.MessageFormat.format;

import edu.tamu.tcat.account.Account;
import edu.tamu.tcat.trc.categorization.CategorizationNode;
import edu.tamu.tcat.trc.categorization.CategorizationScheme;
import edu.tamu.tcat.trc.categorization.CategorizationScope;
import edu.tamu.tcat.trc.categorization.CategorizationScheme.Strategy;
import edu.tamu.tcat.trc.entries.core.InvalidReferenceException;
import edu.tamu.tcat.trc.entries.core.resolver.EntryReference;
import edu.tamu.tcat.trc.entries.core.resolver.EntryResolverRegistry;

public abstract class CategorizationImpl implements CategorizationScheme
{
   private CategorizationScope scope;

   private final String id;
   private final String scopeId;
   private final String key;
   private final Strategy strategy;
   private final String title;
   private final String description;

   public CategorizationImpl(PersistenceModelV1.CategorizationScheme scheme)
   {
      this.id = scheme.id;
      this.scopeId = scheme.scopeId;
      this.key = scheme.key;
      this.strategy = CategorizationScheme.Strategy.valueOf(scheme.strategy);

      this.title = scheme.title;
      this.description = scheme.description;
   }

   public void setScope(CategorizationScope scope)
   {
      this.scope = scope;
   }

   protected Account getAccount()
   {
      if (scope == null)
         throw new IllegalStateException("No categorization scope has been provided.");

      return scope.getAccount();
   }

   @Override
   public final String getId()
   {
      return id;
   }

   @Override
   public final String getScopeId()
   {
      return scopeId;
   }

   @Override
   public final String getKey()
   {
      return key;
   }

   @Override
   public final Strategy getType()
   {
      return strategy;
   }

   @Override
   public final String getLabel()
   {
      return title;
   }

   @Override
   public final String getDescription()
   {
      return description;
   }

   public class CategorizationNodeImpl implements CategorizationNode
   {
      protected final String id;
      protected final String label;
      protected final String description;
      protected final EntryReference ref;

      protected final CategorizationScheme scheme;
      protected final EntryResolverRegistry registry;

      public CategorizationNodeImpl(EntryResolverRegistry registry,
                                    CategorizationScheme scheme,
                                    PersistenceModelV1.CategorizationNode dto)
      {
         // FIXME should be registry, not resolver
         this.registry = registry;
         this.scheme = scheme;

         this.id = dto.id;
         this.label = dto.label;
         this.description = dto.description;
         this.ref = dto.ref;
      }

      @Override
      public final String getId()
      {
         return id;
      }

      @Override
      public final String getLabel()
      {
         return label;
      }

      @Override
      public final String getDescription()
      {
         return description;
      }

      @Override
      public final CategorizationScheme getCategorization()
      {
         return scheme;
      }

      @Override
      public final EntryReference getAssociatedEntryRef()
      {
         return ref;
      }

      @Override
      public final <X> X getAssociatedEntry(Class<X> type)
      {
         if (ref == null)
            return null;

         // TODO pass through user account
         Object entry = registry.getResolver(ref).resolve(getAccount(), ref);
         if (!type.isInstance(entry))
            throw new InvalidReferenceException(ref,
                  format("The referenced entry is not an instance the expected type {0}", type));

         return type.cast(entry);
      }
   }
}