package edu.tamu.tcat.trc.refman.types.zotero.jaxb;

public class ZTypes
{
    private TypeMap[] typeMap;

    public TypeMap[] getTypeMap ()
    {
        return typeMap;
    }

    public void setTypeMap (TypeMap[] typeMap)
    {
        this.typeMap = typeMap;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [typeMap = "+typeMap+"]";
    }
}
