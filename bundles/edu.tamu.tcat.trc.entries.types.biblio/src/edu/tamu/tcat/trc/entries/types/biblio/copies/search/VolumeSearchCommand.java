package edu.tamu.tcat.trc.entries.types.biblio.copies.search;

import edu.tamu.tcat.trc.search.SearchException;

public interface VolumeSearchCommand
{

   /*
    * In keeping with the "spirit of search", the window (offset + length) and other paramters
    * are configured in the query itself and not in a result with a long lifecycle.
    */
   VolumeSearchResult execute() throws SearchException;

   /**
    * Supply a "basic" free-text, keyword query to be executed. In general, the supplied query will
    * be executed against a wide range of fields (e.g., author, title, abstract, publisher, etc.)
    * with different fields being assigned different levels of boosting (per-field weights).
    * The specific fields to be searched and the relative weights associated with different
    * fields is implementation-dependent.
    *
    * @param basicQueryString The "basic" query string. May be {@code null} or empty.
    */
   void query(String basicQueryString) throws SearchException;

   /**
    * Sets the index offset of the first result to be returned. Useful in conjunction with
    * {@link VolumeSearchCommand#setMaxResults(int) } to support result paging. Note that
    * implementations are <em>strongly</em> encouraged to make a best-effort attempt to
    * preserve result order across multiple invocations of the same query.  In general, this
    * is a challenging problem in the face of updates to the underlying index and implementations
    * are not required to guarantee result order consistency of result order across multiple
    * calls.
    *
    * @param offset
    */
   void setOffset(int offset);

   /**
    * Specify the maximum number of results to be returned. Implementations may return fewer
    * results but must not return more.
    * <p>
    * If not specified, the default is 25.
    *
    * @param count
    */
   void setMaxResults(int count);
}
