/*
 * Copyright 2015 Texas A&M Engineering Experiment Station
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.tamu.tcat.trc.notes.rest.v1;

import java.util.UUID;

public class RestApiV1
{
   public static final String MIME_TYPE = "mimeType";
   public static final String AUTHOR_ID = "authorId";
   public static final String CONTENT = "content";
   public static final String ENTRY_REF = "entryRef";

   public static class NotesId
   {
      public String id;
   }

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

   public class NotesSearchResult
   {

   }


}
