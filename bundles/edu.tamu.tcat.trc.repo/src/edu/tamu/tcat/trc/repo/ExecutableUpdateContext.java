package edu.tamu.tcat.trc.repo;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;

public interface ExecutableUpdateContext<StorageType> extends UpdateContext<StorageType>
{
   /**
    * Called by the edit command to enact the edits within the persistence layer.
    * Takes a {@link Supplier} that will generate a new DTO for the updated entry
    * based on an {@link UpdateContext} supplied by the {@link DocumentRepository}.
    *
    * <p>While there are many options for implementing edit commands and generator
    * functions, this is designed to be easy to use in combination with the {@link ChangeSet}
    * API. To do this, the mutator changes can be applied using {@link ChangeSet#apply(Object)}
    * and the resulting object returned from the generator.
    *
    * @param generator A function supplied by the edit command that will modified an initial
    *    DTO to be persisted by the {@code DocumentRepository}. Note that the supplied value
    *    may be modified as is.
    *
    * @return A future that will be completed once object has been stored in the
    *    persistence layer. Note that it is currently an implementation detail as whether
    *    post-commit hooks will have completed execution prior to returning this result.
    */
   CompletableFuture<StorageType> update(Function<StorageType, StorageType> mutator);
}
