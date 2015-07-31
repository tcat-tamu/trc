package edu.tamu.tcat.trc.refman.types.zotero;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import edu.tamu.tcat.osgi.config.ConfigurationProperties;
import edu.tamu.tcat.trc.refman.types.ItemFieldType;
import edu.tamu.tcat.trc.refman.types.ItemType;
import edu.tamu.tcat.trc.refman.types.ItemTypeProvider;
import edu.tamu.tcat.trc.refman.types.zotero.jaxb.ZoteroMap;

public class ZoteroTypeProvider implements ItemTypeProvider
{

	public static final String ZOTERO_MXL = "edu.tamu.tcat.trc.refman.types.zotero.xml";
	private ConfigurationProperties config;
	private ZoteroTypeAdapter zoteroAdapter;

	public void setConfiguration(ConfigurationProperties cp)
	{
		this.config = cp;
	}
	
	public void activate()
	{
		try
		{
			Path xmlPath = Paths.get(config.getPropertyValue(ZOTERO_MXL, String.class));
			if (!xmlPath.toFile().exists())
				throw new IllegalStateException("Unable to find the file provided:" + xmlPath.toString());
			
			JAXBContext jaxbContext = JAXBContext.newInstance( ZoteroMap.class );
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			zoteroAdapter = new ZoteroTypeAdapter((ZoteroMap)jaxbUnmarshaller.unmarshal(xmlPath.toFile()));
		} 
		catch (JAXBException e) 
		{
			throw new IllegalStateException("An error occurred while attempting to unmarshall the xml file to the ZoteroMap.class.\n" + e);
		}
	}
	
	public void deactivate()
	{
	}

	@Override
	public Collection<ItemFieldType> listDefinedFields() 
	{
		return zoteroAdapter.getDefinedFields();
	}

	@Override
	public Collection<ItemType> listDefinedTypes() 
	{
		return zoteroAdapter.getDefinedTypes();
	}

	@Override
	public ItemType getItemType(String typeId) throws IllegalArgumentException 
	{
		return zoteroAdapter.getType(typeId);
	}


}
