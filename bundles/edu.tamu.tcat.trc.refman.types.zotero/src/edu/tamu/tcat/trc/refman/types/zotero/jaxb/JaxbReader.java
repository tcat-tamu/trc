package edu.tamu.tcat.trc.refman.types.zotero.jaxb;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

public class JaxbReader {

	public static void main(String[] args) 
	{
		File file = new File( "files\\typeMap.xml");
		JAXBContext jaxbContext;
		try 
		{
			jaxbContext = JAXBContext.newInstance( ZoteroMap.class );
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			ZoteroMap zotero = (ZoteroMap)jaxbUnmarshaller.unmarshal( file );
			System.out.println(zotero);
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
