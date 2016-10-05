package edu.tamu.tcat.trc.impl.psql.services.categorization.model;

import static java.text.MessageFormat.format;

import edu.tamu.tcat.account.Account;
import edu.tamu.tcat.trc.entries.core.resolver.EntryReference;
import edu.tamu.tcat.trc.entries.core.resolver.EntryResolverRegistry;
import edu.tamu.tcat.trc.entries.core.resolver.InvalidReferenceException;
import edu.tamu.tcat.trc.impl.psql.services.categorization.repo.PersistenceModelV1;
import edu.tamu.tcat.trc.services.ServiceContext;
import edu.tamu.tcat.trc.services.categorization.CategorizationNode;
import edu.tamu.tcat.trc.services.categorization.CategorizationRepo;
import edu.tamu.tcat.trc.services.categorization.CategorizationScheme;
import edu.tamu.tcat.trc.services.categorization.CategorizationScope;

public abstract class CategorizationImpl implements CategorizationScheme
{
   private final String id;
   private final String scopeId;
   private final String key;
   private final Strategy strategy;
   private final String title;
   private final String description;

   private ServiceContext<CategorizationRepo> context;

   public CategorizationImpl(PersistenceModelV1.CategorizationScheme scheme)
   {
      this.id = scheme.id;
      this.scopeId = scheme.scopeId;
      this.key = scheme.key;
      this.strategy = CategorizationScheme.Strategy.valueOf(scheme.strategy);

      this.title = scheme.title;
      this.description = scheme.description;
   }

   public void setContext(ServiceContext<CategorizationRepo> context)
   {
      this.context = context;
   }

   @Deprecated // use #setContext
   public void setScope(CategorizationScope scope)
   {
      this.context = CategorizationRepo.makeContext(scope.getAccount(), scope.getScopeId());
   }

   protected Account getAccount()
   {
      if (context == null)
         throw new IllegalStateException("No categorization scope has been provided.");

      return context.getAccount().orElse(null);
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