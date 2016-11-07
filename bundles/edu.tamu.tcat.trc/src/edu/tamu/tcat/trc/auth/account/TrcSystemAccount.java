package edu.tamu.tcat.trc.auth.account;

import java.util.Objects;
import java.util.UUID;

public class TrcSystemAccount implements TrcAccount
{
   // TODO add documentation
   private String display;
   private String user;
   private UUID uuid;

   public TrcSystemAccount(String display, String user, long id)
   {
      this.display = display;
      this.user = user;
      this.uuid = new UUID(0, id);
   }

   @Override
   public UUID getId()
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public String getDisplayName()
   {
      return display;
   }

   @Override
   public boolean isActive()
   {
      return true;
   }

   @Override
   public String getUsername()
   {
      return user;
   }

   @Override
   public String getTitle()
   {
      return "";
   }

   @Override
   public String getFirstName()
   {
      return "";
   }

   @Override
   public String getLastName()
   {
      return display;
   }

   @Override
   public String getEmailAddress()
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public String getAffiliation()
   {
      return "system";
   }

   @Override
   public boolean equals(Object obj)
   {
      if (obj instanceof TrcSystemAccount)
         return Objects.equals(this.uuid, ((TrcSystemAccount)obj).uuid);

      return false ;
   }

   @Override
   public int hashCode()
   {
      return this.uuid.hashCode();
   }
}
