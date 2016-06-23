package edu.tamu.tcat.trc.repo;

import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

public interface ChangeSet<Type>
{
   ChangeAction add(String property, Consumer<Type> mutatorFn);

   <SubType> ChangeSet<SubType> partial(String property, Function<Type, SubType> selector);

   Type apply(Type dto);

   public interface ChangeAction
   {
      UUID getId();

      String getProperty();
   }
}