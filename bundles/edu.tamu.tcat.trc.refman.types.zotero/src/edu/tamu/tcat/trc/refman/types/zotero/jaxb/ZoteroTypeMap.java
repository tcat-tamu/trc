package edu.tamu.tcat.trc.refman.types.zotero.jaxb;

import javax.xml.bind.annotation.XmlAttribute;

public class ZoteroTypeMap
{
    private ZoteroTypeField[] field;

    private String zType;

    private String cslType;

    public ZoteroTypeField[] getField ()
    {
        return field;
    }

    public void setField (ZoteroTypeField[] field)
    {
        this.field = field;
    }

    @XmlAttribute
    public String getZType ()
    {
        return zType;
    }

    public void setZType (String zType)
    {
        this.zType = zType;
    }

    @XmlAttribute
    public String getCslType ()
    {
        return cslType;
    }

    public void setCslType (String cslType)
    {
        this.cslType = cslType;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [field = "+field+", zType = "+zType+", cslType = "+cslType+"]";
    }
}
