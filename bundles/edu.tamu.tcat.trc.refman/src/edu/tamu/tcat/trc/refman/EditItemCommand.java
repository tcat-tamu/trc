package edu.tamu.tcat.trc.refman;

import java.net.URI;
import java.util.concurrent.Future;

import edu.tamu.tcat.trc.refman.types.ItemFieldType;
import edu.tamu.tcat.trc.refman.types.ItemType;

public interface EditItemCommand
{

   void setType(ItemType type) throws RefManagerException;

   void setField(ItemFieldType field, String value) throws RefManagerException;

   void setCreator(ItemFieldType field, String value) throws RefManagerException;

   Future<URI> execute();

}
