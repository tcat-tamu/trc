package edu.tamu.tcat.trc.entries.types.biblio.search;

import java.time.Period;
import java.time.Year;
import java.util.Collection;

import edu.tamu.tcat.trc.search.SearchException;


/**
 * Command for use in querying the associated {@link WorkSearchService} which provides
 * search proxy instances.
 * <p>
 * A {@link WorkQueryCommand} is intended to be initialized, executed a single time, provide results,
 * and be discarded.
 * <p>
 * The various "query" methods are intended to be for user-entered criteria which results in "like",
 * wildcard, or otherwise interpreted query criteria which may apply to multiple fields of the index.
 * Alternately, the various "filter" methods are intended for specific criteria which typically
 * applies to faceted searching or to known criteria for specific stored data.
 */
public interface WorkQueryCommand
{
   //TODO: javadoc; especially note thread safety, whether this is blocking, how to cancel,
   //      how long it may take, whether the same result may be accessed multiple times, etc. --pb
   /**
    * Execute this query command after it has been parameterized. The query itself contains all
    * parameters and refinement criteria, and the result is simply a listing of matches.
    *
    * @return
    * @throws SearchException
    */
   /*
    * In keeping with the "spirit of search", the window (offset + length) and other paramters
    * are configured in the query itself and not in a result with a long lifecycle.
    */
   SearchWorksResult execute() throws SearchException;

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
    * Add title search criteria. Each invocation will add a literal element to a compound
    * "OR" criteria.
    *
    * @param q The value to search for in titles.
    */
   void queryTitle(String title) throws SearchException;

   /**
    * Add author name search criteria. Each invocation will add a literal element to a compound
    * "OR" criteria.
    * A best effort will be made to match books whose
    * authors correspond to this name, either specifically within the bibliographic table or
    * within the affiliated person record.
    *
    * @param authorName
    */
   void queryAuthorName(String authorName) throws SearchException;

   /**
    * Futher restrict the results to those associated with the provided authors by their identifiers.
    * Each identifier should correspond to a unique author within the search engine.
    * <p>
    * This method is additive, so subsequent calls do not override but combine.
    * <p>
    * An author identifier is to be interpreted by the search engine.
    *
    * @param authorIds
    * @throws SearchException If the identifiers are not valid.
    * @see #clearFilterAuthor()
    */
   void addFilterAuthor(Collection<String> authorIds) throws SearchException;

   void clearFilterAuthor() throws SearchException;

   /**
    * Restrict the results to those associated with the provided date {@link Period}s.
    * If any periods overlap, the resulting filter criteria will be the union of all provided.
    *
    * @param periods
    * @throws SearchException If the identifiers are not valid.
    */
   void addFilterDate(Year start, Year end) throws SearchException;

   // Add an unbounded date filter of the form [start, *)
   //void addFilterDateStart(Year start) throws SearchException;
   // Add an unbounded date filter of the form (*, end]
   //void addFilterDateEnd(Year end) throws SearchException;

   void clearFilterDate() throws SearchException;

//   /**
//    * Filter results to a specific geographical location.
//    *
//    * @param location
//    */
//   void filterByLocation(String location);

   /**
    * Sets the index offset of the first result to be returned. Useful in conjunction with
    * {@link WorkQueryCommand#setMaxResults(int) } to support result paging. Note that
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
   //TODO: note what implementations may do with limited results; e.g. blind truncate, sort
   //      by relevance or some other field
   void setMaxResults(int count);
}
