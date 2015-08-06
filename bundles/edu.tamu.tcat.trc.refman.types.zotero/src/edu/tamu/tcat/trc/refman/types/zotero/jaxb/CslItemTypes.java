package edu.tamu.tcat.trc.refman.types.zotero.jaxb;

public class CslItemTypes
{
    private ZoteroType[] type;

    public ZoteroType[] getType ()
    {
        return type;
    }

    public void setType (ZoteroType[] type)
    {
        this.type = type;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [type = "+type+"]";
    }
}
