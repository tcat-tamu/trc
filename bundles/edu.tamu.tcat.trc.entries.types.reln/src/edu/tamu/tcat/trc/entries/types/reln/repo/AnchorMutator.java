package edu.tamu.tcat.trc.entries.types.reln.repo;

/**
 *  Edit properties associated with a relationship anchor.
 */
public interface AnchorMutator
{
   /**
    * @param label The display label for this mutator.
    */
   void setLabel(String label);

   /**
    * @param key A key that identifies the property to set.
    * @param value The value to set. May not be <code>null</code>.
    */
   void addProperty(String key, String value);

   /**
    * Clears the value associated with a given property.
    * @param key A key that identifies the property to clear.
    */
   void clearProperty(String key);

}
