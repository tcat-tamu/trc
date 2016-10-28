package edu.tamu.tcat.trc.impl.psql.services.seealso;

import edu.tamu.tcat.trc.services.seealso.Link;

public class LinkImpl implements Link
{
   private final String source;
   private final String target;

   public LinkImpl(String source, String target)
   {
      this.source = source;
      this.target = target;
   }

   @Override
   public String getSource()
   {
      return source;
   }

   @Override
   public String getTarget()
   {
      return target;
   }

}
