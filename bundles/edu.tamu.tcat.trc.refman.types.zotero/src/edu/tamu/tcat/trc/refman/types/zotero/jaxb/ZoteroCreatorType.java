package edu.tamu.tcat.trc.refman.types.zotero.jaxb;

import javax.xml.bind.annotation.XmlAttribute;

public class ZoteroCreatorType
{
    private String value;
    private String label;
    private String baseField;

    @XmlAttribute
    public String getValue ()
    {
        return value;
    }

    public void setValue (String value)
    {
        this.value = value;
    }

    @XmlAttribute
    public String getLabel ()
    {
        return label;
    }

    public void setLabel (String label)
    {
        this.label = label;
    }

    @XmlAttribute
    public String getBaseField()
    {
        return baseField;
    }

    public void setBaseField(String baseField)
    {
        this.baseField = baseField;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [value = "+value+", label = "+label+"]";
    }
}
