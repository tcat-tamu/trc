package edu.tamu.tcat.trc.repo;

import java.util.concurrent.Future;

/**
 * This API is currently a work in progress.
 *
 * @param <T>
 */
public interface RecordEditCommand
{

   Future<String> execute();
}
