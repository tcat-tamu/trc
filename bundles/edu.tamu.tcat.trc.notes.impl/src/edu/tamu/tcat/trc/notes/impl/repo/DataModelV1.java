package edu.tamu.tcat.trc.notes.impl.repo;

import java.util.UUID;

public abstract class DataModelV1
{
   public static class Note
   {
      public String id;
      public String dateCreated;
      public String dateModified;
      public String entryRef;
      public UUID authorId;
      public String mimeType;
      public String content;
   }
}
