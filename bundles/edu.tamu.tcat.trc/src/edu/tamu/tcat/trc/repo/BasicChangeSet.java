package edu.tamu.tcat.trc.repo;

import static java.text.MessageFormat.format;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.tamu.tcat.trc.repo.ChangeSet.ApplicableChangeSet;

public class BasicChangeSet<Type> implements ApplicableChangeSet<Type>
{
   private final static Logger logger = Logger.getLogger(BasicChangeSet.class.getName());
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

      private void applyChange(ParentType dto, String prop, Consumer<SubType> mutatorFn)
      {
         SubType relDto = null;
         try
         {
            relDto = selector.apply(dto);
         }
         catch (RuntimeException ex)
         {
            String msg = "Failed to apply update changes to {0}. Sub-property selector failed.";
            logger.log(Level.SEVERE, ex, () -> format(msg, prop));

            // TODO should we rethrow? may want to apply the changes that we are able to
            throw ex;
         }

         try
         {
            if (relDto != null)
               mutatorFn.accept(relDto);
         }
         catch (RuntimeException ex)
         {
            String msg = "Failed to apply update changes to {0}. Property mutator failed.";
            logger.log(Level.SEVERE, ex, () -> format(msg, prop));

            // TODO should we rethrow? may want to apply the changes that we are able to
            throw ex;
         }
      }

      @Override
      public ChangeAction add(String property, Consumer<SubType> mutatorFn)
      {
         String prop = parentProperty + "." + property;
         return delegate.add(prop, dto -> applyChange(dto, prop, mutatorFn));
      }

      @Override
      public <X> ChangeSet<X> partial(String property, Function<SubType, X> selector)
      {
         String prop = parentProperty + "." + property;
         return new PartialChangeSet<>(this, prop, selector);
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
      public String getMessage()
      {
         return property;
      }
   }
}