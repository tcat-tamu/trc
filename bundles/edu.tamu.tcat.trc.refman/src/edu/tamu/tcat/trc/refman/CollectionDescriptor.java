package edu.tamu.tcat.trc.refman;

public interface CollectionDescriptor
{
   /**
    * @return A unique identifier for this collection.
    */
   String getId();

   /**
    * @return A display name for this collection.
    */
   String getName();

   /**
    * @return A brief description of this collection.
    */
   String getDescription();
}
