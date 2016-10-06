The majority of this bundle is not psql specific. It would be nice to break out the 
things that explicitly depend on psql from those that don't so that we can more easily replace
them with a different backing system in the future.