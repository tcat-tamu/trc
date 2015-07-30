package edu.tamu.tcat.trc.refman.types.zotero.jaxb;

import javax.xml.bind.annotation.XmlAttribute;

public class Field
{
    private String value;

    private String label;

    private CreatorType[] creatorType;

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

    public CreatorType[] getCreatorType ()
    {
        return creatorType;
    }

    public void setCreatorType (CreatorType[] creatorType)
    {
        this.creatorType = creatorType;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [value = "+value+", label = "+label+", creatorType = "+creatorType+"]";
    }
}
