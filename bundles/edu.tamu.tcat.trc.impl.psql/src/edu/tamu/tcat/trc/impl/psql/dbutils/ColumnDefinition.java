package edu.tamu.tcat.trc.impl.psql.dbutils;

import static java.text.MessageFormat.format;

import java.util.HashSet;
import java.util.Set;

import edu.tamu.tcat.trc.impl.psql.dbutils.DbTableManager.ColumnMeta;

public class ColumnDefinition
{
   public enum ColumnType
   {
      // TODO I have no idea if these regex values work

      // Numeric Types
      // https://www.postgresql.org/docs/9.5/static/datatype-numeric.html
      smallint("smallint", "^smallint", "signed two-byte integer"),
      integer("integer", "^integer", "signed four-byte integer"),
      bigint("bigint", "^bigint", "signed eight-byte integer"),
      // decimal
      // numeric [ (p, s) ]   decimal [ (p, s) ]   exact numeric of selectable precision
      real("real", "^real", "single precision floating-point number (4 bytes)"),
      double_precision("double precision", "^double", "double precision floating-point number (8 bytes)"),
      smallserial("smallserial", "^smallserial", "small autoincrementing two-byte integer"),
      serial("serial", "^serial", "autoincrementing four-byte integer"),
      bigserial("bigserial", "^bigserial", "autoincrementing eight-byte integer"),

      // Monetary Types
      // https://www.postgresql.org/docs/9.5/static/datatype-money.html
      money("money", "^money", "currency amount"),

      // Character Types
      // https://www.postgresql.org/docs/9.5/static/datatype-character.html
      varchar("character varying", "^char.+", "variable-length character string", true),
      character("character", "^char.+", "fixed-length character string", true),
      text("text", "^text", "variable-length character string"),

      // Date/Time Types
      // https://www.postgresql.org/docs/9.5/static/datatype-datetime.html
      // TODO add support for precision
      timestamp("timestamp", "timestamp without time zone", "both date and time (no time zone)"),
      timestamp_tz("timestamp with time zone", "timestamp with time zone", "both date and time, with time zone"),
      time("time", "^time", "time of day (no time zone)"),
      time_tz("time with time zone", "^time with time zone", "time of day, including time zone"),
      date("date", "^date", "calendar date (year, month, day)"),
//         interval [ fields ] [ (p) ]      time span

      // Boolean Type
      // https://www.postgresql.org/docs/9.5/static/datatype-boolean.html
      bool("boolean", "^bool.*", "logical Boolean (true/false)"),

      // Enumerated Types
      // https://www.postgresql.org/docs/9.5/static/datatype-enum.html
      // TODO add support

      // Geometric Types
      // https://www.postgresql.org/docs/9.5/static/datatype-geometric.html
      point("point", "^point", "geometric point on a plane"),
      line("line", "^line", "infinite line on a plane"),
      lseg("lseg", "^lseg", "line segment on a plane"),
      box("box", "^box", "rectangular box on a plane"),
      path("path", "^path", "geometric path on a plane"),
      polygon("polygon", "^polygon", "closed geometric path on a plane"),
      circle("circle", "^circle", "circle on a plane"),

      // Network Address Types
      // https://www.postgresql.org/docs/9.5/static/datatype-net-types.html
      cidr("cidr", "^cidr", "IPv4 or IPv6 network address"),
      inet("inet", "^inet", "IPv4 or IPv6 host address"),
      macaddr("macaddr", "^macaddr", "MAC (Media Access Control) address"),

      // Binary Data
      // https://www.postgresql.org/docs/9.5/static/datatype-binary.html
      bytea("bytea", "^bytea", "binary data (\"byte array\")"),

      // Bit String Types
      // https://www.postgresql.org/docs/9.5/static/datatype-bit.html
      bit("bit", "^bit", "fixed-length bit string", true),
      varbit("bit varying", "^bit.+", "variable-length bit string", true),

      // Text Search Types
      // https://www.postgresql.org/docs/9.5/static/datatype-textsearch.html
//      tsvector("tsvector", "text search document"),
//      tsquery("tsquery", "text search query"),

      // UUID, JSON, XML
      // https://www.postgresql.org/docs/9.5/static/datatype-uuid.html
      // https://www.postgresql.org/docs/9.5/static/datatype-json.html
      // https://www.postgresql.org/docs/9.5/static/datatype-xml.html
      uuid("uuid", "^uuid", "universally unique identifier"),
      json("json", "^json", "textual JSON data"),
      jsonb("jsonb", "^jsonb", "binary JSON data, decomposed"),
      xml("xml", "^xml", "XML data"),
      ;

      public String type;
      public String desc;
      public boolean sized;
      public String regex;

      ColumnType(String type, String regex, String desc, boolean sized)
      {
         this.type = type;
         this.regex = regex;
         this.desc = desc;
         this.sized = sized;
      }

      ColumnType(String type, String regex, String desc)
      {
         this(type, regex, desc, false);
      }
   }

   private String name;
   private ColumnDefinition.ColumnType type;
   private Integer size = null;
   private boolean allowNull = true;
   private boolean unique = false;
   private String defaultValue = null;

   private ColumnDefinition()
   {

   }

   public String getName()
   {
      return name;
   }

   public ColumnDefinition.ColumnType getType()
   {
      return type;
   }

   public Integer getSize()
   {
      return size;
   }

   public boolean allowNull()
   {
      return allowNull;
   }

   public boolean isUnique()
   {
      return unique;
   }

   public String getDefault()
   {
      return defaultValue;
   }

   public ColumnDefinition(ColumnDefinition defn)
   {
      name = defn.name;
      type = defn.type;
      size = defn.size;
      allowNull = defn.allowNull;
      unique = defn.unique;
      defaultValue = defn.defaultValue;
   }

   public String getCreateSql()
   {
      StringBuilder sb = new StringBuilder();
      sb.append(name).append(" ").append(type.type.toUpperCase());

      if (type.sized && size != null)
         sb.append("(").append(size.intValue()).append(")");

      if (!allowNull)
         sb.append(" ").append("NOT NULL");

      if (defaultValue != null)
         sb.append(" DEFAULT ").append(defaultValue);

      // sb.append(MessageFormat.format(",  {0} TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP", createdField));
      return sb.toString();
   }

   public boolean matches(ColumnMeta meta)
   {
      return false;
   }

   public Set<String> checkValidity()
   {
      // HACK this is overly simplistic. Should use better regex to test name validity etc,
      Set<String> errors = new HashSet<>();
      if (name == null || name.trim().isEmpty())
         errors.add("No column name supplied");

      if (!name.matches("^[\\p{L}_][\\p{L}\\p{N}@$#_]{0,127}$"))
         errors.add(format("The column name \"{0}\" is not a valid SQL identifier"));

      if (this.type == null)
         errors.add("No type is defined");

      // TODO test size/precision validity, test default value validity, test valid name REGEX
      return errors;
   }

   public static class Builder
   {
      private ColumnDefinition defn = new ColumnDefinition();

      public ColumnDefinition.Builder setName(String name)
      {
         defn.name = name;
         return this;
      }

      public ColumnDefinition.Builder setType(ColumnDefinition.ColumnType type)
      {
         defn.type = type;
         return this;
      }

      public ColumnDefinition.Builder setSize(int sz) {
         if (defn.type == null || !defn.type.sized)
            throw new IllegalArgumentException(format("This column type {0} does not accept a size parameter.", defn.type));

         defn.size = Integer.valueOf(sz);
         return this;
      }

      public ColumnDefinition.Builder unique()
      {
         defn.unique = true;
         return this;
      }

      public ColumnDefinition.Builder notNull()
      {
         defn.allowNull = false;
         return this;
      }

      /**
       * Sets the default value for this column. Note that, if this value must be
       * quoted in SQL statements, the value supplied here must be enclosed in
       * quotation marks.
       *
       * @param value The default value for this column
       * @return A reference to this builder.
       */
      public ColumnDefinition.Builder setDefault(String value)
      {
         defn.defaultValue = value;
         return this;
      }

      public Set<String> checkValidity()
      {
         return defn.checkValidity();
      }

      /**
       *
       * @return
       * @throws IllegalStateException
       */
      public ColumnDefinition build() throws IllegalStateException
      {
         Set<String> errors = defn.checkValidity();
         if (!errors.isEmpty())
            throw new IllegalStateException("Invalid column definition:\n\t" + String.join("\n\t", errors));

         return defn;
      }
   }
}