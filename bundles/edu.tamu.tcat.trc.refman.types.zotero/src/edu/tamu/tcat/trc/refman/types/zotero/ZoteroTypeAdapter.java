package edu.tamu.tcat.trc.refman.types.zotero;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import edu.tamu.tcat.trc.refman.types.ItemFieldType;
import edu.tamu.tcat.trc.refman.types.ItemType;
import edu.tamu.tcat.trc.refman.types.zotero.jaxb.CslFieldtoZFieldMap;
import edu.tamu.tcat.trc.refman.types.zotero.jaxb.Field;
import edu.tamu.tcat.trc.refman.types.zotero.jaxb.Remap;
import edu.tamu.tcat.trc.refman.types.zotero.jaxb.TypeMap;
import edu.tamu.tcat.trc.refman.types.zotero.jaxb.Var;
import edu.tamu.tcat.trc.refman.types.zotero.jaxb.ZoteroMap;

/**
 * Adapts data from the Zotero-CSL map XML (via JAXB) into the item type API defined by RefMan.
 */
public class ZoteroTypeAdapter
{
	private ZoteroTypeAdapter()
	{
	}

	public static Collection<ItemFieldType> getDefinedFields(ZoteroMap zMap)
	{
	   // NOTE: using the CslFieldMap to retrieve all field types. This may lead to
	   //       inconsistencies if the XML mapping file does not map all fields used in types
	   //       to CSL values

		Collection<ItemFieldType> itemFieldTypes = new HashSet<>();
		CslFieldtoZFieldMap[] mapArray = zMap.getCslFieldMap().getMap();
		Map<String, Var> cslFields = getCSLFields(zMap);
		Map<String, Remap> cslRemaps = getRemaps(zMap);

		for (CslFieldtoZFieldMap map : mapArray)
		{
			String id = map.getZField();
			Var cslField = null;
			if(cslRemaps.containsKey(id))
				cslField = cslFields.get(cslRemaps.get(id).getDescKey());
			else
				cslField = cslFields.get(map.getCslField());

			// TODO may need to convert CSL defined field types into our own internal representations
			// FIXME: there is a problem with the API here.
			ItemFieldTypeImpl field = new ItemFieldTypeImpl(id, "", cslField.getType(), "", cslField.getDescription());
            itemFieldTypes.add(field);
		}

		return itemFieldTypes;
	}

	public static Collection<ItemType> getDefinedTypes(ZoteroMap zMap)
	{
		Collection<ItemType> itemTypes = new HashSet<>();
		Map<String, Var> cslFields = getCSLFields(zMap);
		Map<String, Remap> cslRemaps = getRemaps(zMap);
		Map<String, CslFieldtoZFieldMap> csltoZFieldMappings = getCsltoZFieldMappings(zMap);
		
		CslFieldtoZFieldMap[] cslFieldMaps = zMap.getCslFieldMap().getMap();
		
		TypeMap[] typeMaps = zMap.getZTypes().getTypeMap();

		for(TypeMap typeMap : typeMaps)
		{
			String typeId = typeMap.getZType();
			String typeLabel = typeMap.getCslType(); // Note:There is not a label attribute for this element

			List<ItemFieldType> fieldTypes = new ArrayList<>();

			if (typeMap.getField() == null)
				continue;
			Map<String, Field> typeMapFields = getTypeMapFields(typeMap);
			
			// This allows us to map the TypeMap to a CSLFieldMap
			typeMapFields.forEach((typeMapKey, typeMapValue) ->
			{
				if (typeMapKey != null)
				{
					// This allows us to map the CSLFieldMap to a CSLVar
					csltoZFieldMappings.forEach((fieldMapKey, fieldMapValue) ->
					{
						Var cslField = null;
						String zField = fieldMapValue.getZField();
						String cslFM = fieldMapValue.getCslField();
						if(typeMapKey.equals(zField))
						{
							if(cslRemaps.containsValue(cslFM))
								cslField = cslFields.get(cslRemaps.get(cslFM).getDescKey());
							else
								cslField = cslFields.get(cslFM);
							
							fieldTypes.add(new ItemFieldTypeImpl(typeMapValue.getValue(), typeMapValue.getLabel(), 
									       cslField == null ? "" : cslField.getType(), 
										   typeMapValue.getBaseField() == null ? "" : typeMapValue.getBaseField(),
										   cslField == null ? "" : cslField.getDescription()));
						}
					});
				}
			});
			
			itemTypes.add(new ItemTypeImpl(typeId, typeLabel, "", fieldTypes));
		}
		return itemTypes;
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
	private static Map<String, Var> getCSLFields(ZoteroMap zMap)
	{
	   Map<String, Var> cslFieldMap = new HashMap<>();
	   Var[] definedCslFields = zMap.getCslVars().getVars().getVar();
	   for (Var v : definedCslFields)
	   {
	      cslFieldMap.put(v.getName(), v);
	   }

	   return cslFieldMap;

	}
	
	/**
	 * 
	 * @param typeMap
	 * @return A map of defined CSL fields contained within a {@link TypeMap} such as a book, note, aritcle...etc.
	 *         This is also a {@code field} element in the {@code typeMap.xml} data definition file, keyed by 
	 *         the CSL field name.  
	 */
	private static Map<String, Field> getTypeMapFields(TypeMap typeMap)
	{
		   Map<String, Field> typeMapFields = new HashMap<>();
		   Field[] fields = typeMap.getField();
		   
		   if (fields == null)
			   return typeMapFields;
		   
		   for (Field field : fields)
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
