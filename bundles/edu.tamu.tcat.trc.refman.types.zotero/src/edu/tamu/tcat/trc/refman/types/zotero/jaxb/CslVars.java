package edu.tamu.tcat.trc.refman.types.zotero.jaxb;

public class CslVars
{
    private Vars vars;

    private ItemTypes itemTypes;

    /**
     *
     * @return The CSL variables. Note that this is distinct from item types.
     */
    public Vars getVars ()
    {
        return vars;
    }

    public void setVars (Vars vars)
    {
        this.vars = vars;
    }

    public ItemTypes getItemTypes ()
    {
        return itemTypes;
    }

    public void setItemTypes (ItemTypes itemTypes)
    {
        this.itemTypes = itemTypes;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [vars = "+vars+", itemTypes = "+itemTypes+"]";
    }
}
