package edu.tamu.tcat.trc.refman.types.zotero;

import java.io.File;
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
	private ZoteroMap zotero;

	public void setConfiguration(ConfigurationProperties cp)
	{
		this.config = cp;
	}
	
	public void activate()
	{
		try
		{
			Path xmlPath = Paths.get(config.getPropertyValue(ZOTERO_MXL, String.class));
			JAXBContext jaxbContext = JAXBContext.newInstance( ZoteroMap.class );
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			zotero = (ZoteroMap)jaxbUnmarshaller.unmarshal(xmlPath.toFile());
		} 
		catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void deactivate()
	{
	}

	@Override
	public Collection<ItemFieldType> listDefinedFields() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<ItemType> listDefinedTypes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ItemType getItemType(String typeId) throws IllegalArgumentException {
		// TODO Auto-generated method stub
		return null;
	}


}
