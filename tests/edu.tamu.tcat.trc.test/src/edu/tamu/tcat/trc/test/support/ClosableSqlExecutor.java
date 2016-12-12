package edu.tamu.tcat.trc.test.support;

import edu.tamu.tcat.db.exec.sql.SqlExecutor;

public interface ClosableSqlExecutor extends SqlExecutor, AutoCloseable
{
}
