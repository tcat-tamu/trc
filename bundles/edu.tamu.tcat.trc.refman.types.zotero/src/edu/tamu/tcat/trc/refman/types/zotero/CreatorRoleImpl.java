package edu.tamu.tcat.trc.refman.types.zotero;

import edu.tamu.tcat.trc.refman.types.CreatorRole;

public class CreatorRoleImpl implements CreatorRole 
{
	private final String id;
	private final String label;
	private final String description;
	
	public CreatorRoleImpl(String id, String label, String description)
	{
		this.id = id;
		this.label = label;
		this.description = description;
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

}
