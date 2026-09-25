package com.w385.template.database;

import java.sql.SQLException;

/**
 * SQLException wrapped into a RuntimeException, so it doesn't propagate through
 * 	signatures.
 * 	It's "quite unlikely" we can recover without database anyway.
 */
public final class UncheckedSQLException extends RuntimeException
{
	public UncheckedSQLException(SQLException exception)
	{
		super(exception);
	}
}
