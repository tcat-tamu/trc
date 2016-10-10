package edu.tamu.tcat.trc.services.seealso;

public interface Link
{
   /**
    * @return The source identifier of this link
    */
   String getSource();

   /**
    * @return The target identifier of this link
    */
   String getTarget();
}