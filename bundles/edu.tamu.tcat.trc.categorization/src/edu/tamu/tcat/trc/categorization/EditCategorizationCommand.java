package edu.tamu.tcat.trc.categorization;

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

  CompletableFuture<String> execute();
}