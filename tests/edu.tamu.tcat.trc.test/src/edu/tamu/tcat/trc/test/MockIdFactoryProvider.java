package edu.tamu.tcat.trc.test;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import edu.tamu.tcat.trc.repo.id.IdFactory;
import edu.tamu.tcat.trc.repo.id.IdFactoryProvider;

public class MockIdFactoryProvider implements IdFactoryProvider
{
   private ConcurrentHashMap<String, AtomicInteger> counters = new ConcurrentHashMap<String, AtomicInteger>();

   @Override
   public IdFactory getIdFactory(String context)
   {
      if (!counters.contains(context))
      {
         counters.putIfAbsent(context, new AtomicInteger(0));
      }

      AtomicInteger counter = counters.get(context);

      return new MockIdFactory(counter);
   }

   public class MockIdFactory implements IdFactory
   {
      private final AtomicInteger counter;

      public MockIdFactory(AtomicInteger counter)
      {
         this.counter = counter;
      }

      @Override
      public String get() {

         int id = counter.incrementAndGet();
         return String.valueOf(id);
      };
   }
}
