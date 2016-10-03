package edu.tamu.tcat.trc.services.categorization;

import java.util.concurrent.CompletableFuture;

public interface EditCategorizationCommand
{
   void setKey(String key);

   /**
    * @param label
    */
   void setLabel(String label);

   /**
    * @param description
    */
   void setDescription(String description);

   CategorizationNodeMutator editNode(String nodeId);

   void removeNode(String nodeId, boolean removeRefs);

   CompletableFuture<String> execute();
}
