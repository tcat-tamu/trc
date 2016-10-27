package edu.tamu.tcat.trc.services.rest.notes.v1;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import edu.tamu.tcat.account.Account;
import edu.tamu.tcat.trc.resolver.EntryId;
import edu.tamu.tcat.trc.resolver.EntryResolverRegistry;
import edu.tamu.tcat.trc.services.notes.Note;

public abstract class ModelAdapter
{
   public static RestApiV1.Note adapt(EntryResolverRegistry reg, Note note)
   {
      RestApiV1.Note dto = new RestApiV1.Note();

      dto.id = note.getId();
      dto.content = note.getContent();
      dto.mimeType = note.getMimeType();

      EntryId ref = note.getAssociatedEntry();
      dto.entryRef = (ref != null) ?  reg.tokenize(ref) : null;

      dto.dateCreated =  formatDate(note.getDateCreated());
      dto.dateModified =  formatDate(note.getDateModified());

      Account account = note.getAuthor();
      dto.authorId = account != null ? account.getId() : null;

      return dto;
   }

   /**
    * ISO 8601 formatting of dates that passes null values through
    */
   private static String formatDate(ZonedDateTime dateCreated)
   {
      return (dateCreated != null)
            ? DateTimeFormatter.ISO_ZONED_DATE_TIME.format(dateCreated)
            : null;
   }
}
