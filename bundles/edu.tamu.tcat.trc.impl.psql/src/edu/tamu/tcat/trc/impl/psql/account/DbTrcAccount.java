package edu.tamu.tcat.trc.impl.psql.account;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.tamu.tcat.trc.auth.account.TrcAccount;

public class DbTrcAccount implements TrcAccount
{
   private final static Logger logger = Logger.getLogger(DbTrcAccount.class.getName());

   private final UUID uuid;
   private final String displayName;
   private final boolean active;
   private final String username;
   private final String title;
   private final String firstName;
   private final String lastName;
   private final String email;
   private final String affiliation;

   private final Map<String, String> properties;

   public DbTrcAccount(DataModelV1.AccountData dto)
   {
      this.uuid = dto.uuid;
      this.displayName = dto.displayName;
      this.active = dto.active;
      this.username = dto.username;
      this.title = dto.title;
      this.firstName = dto.firstName;
      this.lastName = dto.lastName;
      this.email = dto.email;
      this.affiliation = dto.affiliation;

      if (dto.properties != null)
         this.properties = new HashMap<>(dto.properties);
      else
         this.properties = new HashMap<>();
   }

   @Override
   public UUID getId()
   {
      return uuid;
   }

   @Override
   public String getDisplayName()
   {
      return displayName;
   }

   @Override
   public boolean isActive()
   {
      return active;
   }

   @Override
   public String getUsername()
   {
      return username;
   }

   @Override
   public String getTitle()
   {
      return title;
   }

   @Override
   public String getFirstName()
   {
      return firstName;
   }

   @Override
   public String getLastName()
   {
      return lastName;
   }

   @Override
   public String getEmailAddress()
   {
      return email;
   }

   @Override
   public String getAffiliation()
   {
      return affiliation;
   }

   public <T> T getPropertyValue(String name, Class<T> type, T defaultValue)
   {
      try
      {
         T val = getPropertyValue(name, type);
         if (val == null)
            return defaultValue;
         return val;
      }
      catch (Exception pe)
      {
         logger.log(Level.WARNING, "Failed processing property value for [" + name + "], returning default", pe);
         return defaultValue;
      }
   }

   @SuppressWarnings("unchecked")
   public <T> T getPropertyValue(String name, Class<T> type)
   {
      Objects.requireNonNull(name, "property name is null");
      Objects.requireNonNull(type, "property type is null");

      String str;
      synchronized (this)
      {
         if (properties == null)
            throw new IllegalStateException("Not initialized");

         str = properties.get(name);
      }

      if (str == null)
         return null;
      if (type.isInstance(str))
         return (T)str;

      try
      {
         if (Number.class.isAssignableFrom(type))
         {
            if (Byte.class.isAssignableFrom(type))
               return (T)Byte.valueOf(str);
            if (Short.class.isAssignableFrom(type))
               return (T)Short.valueOf(str);
            if (Integer.class.isAssignableFrom(type))
               return (T)Integer.valueOf(str);
            if (Long.class.isAssignableFrom(type))
               return (T)Long.valueOf(str);
            if (Float.class.isAssignableFrom(type))
               return (T)Float.valueOf(str);
            if (Double.class.isAssignableFrom(type))
               return (T)Double.valueOf(str);

            throw new IllegalStateException("Unhandled numeric type ["+type+"] for property ["+name+"] value ["+str+"]");
         }

         if (Boolean.class.isAssignableFrom(type))
            return (T)Boolean.valueOf(str);
      }
      catch (NumberFormatException e)
      {
         throw new IllegalStateException("Failed converting property ["+name+"] value ["+str+"] to primitive "+type, e);
      }

      try
      {
         if (Path.class.isAssignableFrom(type))
            return (T)Paths.get(str);
      }
      catch (Exception e)
      {
         throw new IllegalStateException("Failed converting property ["+name+"] value ["+str+"] to OS file system path "+type, e);
      }

      try
      {
         if (URI.class.isAssignableFrom(type))
            return (T)new URI(str);
      }
      catch (Exception e)
      {
         throw new IllegalStateException("Failed converting property ["+name+"] value ["+str+"] to URI "+type, e);
      }

      throw new IllegalStateException("Unhandled type: " + type.getCanonicalName());
   }
}
