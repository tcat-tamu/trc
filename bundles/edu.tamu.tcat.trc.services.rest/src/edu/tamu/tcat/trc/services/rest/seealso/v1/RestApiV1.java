package edu.tamu.tcat.trc.services.rest.seealso.v1;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;

public abstract class RestApiV1
{

   public static class SeeAlso
   {
      /** The entry for which related links are to be returned. */
      LinkTarget root;

      /** A map of the related links, grouped by the type of related item. */
      Map<String, SortedSet<LinkTarget>> links = new HashMap<>();
   }

   public static class SeeAlsoLink
   {
      LinkTarget source = new LinkTarget();
      LinkTarget target = new LinkTarget();
   }

   public static class LinkTarget
   {
      public String label;
      public String id;
      public String type;
      public String token;
   }
}

