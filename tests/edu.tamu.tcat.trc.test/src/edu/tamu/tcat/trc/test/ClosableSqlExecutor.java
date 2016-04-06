package edu.tamu.tcat.trc.test;

import edu.tamu.tcat.db.exec.sql.SqlExecutor;

public interface ClosableSqlExecutor extends SqlExecutor, AutoCloseable
{
}
