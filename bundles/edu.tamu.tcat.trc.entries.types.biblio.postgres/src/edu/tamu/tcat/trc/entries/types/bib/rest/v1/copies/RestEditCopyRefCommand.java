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
package edu.tamu.tcat.trc.entries.types.bib.rest.v1.copies;

import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import edu.tamu.tcat.db.exec.sql.SqlExecutor;
import edu.tamu.tcat.trc.entries.notification.EntryUpdateHelper;
import edu.tamu.tcat.trc.entries.types.bib.postgres.copies.PsqlDigitalCopyLinkRepo.UpdateEventFactory;
import edu.tamu.tcat.trc.entries.types.biblio.copies.CopyReference;
import edu.tamu.tcat.trc.entries.types.biblio.copies.UpdateCanceledException;
import edu.tamu.tcat.trc.entries.types.biblio.copies.dto.BaseEditCopyRefCmd;
import edu.tamu.tcat.trc.entries.types.biblio.copies.dto.CopyRefDTO;
import edu.tamu.tcat.trc.entries.types.biblio.copies.repo.CopyChangeEvent;

@Deprecated // appears to be unused. should be DB layer concern
public class RestEditCopyRefCommand extends BaseEditCopyRefCmd
{

   private final CopyReference original;
   private final AtomicBoolean executed = new AtomicBoolean(false);
   private EntryUpdateHelper<CopyChangeEvent> notifier;

   /**
    * Edit an existing copy.
    *
    * @param sqlExecutor
    * @param notifier
    * @param factory
    * @param dto
    */
   public RestEditCopyRefCommand(SqlExecutor sqlExecutor,
                                 EntryUpdateHelper<CopyChangeEvent> notifier,
                                 UpdateEventFactory factory,
                                 CopyRefDTO dto)
   {
      super(dto);

      this.notifier = notifier;
      this.original = CopyRefDTO.instantiate(dto);
   }



   @Override
   public Future<CopyReference> execute() throws UpdateCanceledException
   {
      throw new UnsupportedOperationException();
   }

}
