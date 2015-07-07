/*
 * Copyright 2015 Texas A&M Engineering Experiment Station
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.tamu.tcat.trc.entries.types.biblio.dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import edu.tamu.tcat.trc.entries.types.biblio.AuthorList;
import edu.tamu.tcat.trc.entries.types.biblio.AuthorReference;


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
