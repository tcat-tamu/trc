package edu.tamu.tcat.trc.refman.types.zotero.jaxb;

public class CiteprocJStoCSLmap
{
    private Remap[] remap;

    public Remap[] getRemap ()
    {
        return remap;
    }

    public void setRemap (Remap[] remap)
    {
        this.remap = remap;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [remap = "+remap+"]";
    }
}
