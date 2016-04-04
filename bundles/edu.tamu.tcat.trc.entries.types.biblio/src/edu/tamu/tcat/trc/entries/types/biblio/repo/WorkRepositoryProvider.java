package edu.tamu.tcat.trc.entries.types.biblio.repo;

public interface WorkRepositoryProvider
{
   /**
    * Attempts to provide a work repository.
    * May return {@code null} if no instance is available or if a new instance could not be created.
    *
    * @return work repository instance
    */
   WorkRepository getRepository();
}
