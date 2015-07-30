package edu.tamu.tcat.trc.refman.types.zotero.jaxb;

public class Vars
{
    private Var[] var;

    public Var[] getVar ()
    {
        return var;
    }

    public void setVar (Var[] var)
    {
        this.var = var;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [var = "+var+"]";
    }
}
