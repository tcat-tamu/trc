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
package edu.tamu.tcat.trc.digires.rest.books.v1;

import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;

import edu.tamu.tcat.trc.digires.books.discovery.ContentQuery;

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
