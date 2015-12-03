package edu.tamu.tcat.trc.refman.types.zotero;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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
   public static final String ZOTERO_PROVIDER_ID = "edu.tamu.tcat.trc.refman.types.providers.zotero";
	public static final String ZOTERO_MXL = "edu.tamu.tcat.trc.refman.types.zotero.xml";

	private ConfigurationProperties config;
   private Map<String, ItemType> definedTypes = new HashMap<>();
   private Collection<ItemFieldType> definedFields;

	public void setConfiguration(ConfigurationProperties cp)
	{
		this.config = cp;
	}

	public void activate()
	{
		try
		{
		   // TODO need to perform in separate thread
			Path xmlPath = Paths.get(config.getPropertyValue(ZOTERO_MXL, String.class));
			if (!xmlPath.toFile().exists())
				throw new IllegalStateException("Unable to find the file provided:" + xmlPath.toString());

			JAXBContext jaxbContext = JAXBContext.newInstance( ZoteroMap.class );
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			ZoteroMap zMap = (ZoteroMap)jaxbUnmarshaller.unmarshal(xmlPath.toFile());

			definedFields = ZoteroTypeAdapter.getDefinedFields(zMap);
			Collection<ItemType> types = ZoteroTypeAdapter.getDefinedTypes(zMap);
			types.forEach(t -> definedTypes.put(t.getId(), t));

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
		return Collections.unmodifiableCollection(definedFields);
	}

	@Override
	public Collection<ItemType> listDefinedTypes()
	{
	   return Collections.unmodifiableCollection(definedTypes.values());
	}

	@Override
	public String getId()
	{
	   // HACK: should allow this to be configured rather than use symbolic constant
	   return ZoteroTypeProvider.ZOTERO_PROVIDER_ID;
	}

	@Override
	public boolean hasType(String typeId)
	{
	   return definedTypes.containsKey(typeId);
	}

	@Override
	public ItemType getItemType(String typeId) throws IllegalArgumentException
	{
	   if (!definedTypes.containsKey(typeId))
	      throw new IllegalArgumentException(MessageFormat.format("No item type defined for id '{0}'", typeId));

	   return definedTypes.get(typeId);
	}


}
