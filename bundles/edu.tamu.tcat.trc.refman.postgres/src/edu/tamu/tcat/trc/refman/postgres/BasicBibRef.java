package edu.tamu.tcat.trc.refman.postgres;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import edu.tamu.tcat.trc.refman.BibliographicReference;
import edu.tamu.tcat.trc.refman.types.ItemFieldType;
import edu.tamu.tcat.trc.refman.types.ItemType;

public class BasicBibRef implements BibliographicReference
{
   private final ItemType item;
   private final URI collectionId;
   private final Map<String, String> values;
   private final URI id;
   private List<CreatorValue> cVals;

   public BasicBibRef(URI id, URI collectionId, ItemType item, Map<String, String> valueSet, List<CreatorValue> creatorValues)
   {
      this.id = id;
      this.collectionId = collectionId;
      this.item = item;
      this.cVals = new ArrayList<>(creatorValues);
      this.values = new HashMap<>(valueSet);
   }

   public BasicBibRef()
   {
      this.id = null;
      this.collectionId = null;
      this.item = null;
      this.cVals = new ArrayList<>();
      this.values = new HashMap<>();
   }

   @Override
   public URI getId()
   {
      return id;
   }

   public URI getCollectionId()
   {
      return collectionId;
   }

   @Override
   public ItemType getType()
   {
      return item;
   }

   @Override
   public List<CreatorValue> getCreators()
   {
      return cVals.stream().map(cv -> new BasicCreatorValue(cv.getGivenName(),
                                                            cv.getFamilyName(),
                                                            cv.getRoleId(),
                                                            cv.getAuthId())).collect(Collectors.toList());
   }

   @Override
   public String getValue(ItemFieldType field) throws IllegalArgumentException
   {
      return values.get(field.getId());
   }

   @Override
   public Set<FieldValue> getValues()
   {
      return values.keySet().stream()
            .map(k -> new BasicFieldValue(k, values.get(k)))
            .collect(Collectors.toSet());
   }

   private class BasicFieldValue implements FieldValue
   {
      private final String field;
      private final String value;

      public BasicFieldValue(String field, String value)
      {
         this.field = field;
         this.value = value;
      }

      @Override
      public ItemFieldType getFieldType()
      {
         return item.getField(field);
      }

      @Override
      public String getValue()
      {
         return this.value;
      }

   }

   public class BasicCreatorValue implements CreatorValue
   {
      private final String roleId;
      private final String firstName;
      private final String lastName;
      private final String authoritiveId;

      public BasicCreatorValue(String fName, String lName, String role, String authoritiveId)
      {
         this.firstName = fName;
         this.lastName = lName;
         this.roleId = role;
         this.authoritiveId = authoritiveId;
      }

      @Override
      public String getRoleId()
      {
         return roleId;
      }

      @Override
      public String getName()
      {
         return this.lastName;
      }

      @Override
      public String getFamilyName()
      {
         return this.lastName;
      }

      @Override
      public String getGivenName()
      {
         return this.firstName;
      }

      @Override
      public String getAuthId()
      {
         return this.authoritiveId;
      }
   }

}
