package edu.tamu.tcat.trc.resources.books.discovery;

import java.time.temporal.TemporalAccessor;

import edu.tamu.tcat.trc.resources.books.resolve.CopyResolverStrategy;

/**
 * A structured representation of a query for use when searching for digital copies of a book.
 */
public interface ContentQuery
{
   // TODO supply default DTO (JSON serializable) and Adapter interface.

   /**
    *  Returns the query string supplied by the client. This query should be interpreted
    *  generally as a keyword query with no internal structure such as quotation marks for
    *  specific phrases or token modifiers such as the minus sign to search for records that
    *  do not contain a particular term.
    *
    *  <p>
    *  Note that this query is typically used to search for a copy of a book based on metadata
    *  records about that book such as the title, author and publisher. It is not intended that
    *  implementations will provide support for full-text searching or that clients will use
    *  the copy search API to perform full text searching over a digital library.
    *
    *  @return The supplied query. Will not be {@code null}. If the empty string is returned,
    *       the behavior is implementation dependent. Some {@link CopyResolverStrategy}
    *       implementation may support searching based on the author query.
    */
   String getKeyWordQuery();

   /**
    * @return A keyword search string to be applied to author information, if available from
    *       the underlying copy service.
    */
   String getAuthorQuery();

   /**
    *  Returns the lower-bound for a date-range filter to this query. This may be any valid
    *  {@link TemporalAccessor}, but is typically expected to contain information about the
    *  year of publication. In general, more fine-grained publication date information is rarely
    *  available (or consistent) both to the users executing a search or within the metadata
    *  captured by large scale digitization efforts. However, {@link CopyResolverStrategy}s that
    *  do have access to fine grained publication information may take advantage of if provided
    *  in the query.
    *
    *  <p>
    *  Note that this field may not be implemented consistently across all {@link CopyResolverStrategy}s,
    *  since different data sources may record publication dates more or less consistently. In
    *  general, this should be thought of as prioritizing copies that are known to have been
    *  published after the supplied date rather than
    *
    *
    * @return The earliest date for which results should be retrieved. May be {@code null} in
    *       which case no lower bound is assumed.
    *
    */
   TemporalAccessor getDateRangeStart();

   /**
    *  Returns the upper-bound for a date-range filter to this query.
    *
    * @return The latest date for which results should be retrieved. May be {@code null} in
    *       which case no upper bound is assumed.
    * @see #getDateRangeStart()
    */
   TemporalAccessor getDateRangeEnd();

   /**
    * @return the index of the first item to be returned for paged queries. Note that this may
    *    not be supported by all implementations. Specifically, for implementations whose
    *    results are not deterministically ordered, this may produce inconsistent results
    *    across multiple queries.
    */
   int getOffset();

   /**
    * @return the number items to be returned. May be used in conjunction with
    *       {@link #getOffset()} for paged queries. Unlike {@link #getOffset()},
    *       {@link CopySearchService} implementations are implementations are expected to
    *       restrict the number of results to the requested limit. Most implementations will
    *       define default and max limits in the event that the query does not supply one.
    */
   int getLimit();
}
