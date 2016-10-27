package edu.tamu.tcat.trc.entries.types.reln.repo;

/**
 *  Edit properties associated with a relationship anchor.
 */
public interface AnchorMutator
{
   /**
    * @param key A key that identifies the property to set.
    * @param value The value to set. May not be <code>null</code>.
    */
   void setProperty(String key, String value);

   /**
    * Clears the value associated with a given property.
    * @param key A key that identifies the property to clear.
    */
   void clearProperty(String key);
}
