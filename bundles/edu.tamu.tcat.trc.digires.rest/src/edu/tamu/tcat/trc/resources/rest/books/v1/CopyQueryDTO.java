package edu.tamu.tcat.trc.resources.rest.books.v1;

import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;

import edu.tamu.tcat.trc.resources.books.discovery.ContentQuery;

public class CopyQueryDTO
{
   public String q;
   public String author;
   public String before;
   public String after;
   public int offset;
   public int limit;

   /**
    *
    * @param copyImpl
    * @param formatter Formatter for converting before and after dates.
    * @return
    */
   public static CopyQueryDTO create(ContentQuery copyImpl, DateTimeFormatter formatter)
   {
      CopyQueryDTO dto = new CopyQueryDTO();
      dto.q = copyImpl.getKeyWordQuery();
      dto.author = copyImpl.getAuthorQuery();

      TemporalAccessor start = copyImpl.getDateRangeStart();
      TemporalAccessor end = copyImpl.getDateRangeEnd();

      dto.after = (start == null) ? null : formatter.format(start);
      dto.before = (end == null) ? null : formatter.format(end);
      dto.offset = copyImpl.getOffset();
      dto.limit = copyImpl.getLimit();

      return dto;
   }
}
