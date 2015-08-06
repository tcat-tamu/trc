package edu.tamu.tcat.trc.refman.types.zotero.jaxb;

public class CslCreatorMap
{
    private CreatorTypeMap[] map;

    public CreatorTypeMap[] getMap ()
    {
        return map;
    }

    public void setMap (CreatorTypeMap[] map)
    {
        this.map = map;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [map = "+map+"]";
    }
}
