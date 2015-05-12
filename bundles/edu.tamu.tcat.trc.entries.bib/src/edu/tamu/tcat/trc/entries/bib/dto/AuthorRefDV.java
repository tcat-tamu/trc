package edu.tamu.tcat.trc.entries.bib.dto;

import edu.tamu.tcat.trc.entries.bib.AuthorReference;

public class AuthorRefDV
{
   public String authorId;
   public String name;
   public String firstName;
   public String lastName;
   public String role;

   public static AuthorRefDV create(AuthorReference author)
   {
      AuthorRefDV dto = new AuthorRefDV();
      dto.authorId = author.getId();
      dto.name = author.getName();
      if (dto.name != null)
         dto.parseLegacyName();

      String fName = author.getFirstName();
      String lName = author.getLastName();

      dto.firstName = ((fName != null) && !fName.trim().isEmpty()) ? fName : dto.firstName;
      dto.lastName = ((lName != null) && !lName.trim().isEmpty()) ? lName : dto.lastName;

      dto.role = author.getRole();

      return dto;
   }

   public static AuthorReference instantiate(AuthorRefDV authorRef)
   {
      AuthorReferenceImpl ref = new AuthorReferenceImpl();
      ref.id = authorRef.authorId;
      ref.name = authorRef.name;
      ref.lastName = authorRef.lastName;
      ref.firstName = authorRef.firstName;
      ref.role = authorRef.role;

      return ref;
   }

   private void parseLegacyName()
   {
      // HACK for legacy entries, try to split out first and last names.
      // TODO remove once data in DB has been converted.
      this.name = this.name.trim();
      int ix = this.name.lastIndexOf(",");
      ix = ix > 0 ? ix : this.name.lastIndexOf(";");
      if (ix > 0)
      {
         this.firstName = name.substring(ix + 1).trim();
         this.lastName = name.substring(0, ix).trim();
      } else {
         ix = this.name.lastIndexOf(" ");
         if (ix > 0)
         {
            this.lastName = name.substring(ix + 1).trim();
            this.firstName = name.substring(0, ix).trim();

         }
      }
   }

   public static class AuthorReferenceImpl implements AuthorReference
   {
      private String id;
      private String name;
      private String firstName;
      private String lastName;
      private String role;

      @Override
      public String getId()
      {
         return id;
      }

      @Override
      public String getName()
      {
         return name;
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
      public String getRole()
      {
         return role;
      }
   }
}
