package edu.tamu.tcat.trc.repo;

public interface IdFactoryProvider
{
   IdFactory getIdFactory(String context);

   default IdFactoryProvider extend(String subContext)
   {
      return new DelegateIdFactoryProvider(this, subContext);
   }
}
