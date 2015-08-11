package edu.tamu.tcat.trc.refman.types.zotero;

import edu.tamu.tcat.trc.refman.types.ItemFieldType;

public class ItemFieldTypeImpl implements ItemFieldType
{
	private final String id;
	private final String label;
	private final String type;
	private final String fieldBase;
	private final String description;

	public ItemFieldTypeImpl(String id, String label, String type, String fieldBase, String description)
	{
		this.id = id;
		this.label = label;
		this.type = type;
		this.fieldBase = fieldBase;
		this.description = description;
	}

	public ItemFieldTypeImpl(String id, String label)
	{
		this.id = id;
		this.label = label;
		this.type = "";
		this.fieldBase = "";
		this.description = "";
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
