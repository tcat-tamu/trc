package edu.tamu.tcat.trc.entries.bib.search;

import java.time.Year;


/**
 * Command for use in querying the underlying search service
 */
//TODO: might need to clarify between "set" methods and "filter" methods which do similar things --pb
//TODO: this API must be altered if facets or new fields are added. Could use a more flexible API,
//      perhaps by using a factory pattern here, e.g. "getFacetBuilder(Class<T extends FacetQueryBuilder>)"
public interface WorkQueryCommand
{

   // TODO probably should use a builder pattern; this seems like a builder already --pb
   // TODO should be able to serialize a query

   /**
    * Supply a free-text, keyword query to be executed. In general, the supplied query should
    * be executed against a wide range of fields (e.g., author, title, abstract, publisher, etc.)
    * with different fields being assigned different levels of boosting (per-field weights).
    * The specific fields to be searched and the relative weights associated with different
    * fields is a
    *
    * @param q
    * @return
    */
   void setQuery(String q);

   /**
    * @param q The value to search for in the title.
    * @return
    */
   void setTitleQuery(String q);

   /**
    * Set the name of the author to search for. A best effort will be made to match books whose
    * authors correspond to this name, either specifically within the bibliographic table or
    * within the affiliated person record.
    *
    * @param authorName
    * @return
    */
   void setAuthorName(String authorName);

   /**
    * Filter results based on the supplied list of author ids. Only entries that
    * specifically match the supplied authors will be returned.
    *
    * @param ids
    * @return
    */
   //TODO: what is an "author id"? likely not their name --pb
   void filterByAuthor(String... ids);

   /**
    * Filter results to return only those entries that are published between the supplied dates.
    *
    * @param after The lower bound of the date filter. If {@code null} indicates that no
    *       lower bound should be enforced.
    * @param before The upper bound of the date filter. If {@code null} indicates that no upper
    *       bound should be enforced.
    * @return
    */
   void filterByDate(Year after, Year before);

   /**
    * Filter results to a specific geographical location.
    *
    * @param location
    * @return
    */
   void filterByLocation(String location);

   /**
    * Sets the index of the first result to be returned. Useful in conjunction with
    * {@link WorkQueryCommand#setMaxResults(int) } to support result paging. Note that
    * implementations are <em>strongly</em> encouraged to make a best-effort attempt to
    * preserve result order across multiple invocations of the same query.  In general, this
    * is a challenging problem in the face of updates to the underlying index and implementations
    * are not required to guarantee result order consistency of result order across multiple
    * calls.
    *
    * @param start
    * @return
    */
   //TODO: why is this in the query and not the result? "set max" makes sense here, but could
   //      also be moved to the result set --pb
   void setStartIndex(int start);

   /**
    * Specify the maximum number of results to be returned. Implementations may return fewer
    * results but must not return more.
    * <p>
    * If not specified, the default is 25.
    *
    * @param ct
    * @return
    */
   //TODO: note what implementations may do with limited results; e.g. blind truncate, sort
   //      by relevance or some other field
   void setMaxResults(int ct);

   //TODO: javadoc; especially note thread safety, whether this is blocking, how to cancel,
   //      how long it may take, whether the same result may be accessed multiple times, etc. --pb
   SearchWorksResult execute();
}
