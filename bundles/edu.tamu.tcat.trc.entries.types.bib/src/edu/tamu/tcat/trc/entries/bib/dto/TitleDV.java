package edu.tamu.tcat.trc.entries.bib.dto;

import java.util.Objects;

import edu.tamu.tcat.trc.entries.bib.Title;

public class TitleDV
{
   public String type;   // short, default, undefined.
   public String lg;
   public String title;
   public String subtitle;

   public static TitleDV create(Title title)
   {
      TitleDV dto = new TitleDV();
      if (title != null)
      {
         dto.type = title.getType();
         dto.lg = title.getLanguage();
         dto.title = title.getTitle();
         dto.subtitle = title.getSubTitle();
      }
      return dto;
   }

   public static Title instantiate(TitleDV dto)
   {
      TitleImpl result = new TitleImpl();
      result.title = dto.title;
      result.subTitle = dto.subtitle;
      result.type = dto.type;
      result.language = dto.lg;

      return result;
   }

   public static class TitleImpl implements Title
   {
      private String title;
      private String subTitle;
      private String type;
      private String language;

      @Override
      public String getTitle()
      {
         return this.title;
      }

      @Override
      public String getSubTitle()
      {
         return this.subTitle;
      }

      @Override
      public String getFullTitle()
      {
         StringBuilder sb = new StringBuilder();

         sb.append(this.title);

         if (this.subTitle != null && !this.subTitle.trim().isEmpty()) {
            sb.append(": ").append(this.subTitle);
         }

         return sb.toString();
      }

      @Override
      public String getType()
      {
         return this.type;
      }

      @Override
      public String getLanguage()
      {
         return this.language;
      }

      @Override
      public String toString()
      {
         return getFullTitle();
      }

      @Override
      public int hashCode()
      {
         int result = 17;

         result = 37 * result + (title == null ? 0 : title.hashCode());
         result = 37 * result + (subTitle == null ? 0 : subTitle.hashCode());
         result = 37 * result + (type == null ? 0 : type.hashCode());
         result = 37 * result + (language == null ? 0 : language.hashCode());

         return result;
      }

      @Override
      public boolean equals(Object obj)
      {
         if (!(obj instanceof Title)) {
            return false;
         }

         Title t = (Title)obj;

         return Objects.equals(t.getTitle(), title) &&
               Objects.equals(t.getSubTitle(), subTitle) &&
               Objects.equals(t.getType(), type) &&
               Objects.equals(t.getLanguage(), language);
      }

   }

}
