package edu.tamu.tcat.trc.repo;

import java.text.MessageFormat;
import java.util.Objects;

public class BasicSchemaBuilder implements SchemaBuilder
{

   /* SQL identifiers and key words must begin with a letter (a-z, but also letters with
    * diacritical marks and non-Latin letters) or an underscore (_). Subsequent characters in
    * an identifier or key word can be letters, underscores, digits (0-9), or dollar signs ($).
    * Note that dollar signs are not allowed in identifiers according to the letter of the SQL
    * standard, so their use might render applications less portable.
    *
    * See http://www.postgresql.org/docs/current/static/sql-syntax-lexical.html#SQL-SYNTAX-IDENTIFIERS
    */
   private static final String VALID_NAME_REGEX = "^[a-zA-Z_][a-zA-Z0-9_]*$";

   private static class SchemaImpl implements RepositorySchema
   {
      String id;
      String idField = "id";
      String dataField = "record_value";
      String removedField = "removed";
      String createdField = "date_created";
      String modifiedField = "last_modified";

      @Override
      public String getId()
      {
         return id;
      }

      @Override
      public String getIdField()
      {
         return idField;
      }

      @Override
      public String getDataField()
      {
         return dataField;
      }

      @Override
      public String getRemovedField()
      {
         return removedField;
      }

      @Override
      public String getModifiedField()
      {
         return modifiedField;
      }

      @Override
      public String getCreatedField()
      {
         return createdField;
      }
   }

   private volatile boolean built = false;
   private final SchemaImpl schema = new SchemaImpl();

   public BasicSchemaBuilder()
   {
      // TODO Auto-generated constructor stub
   }

   private String checkValue(String value, String fieldName, boolean required)
   {
      if (built)
         throw new IllegalStateException("This schema has already been built.");

      if (required && (value == null || value.isEmpty()))
         throw new IllegalArgumentException(
               MessageFormat.format("A value is required for the {0}", fieldName));

      if (value.isEmpty())
         value = null;

      if (value != null && !value.matches(VALID_NAME_REGEX))
         throw new IllegalArgumentException(
               MessageFormat.format("The supplied value {0} is not a valid field identifier", value));

      return value;
   }

   @Override
   public SchemaBuilder setId(String id)
   {
      schema.id = checkValue(id, "schema id", true);
      return this;
   }

   @Override
   public SchemaBuilder setIdField(String id)
   {
      schema.idField = checkValue(id, "id field", true);
      return this;
   }

   @Override
   public SchemaBuilder setDataField(String colName)
   {
      schema.dataField = checkValue(colName, "data field", false);
      return this;
   }

   @Override
   public SchemaBuilder setRemovedField(String colName)
   {
      schema.removedField = checkValue(colName, "removed field", false);
      return null;
   }

   @Override
   public SchemaBuilder setCreatedField(String colName)
   {
      schema.createdField = checkValue(colName, "created field", false);
      return this;
   }

   @Override
   public SchemaBuilder setModifiedField(String colName)
   {
      schema.modifiedField = checkValue(colName, "modified field", false);
      return this;
   }

   @Override
   public RepositorySchema build()
   {
      built = true;

      Objects.requireNonNull(schema.id, "Invalid schema configuration. No schema id provided.");
      Objects.requireNonNull(schema.idField, "Invalid schema configuration. No id field defined.");
      Objects.requireNonNull(schema.dataField, "Invalid schema configuration. No data field defined.");

      return schema;
   }
}
