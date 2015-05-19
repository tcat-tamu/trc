package edu.tamu.tcat.trc.entries.bib;

import java.util.Locale;
import java.util.Set;

/**
 * The title of a work. In practice, documenting the title of a historical work can be a
 * complex task. The title as it appears on the title page may be exceptionally long (in
 * excess of one hundred words) and multiple representations of the title may be used in
 * different places for different purposes. Consequently, we need to represent different
 * versions of a title for a single work and support certain common use cases.
 *
 * <p>For example, the book <em>Tractatus Theologico-Politicus</em>, is commonly refered to
 * by its shorter designation <em>Tractatus</em> and sometimes using the English translation
 * <em>Theologico-Political Treatise</em>. The full title is rather longer, as represented in
 * this translation: <em>Tractatus theologico-politicus: a critical inquiry into the history,
 * purpose, and authenticity of the Hebrew scriptures; with the right to free thought and free
 * discussion asserted, and shown to be not only consistent but necessarily bound up with
 * true piety and good government.</em>
 *
 */
public interface TitleDefinition
{
   // FIXME this should provide a more generic API.
   //       access by role.

   /**
    * @return The title that should be used as the authoritative title for the associated work.
    */
   Title getCanonicalTitle();

   /**
    * @return A short title to be used when compact representations are most appropriate.
    */
   Title getShortTitle();

   /**
    * @return A set containing all titles defined for this work.
    */
   Set<Title> getAlternateTitles();

   /**
    * For translated titles, returns the title associated with the supplied local. While
    * titles may regularly be represented in multiple languages, this is intended to allow the
    * collection maintainer to supply translations of titles into the language of different
    * users rather than to re
    *
    * @param language
    * @return
    */
   Title getTitle(Locale language);
}
