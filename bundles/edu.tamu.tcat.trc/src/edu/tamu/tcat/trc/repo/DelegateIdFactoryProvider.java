package edu.tamu.tcat.trc.repo;

/**
 * Recursive wrapping decorator for encapsulating arbitrarily nested ID Factory context
 */
public class DelegateIdFactoryProvider implements IdFactoryProvider
{
   private final IdFactoryProvider idFactoryProvider;
   private final String context;

   public DelegateIdFactoryProvider(IdFactoryProvider idFactoryProvider, String context)
   {
      this.idFactoryProvider = idFactoryProvider;
      this.context = context;
   }

   @Override
   public IdFactory getIdFactory(String subContext)
   {
      return idFactoryProvider.getIdFactory(context + subContext);
   }

   @Override
   public IdFactoryProvider extend(String subContext)
   {
      return new DelegateIdFactoryProvider(idFactoryProvider, context + subContext);
   }

}
