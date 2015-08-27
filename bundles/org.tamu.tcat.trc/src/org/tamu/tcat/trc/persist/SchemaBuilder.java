package org.tamu.tcat.trc.persist;

/**
 *  Used to construct {@link RepositorySchema} instances. Note that implementations are not
 *  guaranteed to be thread safe. Use of a {@codeSchemaBuilder} is expected to be confined to a
 *  single thread.
 */
public interface SchemaBuilder
{

   /**
    *
    * @param name the id of the schema.
    * @return this {@code SchemaBuilder} instance to support method chaining
    */
   SchemaBuilder setId(String id);

   /**
    *
    * @param id
    * @return
    */
   SchemaBuilder setIdField(String id);

   /**
    *
    * @param fieldName the field name to be used to store the data associated with a record.
    * @return this {@code SchemaBuilder} instance to support method chaining
    */
   SchemaBuilder setDataField(String fieldName);

   /**
    * @param fieldName A hint for the field name used to store the date a record was created.
    *       If {@code null}, creation dates may not be stored (depending on the underlying data
    *       storage layer, creation dates may be stored automatically.
    * @return this {@code SchemaBuilder} instance to support method chaining
    */
   SchemaBuilder setCreatedField(String fieldName);

   /**
    * @param fieldName A hint for the field name used to store the date a record was last
    *       modified. If {@code null}, modification dates may not be stored (depending on the
    *       underlying data storage layer, creation dates may be stored automatically.
    * @return this {@code SchemaBuilder} instance to support method chaining
    */
   SchemaBuilder setModifiedField(String fieldName);

   /**
    * @param fieldName A hint for the field name used to indicate that a record has been
    *       deleted. This is used by repositories that mark records as having been deleted
    *       but retain their data for historical and/or data consistency purposes. If
    *       {@code null}, deletion of records will be performed using a repository default
    *       methodology. Repositories should document how they implement deletion in an
    *       implementation note on the {@link DocumentRepository#delete(String)} method.
    * @return this {@code SchemaBuilder} instance to support method chaining
    */
   SchemaBuilder setRemovedField(String fieldName);

   /**
    * Constructs the schema. Builders are indended to be single use. Once built, all setter
    * methods on this instance will throw {@link IllegalStateException} if called.
    *
    * <p>
    * Note that this does not create the schema in some underlying data store, it merely builds
    * the schema definition.
    *
    * @return the built schema
    * @throws IllegalStateException If the schema has already been built of if the supplied
    *       parameterization does not match the constraints for a valid schema definition.
    */
   RepositorySchema build();

}
