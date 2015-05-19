package edu.tamu.tcat.trc.entries.bib;

/**
 * Provides a structured representation of a title.
 */
public interface Title
{
   /**
    * @return The main title of a work that can be sub-divided into a title and sub-title. Will
    *    not be {@code null}. If the title is not structured, this will have the same value as
    *    {@link #getFullTitle()}.
    */
   String getTitle();

   /**
    * @return The sub-title of a work that can be sub-divided into a title and sub-title. Will
    *    not be {@code null} but may be empty if no sub-title is defined for this work.
    */
   String getSubTitle();

   /**
    * @return A full representation of the title of this work as it appears on the title-page
    *    of the work or some other canonical representation. This may be inferred from the
    *    structured form (e.g., 'Title: Subtitle') or supplied explicitly.
    */
   String getFullTitle();

   /**
    * @return The type of title: canonical, short, etc...
    */
   String getType();

   /**
    * @return The language that the title is written in.
    */
   String getLanguage();
}
