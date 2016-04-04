package edu.tamu.tcat.trc.entries.types.biblio.postgres.copies;

import java.util.Map;
import java.util.concurrent.Future;
import java.util.function.Supplier;

import edu.tamu.tcat.trc.entries.types.biblio.dto.copies.CopyReferenceDTO;
import edu.tamu.tcat.trc.entries.types.biblio.repo.copies.EditCopyReferenceCommand;
import edu.tamu.tcat.trc.repo.CommitHook;

public class EditCopyReferenceCommandImpl implements EditCopyReferenceCommand
{
   private final Supplier<CopyReferenceDTO> currentState;
   private final CommitHook<CopyReferenceDTO> hook;
   private final CopyReferenceChangeSet changeSet;

   public EditCopyReferenceCommandImpl(String id, Supplier<CopyReferenceDTO> currentState, CommitHook<CopyReferenceDTO> hook)
   {
      this.currentState = currentState;
      this.hook = hook;
      this.changeSet = new CopyReferenceChangeSet(id);
   }

   @Override
   public void setType(String type)
   {
      changeSet.type = type;
   }

   @Override
   public void setProperties(Map<String, String> properties)
   {
      changeSet.properties = properties;
   }

   @Override
   public void setTitle(String title)
   {
      changeSet.title = title;
   }

   @Override
   public void setSummary(String summary)
   {
      changeSet.summary = summary;
   }

   @Override
   public void setRights(String description)
   {
      changeSet.rights = description;
   }

   @Override
   public Future<String> execute()
   {
      if (currentState != null)
      {
         changeSet.original = currentState.get();
      }

      CopyReferenceDTO data = constructUpdatedData(changeSet.original);

      return hook.submit(data, changeSet);
   }

   /**
    * Builds a new repository data object reflecting the changes in the current change set.
    *
    * @param original the original repository data state.
    * @return data transfer object to replace the original
    */
   private CopyReferenceDTO constructUpdatedData(CopyReferenceDTO original)
   {
      CopyReferenceDTO data = new CopyReferenceDTO();

      data.id = changeSet.id;
      data.type = changeSet.type;
      data.properties = changeSet.properties;
      data.title = changeSet.title;
      data.summary = changeSet.summary;
      data.rights = changeSet.rights;

      return data;
   }
}
