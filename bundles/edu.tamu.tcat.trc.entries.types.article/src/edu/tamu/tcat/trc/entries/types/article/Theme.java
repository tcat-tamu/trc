package edu.tamu.tcat.trc.entries.types.article;

import java.util.List;

public interface Theme
{
   String getTitle();
   String getAbstract();
   List<Treatment> getTreatments();
   
   public interface Treatment
   {
      String getType();
      String getURI();
   }
}
