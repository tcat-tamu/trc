package edu.tamu.tcat.trc.refman.types.zotero;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import edu.tamu.tcat.trc.refman.types.CreatorRole;
import edu.tamu.tcat.trc.refman.types.ItemFieldType;
import edu.tamu.tcat.trc.refman.types.ItemType;
import edu.tamu.tcat.trc.refman.types.zotero.jaxb.CreatorTypeMap;
import edu.tamu.tcat.trc.refman.types.zotero.jaxb.CslFieldtoZFieldMap;
import edu.tamu.tcat.trc.refman.types.zotero.jaxb.CslVar;
import edu.tamu.tcat.trc.refman.types.zotero.jaxb.Remap;
import edu.tamu.tcat.trc.refman.types.zotero.jaxb.ZoteroCreatorType;
import edu.tamu.tcat.trc.refman.types.zotero.jaxb.ZoteroMap;
import edu.tamu.tcat.trc.refman.types.zotero.jaxb.ZoteroTypeField;
import edu.tamu.tcat.trc.refman.types.zotero.jaxb.ZoteroTypeMap;

/**
 * Adapts data from the Zotero-CSL map XML (via JAXB) into the item type API defined by RefMan.
 */
public class ZoteroTypeAdapter
{
	private ZoteroTypeAdapter()
	{
	}

	static Collection<ItemFieldType> getDefinedFields(ZoteroMap zMap)
	{
	   // NOTE: using the CslFieldMap to retrieve all field types. This may lead to
	   //       inconsistencies if the XML mapping file does not map all fields used in types
	   //       to CSL values

		Collection<ItemFieldType> itemFieldTypes = new HashSet<>();
		CslFieldtoZFieldMap[] mapArray = zMap.getCslFieldMap().getMap();
		Map<String, CslVar> cslFields = getCSLFields(zMap);
		Map<String, Remap> cslRemaps = getRemaps(zMap);

		for (CslFieldtoZFieldMap map : mapArray)
		{
			String id = map.getZField();
			CslVar cslField = (cslRemaps.containsKey(id))
				? cslFields.get(cslRemaps.get(id).getDescKey())
				: cslFields.get(map.getCslField());

			// TODO may need to convert CSL defined field types into our own internal representations
			// FIXME: there is a problem with the API here.
			ItemFieldTypeImpl field = new ItemFieldTypeImpl(id, "", cslField.getType(), "", cslField.getDescription());
			itemFieldTypes.add(field);
		}

		return itemFieldTypes;
	}

	/**
	 * Walks the supplied Zotero map to construct item type instances.
	 */
	private static class ItemTypeParser
	{
	   private final ZoteroMap zMap;

      private final Map<String, CslVar> cslFields;
      private final Map<String, Remap> cslRemaps;
      private final Map<String, CreatorTypeMap> creatorTypeMap;
      private final Map<String, CslFieldtoZFieldMap> csltoZFieldMappings;

      private final ZoteroTypeMap[] typeMaps;

      public ItemTypeParser(ZoteroMap zMap)
      {
         this.zMap = zMap;
         cslFields = getCSLFields(zMap);
         cslRemaps = getRemaps(zMap);
         creatorTypeMap = getCreatorType(zMap);
         csltoZFieldMappings = getCsltoZFieldMappings(zMap);

         typeMaps = zMap.getZTypes().getTypeMap();
      }

      public Collection<ItemType> extractTypes()
      {
         Collection<ItemType> itemTypes = new HashSet<>();

         for(ZoteroTypeMap typeMap : typeMaps)
         {
            String typeId = typeMap.getZType();
            String typeLabel = typeMap.getCslType(); // Note:There is not a label attribute for this element
            List<ItemFieldType> fieldTypes = new ArrayList<>();
            List<CreatorRole> creatorRoles = new ArrayList<>();
            if (typeMap.getField() == null)
               continue;
            Map<String, ZoteroTypeField> typeMapFields = getTypeMapFields(typeMap);

            // This allows us to map the TypeMap to a CSLFieldMap
            typeMapFields.forEach((typeMapKey, typeMapValue) ->
            {
               if (typeMapKey != null)
               {
                  // This allows us to map the CSLFieldMap to a CSLVar
                  csltoZFieldMappings.forEach((fieldMapKey, fieldMapValue) ->
                  {
                     CslVar cslField = null;
                     String zField = fieldMapValue.getZField();
                     String cslFM = fieldMapValue.getCslField();
                     if (typeMapKey.equals(zField))
                     {
                        cslField = (cslRemaps.containsValue(cslFM))
                              ? cslFields.get(cslRemaps.get(cslFM).getDescKey())
                              : cslFields.get(cslFM);

                        ItemFieldTypeImpl fieldTypeImpl = new ItemFieldTypeImpl(
                              typeMapValue.getValue(),
                              typeMapValue.getLabel(),
                              cslField == null ? "" : cslField.getType(),
                              typeMapValue.getBaseField() == null ? "" : typeMapValue.getBaseField(),
                              cslField == null ? "" : cslField.getDescription());

                        fieldTypes.add(fieldTypeImpl);
                     }

                  });
               }
               if (typeMapKey.equals("creator"))
               {
                  Map<String, ZoteroCreatorType> zoteroCreatorTypes = getCreatorTypes(typeMapValue.getCreatorType());
                  zoteroCreatorTypes.forEach((creatorKey, creatorValue) ->
                  {
                     String baseField = creatorValue.getBaseField();
                     CreatorTypeMap ctm = (baseField != null)
                           ? creatorTypeMap.get(baseField)
                           : creatorTypeMap.get(creatorKey);

                     if(ctm != null && cslFields.containsKey(ctm.getCslField()))
                     {
                        // ctmVar - creator type map variables
                        CslVar ctmVar = cslFields.get(ctm.getCslField());
                        creatorRoles.add(new CreatorRoleImpl(ctmVar.getName(), creatorValue.getLabel(), ctmVar.getDescription()));
                     }
                  });
               }
            });

            itemTypes.add(new ItemTypeImpl(typeId, typeLabel, "", fieldTypes, creatorRoles));
         }

         return itemTypes;
      }
	}


	static Collection<ItemType> getDefinedTypes(ZoteroMap zMap)
	{
		ItemTypeParser extractor = new ItemTypeParser(zMap);
		return extractor.extractTypes();
	}

	private static Map<String, ZoteroCreatorType> getCreatorTypes(ZoteroCreatorType[] types)
	{
		Map<String, ZoteroCreatorType> zct = new HashMap<>();

		for(ZoteroCreatorType t : types)
		{
			zct.put(t.getValue(), t);
		}

		return zct;
	}

	private static Map<String, CreatorTypeMap> getCreatorType(ZoteroMap zMap)
	{
		Map<String, CreatorTypeMap> creatorTypes = new HashMap<>();
		CreatorTypeMap[] map = zMap.getCslCreatorMap().getMap();

		for (CreatorTypeMap m : map)
		{
			creatorTypes.put(m.getZField(), m);
		}
		return creatorTypes;
	}

	/**
	 *
	 * @param zMap
	 * @return A map of defined CSL fields (a {@code cslFieldtoZfieldMap} element in the
	 *      {@code typeMap.xml} data definition file), keyed by the CSL field name.
	 */
	private static Map<String, CslFieldtoZFieldMap> getCsltoZFieldMappings(ZoteroMap zMap)
	{

		   Map<String, CslFieldtoZFieldMap> maps = new HashMap<>();
		   CslFieldtoZFieldMap[] cslFieldMaps = zMap.getCslFieldMap().getMap();

		   for (CslFieldtoZFieldMap m : cslFieldMaps)
		   {
			   maps.put(m.getZField(), m);
		   }
		   return maps;
	}

	/**
	 *
	 * @param zMap
	 * @return A map of defined CSL fields (a {@code var} element in the
	 *      {@code typeMap.xml} data definition file), keyed by the CSL field name.
	 */
	private static Map<String, CslVar> getCSLFields(ZoteroMap zMap)
	{
	   Map<String, CslVar> cslFieldMap = new HashMap<>();
	   CslVar[] definedCslFields = zMap.getCslVars().getVars().getVar();
	   for (CslVar v : definedCslFields)
	   {
	      cslFieldMap.put(v.getName(), v);
	   }

	   return cslFieldMap;

	}

	/**
	 *
	 * @param typeMap
	 * @return A map of defined CSL fields contained within a {@link ZoteroTypeMap} such as a book, note, aritcle...etc.
	 *         This is also a {@code field} element in the {@code typeMap.xml} data definition file, keyed by
	 *         the CSL field name.
	 */
	private static Map<String, ZoteroTypeField> getTypeMapFields(ZoteroTypeMap typeMap)
	{
		   Map<String, ZoteroTypeField> typeMapFields = new HashMap<>();
		   ZoteroTypeField[] fields = typeMap.getField();

		   if (fields == null)
			   return typeMapFields;

		   for (ZoteroTypeField field : fields)
		   {
			   typeMapFields.put(field.getValue(), field);
		   }

		   return typeMapFields;
	}

	/**
	 *
	 * @param zMap
	 * @return A map of defined CSL fields that required a remapping due to hyphen's in the name. These can be
	 *         found in the {@code typeMap.xml} data definition file, defined as a {@code remap} field.
	 */
	private static Map<String, Remap> getRemaps(ZoteroMap zMap)
	{
		Map<String, Remap> cslRemaps = new HashMap<>();
		Remap[] remaps = zMap.getCiteprocJStoCSLmap().getRemap();

		for(Remap remap : remaps)
		{
			cslRemaps.put(remap.getCiteprocField(), remap);
		}

		return cslRemaps;
	}
}
