package edu.tamu.tcat.trc.refman.types.zotero.jaxb;

import javax.xml.bind.annotation.*;

@XmlRootElement(name="map")
@XmlAccessorType(XmlAccessType.FIELD)
public class ZoteroMap
{
    private CslFieldMap cslFieldMap;

    private CiteprocJStoCSLmap citeprocJStoCSLmap;

    private ZTypes zTypes;

    private CslCreatorMap cslCreatorMap;

    private ZoteroVersion zoteroVersion;

    private CslVariables cslVars;

    private ZoteroDate date;

    public CslFieldMap getCslFieldMap ()
    {
        return cslFieldMap;
    }

    public void setCslFieldMap (CslFieldMap cslFieldMap)
    {
        this.cslFieldMap = cslFieldMap;
    }

    public CiteprocJStoCSLmap getCiteprocJStoCSLmap ()
    {
        return citeprocJStoCSLmap;
    }

    public void setCiteprocJStoCSLmap (CiteprocJStoCSLmap citeprocJStoCSLmap)
    {
        this.citeprocJStoCSLmap = citeprocJStoCSLmap;
    }

    public ZTypes getZTypes ()
    {
        return zTypes;
    }

    public void setZTypes (ZTypes zTypes)
    {
        this.zTypes = zTypes;
    }

    public CslCreatorMap getCslCreatorMap ()
    {
        return cslCreatorMap;
    }

    public void setCslCreatorMap (CslCreatorMap cslCreatorMap)
    {
        this.cslCreatorMap = cslCreatorMap;
    }

    public ZoteroVersion getZoteroVersion ()
    {
        return zoteroVersion;
    }

    public void setZoteroVersion (ZoteroVersion zoteroVersion)
    {
        this.zoteroVersion = zoteroVersion;
    }

    public CslVariables getCslVars ()
    {
        return cslVars;
    }

    public void setCslVars (CslVariables cslVars)
    {
        this.cslVars = cslVars;
    }

    public ZoteroDate getDate ()
    {
        return date;
    }

    public void setDate (ZoteroDate date)
    {
        this.date = date;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [cslFieldMap = "+cslFieldMap+", citeprocJStoCSLmap = "+citeprocJStoCSLmap+", zTypes = "+zTypes+", cslCreatorMap = "+cslCreatorMap+", zoteroVersion = "+zoteroVersion+", cslVars = "+cslVars+", date = "+date+"]";
    }
}
