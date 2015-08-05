package edu.tamu.tcat.trc.refman.types.zotero.jaxb;

public class CslCreatorMap
{
    private CslFieldtoZFieldMap[] map;

    public CslFieldtoZFieldMap[] getMap ()
    {
        return map;
    }

    public void setMap (CslFieldtoZFieldMap[] map)
    {
        this.map = map;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [map = "+map+"]";
    }
}
