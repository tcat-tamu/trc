package edu.tamu.tcat.trc.entries.types.bib.rest.v1;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * An encapsulation of the data vehicle types used to process JSON requests and responses
 * for version 1 of the TRC REST API for Bibliographic entries.
 */
public class RestApiV1
{
   public static class WorkSearchResult
   {
      public String id;
      public String uri;
      public List<AuthorRef> authors = new ArrayList<>();
      public String title;
      public String label;
      public String summary;
      public String pubYear = null;
   }
   
   public static class AuthorRef
   {
      public String authorId;
      public String name;
      public String firstName;
      public String lastName;
      public String role;
   }
   
   public static class WorkId
   {
      public String id;
   }
   
   public static class EditionId
   {
      public String id;
   }
   
   public static class VolumeId
   {
      public String id;
   }
   
   public static class Work
   {
      public String id;
      public List<AuthorRef> authors;
      public Collection<Title> titles;
      public List<AuthorRef> otherAuthors;
      public String series;
      public String summary;

      public Collection<Edition> editions;
   }
   
   public static class Title
   {
      // short, default, undefined.
      public String type;
      // language
      public String lg;
      public String title;
      public String subtitle;
   }
   
   public static class Edition
   {
      public String id;
      public String editionName;
      public PublicationInfo publicationInfo;
      public List<AuthorRef> authors;
      public Collection<Title> titles;
      public List<AuthorRef> otherAuthors;
      public String summary;
      public String series;
      public List<Volume> volumes;
   }
   
   public static class Volume
   {
      public String id;
      public String volumeNumber;
      public PublicationInfo publicationInfo;
      public List<AuthorRef> authors;
      public Collection<Title> titles;
      public List<AuthorRef> otherAuthors;
      public String summary;
      public String series;
   }
   
   public static class PublicationInfo
   {
      public String publisher;
      public String place;
      public DateDescription date;
   }
   
   public static class DateDescription
   {
      /** ISO 8601 local (YYYY-MM-DD) representation of this date. */
      public String calendar;

      /** A human readable description of this date. */
      public String description;     // NOTE use this to capture intended degree of precision
   }
}
