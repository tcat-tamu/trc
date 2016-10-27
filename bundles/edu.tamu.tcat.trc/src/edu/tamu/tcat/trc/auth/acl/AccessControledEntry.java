package edu.tamu.tcat.trc.auth.acl;

import java.util.UUID;

import edu.tamu.tcat.trc.auth.account.TrcAccount;
import edu.tamu.tcat.trc.resolver.EntryId;

public interface AccessControledEntry
{
   /**
    * @return The reference for the entry
    */
   EntryId getEntry();

   boolean hasPermission(TrcAccount account, AccessControlPermission permission);

   void grantPermission(TrcAccount grantor, UUID grantee, AccessControlPermission...permissions)
         throws AccessPermissionException;

   void revokePermission(TrcAccount grantor, UUID grantee, AccessControlPermission...permissions)
         throws AccessPermissionException;
}
