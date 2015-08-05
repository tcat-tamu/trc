package edu.tamu.tcat.trc.refman.types.zotero;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import edu.tamu.tcat.trc.refman.types.ItemFieldType;
import edu.tamu.tcat.trc.refman.types.ItemType;
import edu.tamu.tcat.trc.refman.types.zotero.jaxb.Field;
import edu.tamu.tcat.trc.refman.types.zotero.jaxb.Map;
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

	   // FIXME: Missing fields such as dictionary title that map to CSL fields via 'base' attr
	   // FIXME: need to get type

		Collection<ItemFieldType> itemFieldTypes = new HashSet<>();
		// FIXME -- not a java.util.map. This is an XML element that maps one Zotero field (or a family of fields)
		//          to their coorresponding CSL field
		Map[] mapArray = zMap.getCslFieldMap().getMap();
		java.util.Map<String, Var> cslFields = getCSLFields(zMap);

		Var[] varArray = zMap.getCslVars().getVars().getVar();
		for (Map map : mapArray)
		{
			String id = map.getZField();
			String label = map.getZField();     // TODO not defined here. . . labels are provided by the type defns.

			Var cslField = cslFields.get(map.getCslField());

			// TODO may need to convert CSL defined field types into our own internal representations



			// FIXME: there is a problem with the API here.
			ItemFieldTypeImpl field = new ItemFieldTypeImpl(id, label, cslField.getType(), "", cslField.getDescription());
         itemFieldTypes.add(field);
		}

		return itemFieldTypes;
	}

	/**
	 *
	 * @param zMap
	 * @return A map of defined CSL fields (a {@code var} element in the
	 *      {@code typeMap.xml} data definition file), keyed by the CSL field name.
	 */
	public static java.util.Map<String, Var> getCSLFields(ZoteroMap zMap)
	{
	   java.util.Map<String, Var> cslFieldMap = new HashMap<>();
	   Var[] definedCslFields = zMap.getCslVars().getVars().getVar();
	   for (Var v : definedCslFields)
	   {
	      cslFieldMap.put(v.getName(), v);
	   }

	   return cslFieldMap;

	}

   private static String getFieldDescription(Var[] varArray, Map map)
   {
      String description = "";
      for (Var var : varArray)
      {
      	String name = var.getName();
      	if (map.getCslField().equals(name))
      		return var.getDescription();
      }
      return description;
   }

	public static Collection<ItemType> getDefinedTypes(ZoteroMap zMap)
	{
		Collection<ItemType> itemTypes = new HashSet<>();
		Var[] varArray = zMap.getCslVars().getVars().getVar();
		TypeMap[] typeMaps = zMap.getZTypes().getTypeMap();

		for(TypeMap typeMap : typeMaps)
		{
			String typeId = typeMap.getZType();
			String typeLabel = typeMap.getZType();

			List<ItemFieldType> fieldTypes = new ArrayList<>();

			Field[] fields = typeMap.getField();
			if (fields == null)
			   continue;      // some types have no defined fields.

			for (Field field : fields)
			{
			   String id = field.getValue();
			   String label = field.getLabel();
			   String description = "";
			   for(Var var : varArray)
			   {
			      String name = var.getName();
			      if(field.getValue().equals(name))
			      {
			         description = var.getDescription();
			         continue;
			      }
			   }
			   fieldTypes.add(new ItemFieldTypeImpl(id, label, "", "", description));
			}

			itemTypes.add(new ItemTypeImpl(typeId, typeLabel, "", fieldTypes));
		}

		return itemTypes;
	}

}
