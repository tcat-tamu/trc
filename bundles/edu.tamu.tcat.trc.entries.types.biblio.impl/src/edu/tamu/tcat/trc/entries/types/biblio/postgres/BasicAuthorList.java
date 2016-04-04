package edu.tamu.tcat.trc.entries.types.biblio.postgres;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import edu.tamu.tcat.trc.entries.types.biblio.AuthorList;
import edu.tamu.tcat.trc.entries.types.biblio.AuthorReference;

public class BasicAuthorList implements AuthorList
{
   private final List<AuthorReference> authorReferences;

   public BasicAuthorList(List<AuthorReference> refs)
   {
      ArrayList<AuthorReference> copy = new ArrayList<>(refs);
      this.authorReferences = Collections.unmodifiableList(copy);
   }

   @Override
   public Iterator<AuthorReference> iterator()
   {
      return authorReferences.iterator();
   }

   @Override
   public AuthorReference get(int index) throws IndexOutOfBoundsException
   {
      return authorReferences.get(index);
   }

   @Override
   public int size()
   {
      return authorReferences.size();
   }

}