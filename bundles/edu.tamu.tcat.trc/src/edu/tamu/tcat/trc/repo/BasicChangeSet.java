package edu.tamu.tcat.trc.repo;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

public class BasicChangeSet<Type> implements ChangeSet<Type>
{
   private final List<BasicChangeSet.BasicChangeAction<Type>> changes = new ArrayList<>();

   @Override
   public ChangeAction add(String property, Consumer<Type> mutatorFn)
   {
      BasicChangeSet.BasicChangeAction<Type> action = new BasicChangeSet.BasicChangeAction<>(property, mutatorFn);
      changes.add(action);
      return action;
   }

   @Override
   public <SubType> ChangeSet<SubType> partial(String property, Function<Type, SubType> selector)
   {
      return new BasicChangeSet.PartialChangeSet<>(this, property, selector);
   }

   @Override
   public Type apply(Type dto)
   {
      changes.forEach(action -> action.apply(dto));
      return dto;
   }

   private static class PartialChangeSet<SubType, ParentType> implements ChangeSet<SubType>
   {
      private final String parentProperty;
      private final ChangeSet<ParentType> delegate;
      private final Function<ParentType, SubType> selector;


      public PartialChangeSet(ChangeSet<ParentType> delegate,
                              String property,
                              Function<ParentType, SubType> selector)
      {
         this.delegate = delegate;
         this.parentProperty = property;
         this.selector = selector;
      }

      @Override
      public ChangeAction add(String property, Consumer<SubType> mutatorFn)
      {
         String prop = parentProperty + "." + property;

         return delegate.add(prop, dto -> {
            SubType relDto = selector.apply(dto);
            if (relDto != null)
               mutatorFn.accept(relDto);
         });
      }

      @Override
      public <X> ChangeSet<X> partial(String property, Function<SubType, X> selector)
      {
         String prop = parentProperty + "." + property;
         return new PartialChangeSet<>(this, prop, selector);
      }

      @Override
      public SubType apply(SubType dto)
      {
         throw new UnsupportedOperationException("Cannot apply partial change sets");
      }

   }

   private static class BasicChangeAction<Type> implements ChangeAction
   {
      private final UUID id = UUID.randomUUID();
      private final String property;
      private final Consumer<Type> mutator;

      private BasicChangeAction(String prop, Consumer<Type> mutator)
      {
         property = prop;
         this.mutator = mutator;
      }

      private void apply(Type dto)
      {
         mutator.accept(dto);
      }

      @Override
      public UUID getId()
      {
         return id;
      }

      @Override
      public String getProperty()
      {
         return property;
      }
   }
}