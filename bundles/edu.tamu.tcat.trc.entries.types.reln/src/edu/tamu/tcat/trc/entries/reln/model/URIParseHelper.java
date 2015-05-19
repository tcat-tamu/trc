package edu.tamu.tcat.trc.entries.reln.model;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *  Support for parsing string-valued URIs
 */
public class URIParseHelper
{

   /**
    * Parses the supplied string-valued URIs to return types. Checks for syntactic validity
    * of the supplied data and throws an exception that reports all malformed URIs.
    *
    * @param data
    * @return A collection of the provided URIs
    * @throws IllegalArgumentException If one or more of the supplied URIs were not properly formatted.
    */
   public static Collection<URI> parse(Collection<String> data)
   {
      // TODO need a better exception that will support future error format reporting
      Map<String, URISyntaxException> errors = new HashMap<>();
      Set<URI> uris = new HashSet<>();
      for (String uri: data)
      {
         try
         {
            uris.add(new URI(uri));
         }
         catch (URISyntaxException ex)
         {
            errors.put(uri, ex);
         }
      }

      handleErrors(errors);
      return uris;
   }

   public static Set<String> toStringSet(Collection<URI> uris)
   {
      Set<String> result = new HashSet<String>();
      for (URI uri : uris)
      {
         result.add(uri.toASCIIString());
      }

      return result;
   }

   private static void handleErrors(Map<String, URISyntaxException> errors)
   {
      if (!errors.isEmpty())
      {
         StringBuilder sb = new StringBuilder();
         sb.append("Failed to instantiate relationship anchor. Invalid URIs for one or more of the referenced entities. Problem values are:");
         for (String uri : errors.keySet())
         {
            sb.append("\n\t").append(uri);
         }
         IllegalArgumentException syntaxErrors = new IllegalArgumentException(sb.toString());
         for (URISyntaxException ex: errors.values())
         {
            syntaxErrors.addSuppressed(ex);
         }

         throw syntaxErrors;
      }
   }

   /**
    *  Validate that the supplied values are valid URIs. If any of the supplied URIs are
    *  not valid, this will throw the same runtime exception as is thrown by {@link #parse(Collection)}.
    *
    *  @param uris The uri's to validate.
    */
   public static void validate(Collection<String> uris)
   {
      // HACK: simply delegate to parse
      parse(uris);
   }
}
