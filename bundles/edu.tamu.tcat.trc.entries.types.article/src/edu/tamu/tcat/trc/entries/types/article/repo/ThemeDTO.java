package edu.tamu.tcat.trc.entries.types.article.repo;

import java.util.List;

public class ThemeDTO
{
   public String title;
   public String themeAbstract;
   public List<Treatment> treatments;
   
   public class Treatment
   {
      public String type;
      public String uri;
   }
}
