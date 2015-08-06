package edu.tamu.tcat.trc.refman.types.zotero.jaxb;

public class CslVariable
{
    private CslVar[] var;

    public CslVar[] getVar ()
    {
        return var;
    }

    public void setVar (CslVar[] var)
    {
        this.var = var;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [var = "+var+"]";
    }
}
