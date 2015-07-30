package edu.tamu.tcat.trc.refman.types.zotero.jaxb;

import javax.xml.bind.annotation.XmlAttribute;

public class Var
{
    private String description;

    private String name;

    private String type;

    @XmlAttribute
    public String getDescription ()
    {
        return description;
    }

    public void setDescription (String description)
    {
        this.description = description;
    }

    @XmlAttribute
    public String getName ()
    {
        return name;
    }

    public void setName (String name)
    {
        this.name = name;
    }

    @XmlAttribute
    public String getType ()
    {
        return type;
    }

    public void setType (String type)
    {
        this.type = type;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [description = "+description+", name = "+name+", type = "+type+"]";
    }
}
