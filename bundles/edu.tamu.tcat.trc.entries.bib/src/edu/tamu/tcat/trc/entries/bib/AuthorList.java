package edu.tamu.tcat.trc.entries.bib;

/**
 * Defines a list of authors associated with a bibliographic work.
 */
public interface AuthorList extends Iterable<AuthorReference>
{
   /**
    * @param ix The index of the author to retrieve.
    * @return The specified entry in the author list.
    * @throws IndexOutOfBoundsException if {@code ix < 0 || ix >= size()}. 
    */
   AuthorReference get(int ix) throws IndexOutOfBoundsException;
   
   /**
    * @return The number of authors in this list.
    */
   int size();

}
