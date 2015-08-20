package org.tamu.tcat.trc.persist;

/**
 *  Provides a general-purpose definition of a repository that can be used interchangeably
 *  between different types of data storage implementations. The specific structures
 *  used to instantiate the supplied schema depend on the structure of the underlying data
 *  storage mechanism. For example, a data store backed by the filesystem might ignore the
 *  supplied creation and modification descriptors and use the built-in filesystem mechanisms
 *  to record this information.
 *
 *  <p>
 *  Consequently the schema should be understood as a tool that hints at the intended structure
 *  of the underlying data storage mechanisms and, where supported, to allow access with
 *  existing data structures that may have been created (and may be used) externally to the
 *  data store API. Implementations of
 */
public interface RepositorySchema
{
   /**
    * @return The name used to identify this schema in the underlying data store records.
    */
   String getName();

   /**
    * @return The name of the database column (or other persistence layer field) used to store
    *       the source data (typically JSON data) for records. Defaults to '{@code record_value}'.
    *       Will not be {@code null}.
    */
   String getIdField();

   /**
    * @return The name of the database column (or other persistence layer field) used to indicate
    *       whether this record has been removed.
    *       Will not be {@code null}.
    */
   String getRemovedField();

   /**
    * @return The name of the database column (or other persistence layer field) used to store
    *       the source data (typically JSON data) for records. Defaults to '{@code record_value}'.
    *       Will not be {@code null}.
    */
   String getDataField();

   /**
    * @return The name of the database column (or other persistence layer field) used to store
    *       the information about when a data record was last modified. If {@code null}, no
    *       information about record modification date will be stored.
    */
   String getModifiedField();

   /**
    * @return The name of the database column (or other persistence layer field) used to store
    *       the information about when a data record was first created. If {@code null}, no
    *       information about record creation date will be stored.
    */
   String getCreatedField();
}
