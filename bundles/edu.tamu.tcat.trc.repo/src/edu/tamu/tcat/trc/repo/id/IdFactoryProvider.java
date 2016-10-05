package edu.tamu.tcat.trc.repo.id;

@FunctionalInterface
public interface IdFactoryProvider
{
   IdFactory getIdFactory(String context);
}
