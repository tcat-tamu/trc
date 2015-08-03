package edu.tamu.tcat.trc.refman.types.zotero;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import edu.tamu.tcat.trc.refman.types.ItemFieldType;
import edu.tamu.tcat.trc.refman.types.ItemType;
import edu.tamu.tcat.trc.refman.types.zotero.jaxb.Field;
import edu.tamu.tcat.trc.refman.types.zotero.jaxb.Map;
import edu.tamu.tcat.trc.refman.types.zotero.jaxb.TypeMap;
import edu.tamu.tcat.trc.refman.types.zotero.jaxb.Var;
import edu.tamu.tcat.trc.refman.types.zotero.jaxb.ZoteroMap;

public class ZoteroTypeAdapter 
{
	private static ZoteroMap zMap;

	public ZoteroTypeAdapter(ZoteroMap map)
	{
		ZoteroTypeAdapter.zMap = map;
	}
	
	public static Collection<ItemFieldType> getDefinedFields()
	{
		Collection<ItemFieldType> itemFieldTypes = new HashSet<>();
		Map[] mapArray = zMap.getCslFieldMap().getMap();
		Var[] varArray = zMap.getCslVars().getVars().getVar();
		for(Map map : mapArray)
		{
			String id = map.getZField();
			String label = map.getZField();
			String description = "";
			for(Var var : varArray)
			{
				String name = var.getName();
				if(map.getCslField().equals(name))
				{
					description = var.getDescription();
					continue;
				}
			}
			
			itemFieldTypes.add(new ItemFieldTypeImpl(id, label, "", "", description));
		}
		return itemFieldTypes;
	}
	
	public static Collection<ItemType> getDefinedTypes()
	{
		Collection<ItemType> itemTypes = new HashSet<>();
		Var[] varArray = zMap.getCslVars().getVars().getVar();
		TypeMap[] typeMaps = zMap.getZTypes().getTypeMap();
		
		for(TypeMap typeMap : typeMaps)
		{
			String typeId = typeMap.getZType();
			String typeLabel = typeMap.getZType();
			
			List<ItemFieldType> fieldTypes = new ArrayList<>();
			
			if(typeMap.getField() != null)
			{
				for(Field field : typeMap.getField())
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
			}
			itemTypes.add(new ItemTypeImpl(typeId, typeLabel, "", fieldTypes));
		}
		
		return itemTypes;
	}
	
	ItemType getType(String typeId)
	{
		Collection<ItemType> definedTypes = getDefinedTypes();
		for(ItemType item : definedTypes)
		{
			if(item.getId().equals(typeId))
				return item;
		}
		return null;
	}
}
