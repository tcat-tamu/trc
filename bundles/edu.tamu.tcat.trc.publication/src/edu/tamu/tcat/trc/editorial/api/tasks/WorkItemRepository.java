package edu.tamu.tcat.trc.editorial.api.tasks;

import java.util.Iterator;
import java.util.Optional;

public interface WorkItemRepository
{

   Iterator<WorkItem> getAllItems();

   Optional<WorkItem> getItem(String id);

   EditWorkItemCommand createItem();

   EditWorkItemCommand editItem(String id);

}
