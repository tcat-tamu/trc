package edu.tamu.tcat.trc.entries.types.biblio.impl.model;

import java.util.Objects;

import edu.tamu.tcat.trc.entries.types.biblio.Title;
import edu.tamu.tcat.trc.entries.types.biblio.impl.repo.DataModelV1;

public class BasicTitle implements Title
{
   String title;
   String subTitle;
   String type;
   String language;

   public BasicTitle(DataModelV1.TitleDTO dto)
   {
      this.title = dto.title;
      this.subTitle = dto.subtitle;
      this.type = dto.type;
      this.language = dto.lg;
   }

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
      if (!(obj instanceof Title))
      {
         return false;
      }

      Title t = (Title)obj;

      return Objects.equals(t.getTitle(), title) &&
            Objects.equals(t.getSubTitle(), subTitle) &&
            Objects.equals(t.getType(), type) &&
            Objects.equals(t.getLanguage(), language);
   }

}