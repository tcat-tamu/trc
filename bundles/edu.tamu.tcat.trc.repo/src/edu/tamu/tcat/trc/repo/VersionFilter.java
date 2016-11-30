package edu.tamu.tcat.trc.repo;

import java.time.Instant;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface VersionFilter
{
   /**
    * Filters the returned version set.
    *
    * @return UUIDs for the actors to filter by. If empty, no restrictions will be applied.
    *    Defaults to empty set.
    */
   default Set<UUID> filterByActor()
   {
      return Collections.emptySet();
   }

   /**
    * If present, filter results to return only those versions modified after the supplied date.
    *
    * @return The date to filter by. Defaults to empty.
    */
   default Optional<Instant> after()
   {
      return Optional.empty();
   }

   /**
    * If present, filter results to return only those versions modified before the supplied date.
    *
    * @return The date to filter by. Defaults to empty.
    */
   default Optional<Instant> before()
   {
      return Optional.empty();
   }

   /**
    * If present, filter results to return only those versions modified after the supplied version.
    *
    * @return The version id to filter by. Defaults to empty.
    */
   default Optional<String> afterVersion()
   {
      return Optional.empty();
   }

   /**
    * If present, filter results to return only those versions modified before the supplied version.
    *
    * @return The version id to filter by. Defaults to empty.
    */
   default Optional<String> beforeVersion()
   {
      return Optional.empty();
   }

   /**
    * If <code>true</code>, return the versions in reverse chronological order. If used with
    * a limit, this will return the first elements rather than the last.
    *
    * @return Whether the returned version should be returned in reverse chronological order.
    *       Defaults to <code>false</code>.
    */
   default boolean reverseChronologicalOrder()
   {
      return false;
   };

   /**
    * If present, return at most the <code>X</code> most recent versions (least recent, if used in
    * conjunction with {@link #reverseChronologicalOrder()}), where <code>X</code> is the value
    * supplied by the optional.
    *
    * @return The number or versions to limit the result set to.
    */
   default Optional<Integer> limit()
   {
      return Optional.empty();
   }
}
