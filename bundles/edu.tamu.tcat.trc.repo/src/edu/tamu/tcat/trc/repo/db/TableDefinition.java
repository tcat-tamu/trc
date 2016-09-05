package edu.tamu.tcat.trc.repo.db;

import static java.text.MessageFormat.format;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TableDefinition
{
   private String tablename;
   private List<ColumnDefinition> columns = new ArrayList<>();

   public String getName()
   {
      return tablename;
   }

   public List<ColumnDefinition> getColumns()
   {
      return Collections.unmodifiableList(columns);
   }

   public String getCreateSql()
   {
      String columsSql = buildColumnSql();
      String constrainsSql = buildConstraints();
      return format("CREATE TABLE IF NOT EXISTS {0} ({1} {2}\n) ", tablename, columsSql, constrainsSql);
   }

   private String buildColumnSql()
   {
      StringBuilder sb = new StringBuilder();
      boolean first = true;
      for (ColumnDefinition defn : columns)
      {
         sb.append(first ? "\n    " : ",\n    ")
           .append(defn.getCreateSql());
         first = false;
      }

      return sb.toString();
   }

   private String buildConstraints()
   {
      return "";
//      StringBuilder sb = new StringBuilder();
//
//      // TODO create PRIMARY KEY CONSTRAINT
//      sb.append(MessageFormat.format(",  CONSTRAINT {0}_pkey PRIMARY KEY ({1})", tablename, idField));
//
//
//      return sb.toString();
   }

   public static class Builder
   {
      private TableDefinition defn;

      public Builder()
      {
         defn = new TableDefinition();
      }

      public Builder setName(String name)
      {
         defn.tablename = name;
         return this;
      }

      public Builder addColumn(ColumnDefinition col)
      {
         String dupName = "Cannot add column. A column with the name {0} is already defined.";

         String name = col.getName().toLowerCase();
         boolean exists = defn.columns.stream()
                                      .map(c -> c.getName().toLowerCase())
                                      .anyMatch(n -> name.equals(n));
         if (exists)
            throw new IllegalArgumentException(format(dupName, col.getName()));

         defn.columns.add(col);
         return this;
      }

      public TableDefinition build()
      {
         return defn;
      }
   }
}