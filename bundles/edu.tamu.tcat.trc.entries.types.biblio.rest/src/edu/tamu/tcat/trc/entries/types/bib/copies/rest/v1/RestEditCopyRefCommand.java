package edu.tamu.tcat.trc.entries.types.bib.copies.rest.v1;

import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import edu.tamu.tcat.db.exec.sql.SqlExecutor;
import edu.tamu.tcat.trc.entries.notification.EntryUpdateHelper;
import edu.tamu.tcat.trc.entries.types.bib.copies.postgres.PsqlDigitalCopyLinkRepo.UpdateEventFactory;
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
