package edu.tamu.tcat.trc.entries.types.biblio.repo.copies;

import edu.tamu.tcat.trc.entries.types.biblio.copies.CopyReference;
import edu.tamu.tcat.trc.repo.DocumentRepository;

/**
 * @deprecated copy references are now stored locally on works
 */
@Deprecated
public interface CopyReferenceRepositoryProvider
{
   /**
    * Attempts to provide a copy repository.
    * May return {@code null} if no instance is available or if a new instance could not be created.
    *
    * @return copy reference repository instance
    */
   DocumentRepository<CopyReference, EditCopyReferenceCommand> getRepository();
}
