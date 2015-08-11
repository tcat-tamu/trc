package edu.tamu.tcat.trc.refman.types.zotero;

import java.util.List;

import edu.tamu.tcat.trc.refman.types.CreatorRole;
import edu.tamu.tcat.trc.refman.types.ItemFieldType;
import edu.tamu.tcat.trc.refman.types.ItemType;

public class ItemTypeImpl implements ItemType 
{

	private final String id;
	private final String label;
	private final String description;
	private final List<ItemFieldType> fieldTypes;
	private final List<CreatorRole> creatorRoles;
	
	public ItemTypeImpl(String id, String label, String description, List<ItemFieldType> fieldTypes, List<CreatorRole> creatorRoles)
	{
		this.id = id;
		this.label = label;
		this.description = description;
		this.fieldTypes = fieldTypes;
		this.creatorRoles = creatorRoles;
	}
	
	@Override
	public String getId() 
	{
		return id;
	}

	@Override
	public String getLabel() 
	{
		return label;
	}

	@Override
	public String getDescription() 
	{
		return description;
	}

	@Override
	public List<ItemFieldType> getFields() 
	{
		return fieldTypes;
	}

	@Override
	public List<CreatorRole> getCreatorRoles() 
	{
		return creatorRoles;
	}

}
