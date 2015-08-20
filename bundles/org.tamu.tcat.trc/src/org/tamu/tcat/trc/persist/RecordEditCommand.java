package org.tamu.tcat.trc.persist;

import java.util.concurrent.Future;

/**
 * This API is currently a work in progress.
 *
 * @param <T>
 */
public interface RecordEditCommand<T>
{

   Future<String> execute();
}
