package edu.tamu.tcat.trc.repo.id;

@FunctionalInterface
public interface IdFactoryProvider
{
   // TODO move to main TRC bundle
   IdFactory getIdFactory(String context);
}
