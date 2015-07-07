package edu.tamu.tcat.trc.entries.types.bib.copies.rest.v1;

import java.net.URI;
import java.util.UUID;

/**
 * An encapsulation of the data vehicle types used to process JSON requests and responses
 * for version 1 of the TRC REST API for Bibliographic entries.
 */
public class RestApiV1
{
   public static class CopyReference
   {
      public UUID id;
      public URI associatedEntry;
      public String copyId;
      public String title;
      public String summary;
      public String rights;
   }
}
