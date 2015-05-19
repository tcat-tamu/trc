package edu.tamu.tcat.trc.entries.bib.dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import edu.tamu.tcat.trc.entries.bib.AuthorList;
import edu.tamu.tcat.trc.entries.bib.AuthorReference;


public class AuthorListDV
{
   public int size;
   public List<AuthorRefDV> refs;

   public AuthorListDV()
   {
   }

   public static AuthorListDV create(AuthorList authorList)
   {
      AuthorListDV authors = new AuthorListDV();
      authors.size = authorList.size();
      authors.refs = new ArrayList<>();
      for (AuthorReference ref : authorList)
      {
         authors.refs.add(AuthorRefDV.create(ref));
      }

      return authors;
   }

   public static AuthorList instantiate(List<AuthorRefDV> refs)
   {
      if (refs == null)
         return new AuthorListImpl(new ArrayList<>());

      List<AuthorReference> authRefs = refs.stream()
                                           .map(AuthorRefDV::instantiate)
                                           .collect(Collectors.toList());
      return new AuthorListImpl(authRefs);
   }

   public static class AuthorListImpl implements AuthorList
   {
      private final List<AuthorReference> authRef;

      public AuthorListImpl(List<AuthorReference> refs)
      {
         this.authRef = Collections.unmodifiableList(new ArrayList<>(refs));
      }

      @Override
      public Iterator<AuthorReference> iterator()
      {
         return authRef.iterator();
      }

      @Override
      public AuthorReference get(int ix) throws IndexOutOfBoundsException
      {
         return authRef.get(ix);
      }

      @Override
      public int size()
      {
         return authRef.size();
      }

   }

}
