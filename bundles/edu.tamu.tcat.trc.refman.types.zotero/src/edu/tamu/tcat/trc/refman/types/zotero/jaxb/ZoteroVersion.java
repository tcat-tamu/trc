package edu.tamu.tcat.trc.refman.types.zotero.jaxb;

import javax.xml.bind.annotation.XmlAttribute;

public class ZoteroVersion
{
    private String value;

    @XmlAttribute
    public String getValue ()
    {
        return value;
    }

    public void setValue (String value)
    {
        this.value = value;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [value = "+value+"]";
    }
}
