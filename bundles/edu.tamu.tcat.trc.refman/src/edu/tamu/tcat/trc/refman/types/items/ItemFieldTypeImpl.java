package edu.tamu.tcat.trc.refman.types.items;

import edu.tamu.tcat.trc.refman.types.ItemFieldType;

public class ItemFieldTypeImpl implements ItemFieldType 
{
	private String id;
	private String label;
	private String type;
	private String fieldBase;
	private String description;
	
	public ItemFieldTypeImpl(String id, String label, String type, String fieldBase, String description)
	{
		this.id = id;
		this.label = label;
		this.type = type;
		this.fieldBase = fieldBase;
		this.description = description;
		
	}
	
	@Override
	public String getId() 
	{
		return id;
	}

	@Override
	public String getFieldBase()
	{
		return fieldBase;
	}

	@Override
	public String getType() 
	{
		return type;
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

}
