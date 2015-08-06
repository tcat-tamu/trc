package edu.tamu.tcat.trc.refman.types.zotero.jaxb;

import javax.xml.bind.annotation.XmlAttribute;

public class CreatorTypeMap
{
    private String cslField;

    private String zField;

    @XmlAttribute
    public String getCslField ()
    {
        return cslField;
    }

    public void setCslField (String cslField)
    {
        this.cslField = cslField;
    }

    @XmlAttribute
    public String getZField ()
    {
        return zField;
    }

    public void setZField (String zField)
    {
        this.zField = zField;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [cslField = "+cslField+", zField = "+zField+"]";
    }
}
