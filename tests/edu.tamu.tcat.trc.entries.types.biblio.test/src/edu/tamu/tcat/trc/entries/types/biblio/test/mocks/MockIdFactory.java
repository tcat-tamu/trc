package edu.tamu.tcat.trc.entries.types.biblio.test.mocks;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import edu.tamu.tcat.catalogentries.IdFactory;

public class MockIdFactory implements IdFactory
{
   private ConcurrentHashMap<String, AtomicInteger> counters = new ConcurrentHashMap<String, AtomicInteger>();

   @Override
   public String getNextId(String context) {
      if (!counters.contains(context))
      {
         counters.putIfAbsent(context, new AtomicInteger(0));
      }

      int id = counters.get(context).incrementAndGet();
      return String.valueOf(id);
   };
}
