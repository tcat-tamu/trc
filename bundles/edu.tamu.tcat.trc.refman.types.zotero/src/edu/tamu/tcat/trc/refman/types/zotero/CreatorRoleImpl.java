package edu.tamu.tcat.trc.refman.types.zotero;

import edu.tamu.tcat.trc.refman.types.CreatorRole;

public class CreatorRoleImpl implements CreatorRole 
{
	private final String id;
	private final String label;
	
	public CreatorRoleImpl(String id, String label)
	{
		this.id = id;
		this.label = label;
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

}
