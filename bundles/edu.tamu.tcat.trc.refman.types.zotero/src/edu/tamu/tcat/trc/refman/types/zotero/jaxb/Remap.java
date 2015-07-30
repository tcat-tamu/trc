package edu.tamu.tcat.trc.refman.types.zotero.jaxb;

import javax.xml.bind.annotation.XmlAttribute;

public class Remap
{
    private String cslUsage;

    private String citeprocField;

    private String descKey;

    @XmlAttribute
    public String getCslUsage ()
    {
        return cslUsage;
    }

    public void setCslUsage (String cslUsage)
    {
        this.cslUsage = cslUsage;
    }

    @XmlAttribute
    public String getCiteprocField ()
    {
        return citeprocField;
    }

    public void setCiteprocField (String citeprocField)
    {
        this.citeprocField = citeprocField;
    }

    @XmlAttribute
    public String getDescKey ()
    {
        return descKey;
    }

    public void setDescKey (String descKey)
    {
        this.descKey = descKey;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [cslUsage = "+cslUsage+", citeprocField = "+citeprocField+", descKey = "+descKey+"]";
    }
}
