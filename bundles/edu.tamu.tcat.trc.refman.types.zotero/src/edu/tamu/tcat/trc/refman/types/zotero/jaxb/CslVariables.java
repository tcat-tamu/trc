package edu.tamu.tcat.trc.refman.types.zotero.jaxb;

public class CslVariables
{
    private CslVariable vars;

    private CslItemTypes itemTypes;

    /**
     *
     * @return The CSL variables. Note that this is distinct from item types.
     */
    public CslVariable getVars ()
    {
        return vars;
    }

    public void setVars (CslVariable vars)
    {
        this.vars = vars;
    }

    public CslItemTypes getItemTypes ()
    {
        return itemTypes;
    }

    public void setItemTypes (CslItemTypes itemTypes)
    {
        this.itemTypes = itemTypes;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [vars = "+vars+", itemTypes = "+itemTypes+"]";
    }
}
