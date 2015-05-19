package edu.tamu.tcat.catalogentries;

public interface CommandExecutionListener
{
   // currently a placeholder
   void notifyCommandExectution(ExecutionEvent evt) throws Exception;

   interface ExecutionEvent
   {
      Object getCommand();
   }
}
