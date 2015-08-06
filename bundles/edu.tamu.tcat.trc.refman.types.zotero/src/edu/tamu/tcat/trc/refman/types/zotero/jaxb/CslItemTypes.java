package edu.tamu.tcat.trc.refman.types.zotero.jaxb;

public class ItemTypes
{
    private Type[] type;

    public Type[] getType ()
    {
        return type;
    }

    public void setType (Type[] type)
    {
        this.type = type;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [type = "+type+"]";
    }
}
