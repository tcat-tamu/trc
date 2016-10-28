package edu.tamu.tcat.trc.resolver;

import java.net.URI;

import edu.tamu.tcat.account.Account;

/**
 *  A reference to an entry registered through the resolver system.
 */
public interface EntryReference<T>
{
   /**
    * @return The typed id associated with this entry.
    */
   EntryId getEntryId();

   /**
    * @return The Java type of the associated entry.
    */
   Class<T> getEntryType();

   /**
    * @return A resolver instance to support access and manipulate of this entry.
    */
   EntryResolver<T> getResolver();

   /**
    * @return The identifier for this entry. This will be unique within the scope of
    *       the entry's corresponding semantic type.
    */
   String getId();

   /**
    * @return The semantic type of this entry.
    */
   String getType();

   /**
    * @return An opaque token that can be used to retrieve this entry reference.
    */
   String getToken();

   /**
    * @return A URI that identifies this entry.
    */
   URI getUri();

   /**
    * @param account An {@link Account} to be used to access the entry.
    * @return The referenced entry.
    */
   T getEntry(Account account);



}
