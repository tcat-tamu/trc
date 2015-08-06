package edu.tamu.tcat.trc.refman.types.zotero.jaxb;

public class ZTypes
{
    private ZoteroTypeMap[] typeMap;

    public ZoteroTypeMap[] getTypeMap ()
    {
        return typeMap;
    }

    public void setTypeMap (ZoteroTypeMap[] typeMap)
    {
        this.typeMap = typeMap;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [typeMap = "+typeMap+"]";
    }
}
