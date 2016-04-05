package edu.tamu.tcat.trc.entries.types.biblio.rest;

import java.util.Collection;
import java.util.function.Consumer;

public interface CollectionRepoHelper<Model, Editor>
{
   Collection<Model> get();

   RepoHelper<Model, Editor> get(String id);

   String create(Consumer<Editor> modifier);
}
