package edu.tamu.tcat.trc.services.categorization;

import edu.tamu.tcat.account.Account;

/**
 *  Defines a scope that is used to organize related categorizations paired with an
 *  {@link Account} that identifies the actor associated with a given repository instance.
 *
 *  FIXME review and revise JavaDoc
 *  <p>Categorizations are defined relative to some scope. For example, some may be
 *  global to the site and integral to the vision of the TRC, such as an encyclopedia
 *  of reference articles. Other categorizations might reflect personal note-taking
 *  tools for users such as notebooks. Still others might reflect group efforts such
 *  as a set of people compiled as part of a group project for a class.
 *
 *  <p>To support this, the {@link CategorizationSchemesResource} is designed to be
 *  configured as a sub-resource and provided within a particular scoped context
 *  such as a user's personal categorizations, the categorizations maintained
 *  globally by the TRC editors, etc. The specific API endpoint that provides
 *  access to a {@code CategorizationSchemesResource} should document the nature and
 *  purpose of categorization within that scope, but will provide a common API
 *  implementation.
 *
 */
public interface CategorizationScope
{
   String getScopeId();

   Account getAccount();
}
