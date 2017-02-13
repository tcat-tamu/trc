package edu.tamu.tcat.trc.repo;

@FunctionalInterface
public interface DocRepoBuilderFactory
{
   /**
    * Builds a basic document repository using the supplied configuration utilities.
    *
    * @param tablename The name of the database table (or other storage specific mechanism)
    *       for storing recording these entries. Note that if the underlying table does not
    *       exist, it will be created.
    * @param factory A factory for generating edit commands.
    * @param adapter An adapter to convert stored data representations (type DTO) to
    *       instances of the associated entries Java type (an Interface).
    * @param type The Java interface that defines this entry.
    *
    * @return A document repository with the supplied configuration.
    */
   <T, DTO, CMD> DocRepoBuilder<T, DTO, CMD> getDocRepoBuilder();
}
