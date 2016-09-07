/*
 * Copyright 2015 Texas A&M Engineering Experiment Station
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.tamu.tcat.trc.entries.types.biblio.search;

import edu.tamu.tcat.trc.search.SearchException;

/**
 * The main API for searching a {@link BibliographicEntryRepository}.
 */
public interface WorkSearchService
{
   /**
    * Create a new command for searching works. The returned {@link WorkQueryCommand} may be
    * parameterized according to the search criteria and executed to run the search against
    * this service and the {@link BibliographicEntryRepository} backing it.
    *
    * @throws SearchException
    */
   /*
    * This is a command instead of a builder. Needs:
    *  - variable parameters; handling of default options
    *  - ability to "export" query for persist
    *  - query operates on this service
    * Both builder and command pattern suit these needs. However, the command pattern
    * also forces the returned object to be executed in its own context, being that of
    * its source (this service). The builder pattern alternatively would force the
    * returned builder to require additional API, a "work query", which would be provided
    * to the service again for execution, involving more error handling.
    *
    * The builder and command would both need an API such as "export" to convert the
    * state stored to a persistable form, and likely this service again is the point
    * of reconstruction from that persistable form.
    */
   WorkQueryCommand createQueryCommand() throws SearchException;

   //TODO: potential API for import/export; to be used for user "saved queries"
   //Object exportPersistable(WorkQueryCommand cmd);
   //WorkQueryCommand importPersistable(Object exportedCommand);

   //TODO: would it be helpful to get the repo from here?
   //WorkRepository getRepository();
}
