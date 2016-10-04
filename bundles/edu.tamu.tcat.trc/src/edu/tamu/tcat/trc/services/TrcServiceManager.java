package edu.tamu.tcat.trc.services;

import edu.tamu.tcat.account.Account;
import edu.tamu.tcat.trc.entries.core.resolver.EntryReference;

/**
 * Provides a central point of access for TRC Services.
 *
 * <p>The TRC framework provides a variety of services to support both system internal
 * functions (such as versioning of entries) and editorial and end-user interaction with
 * content (such as creating notes or categorizing entries). Examples of these services
 * include:
 *
 * <ul>
 *   <li>Notes</li>
 *   <li>Categorizations</li>
 *   <li>Bibliographic Reference</li>
 *   <li>Versioning</li>
 *   <li>Related Entries</li>
 *   <li>Editorial Tasks</li>
 * </ul>
 *
 * <p>Service can represent content that is similar to that supported by TRC Entry types,
 * such as TRC Articles and Notes, TRC Relationships and Related Entries, and TRC
 * Bibliographic Entries and bibliographic references. The primary difference between content
 * managed by services and the content associated with a TRC Entry, is that entries represent
 * units of scholarly production. This assumes an identifiable author and versions of the
 * content as well as the investment of significant editorial expertise. Content managed by
 * services, on the other hand, is typically intended to provide supporting material about a
 * TRC Entry (for example, a set of bibliographic references about works consulted when create
 * a TRC Bibliographic Entry on a significant historical work) or facilitate personal,
 * editorial or group oriented notetaking and annotation support.
 *
 * <p>Services are accessed within the scope of a {@link ServiceContext}. For example, the
 * categorization service supports system and user defined curation of material within a TRC.
 * Categorization schemes are identified by user-defined keys that are unique within a the
 * context of a given scope id (for instance, a user or group id). The specific parameters
 * associated with a {@code ServiceContext} are defined by the service implementation. One
 * core element associated with all {@code ServiceContext} instances is the {@link Account}
 * that is associated with the service request.
 *
 *
 *
 */
public interface TrcServiceManager
{
// TODO should add ACL, perhaps resolvers.

   /**
    *
    * @param ctx
    * @return A service for the
    *
    */
   <ServiceType> ServiceType getService(ServiceContext<ServiceType> ctx) throws TrcServiceException;

   <EntryType> EntryMediator<EntryType> getMediator(EntryType entry, Account account);

   EntryMediator<?> getMediator(EntryReference ref, Account account);
}
