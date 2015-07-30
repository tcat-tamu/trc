package edu.tamu.tcat.trc.refman.types.zotero.jaxb;

public class CslCreatorMap
{
    private Map[] map;

    public Map[] getMap ()
    {
        return map;
    }

    public void setMap (Map[] map)
    {
        this.map = map;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [map = "+map+"]";
    }
}
