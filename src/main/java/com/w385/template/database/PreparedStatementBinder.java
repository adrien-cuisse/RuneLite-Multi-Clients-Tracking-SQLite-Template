package com.w385.template.database;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * A callback to bind values to a {@link PreparedStatement PreparedStatement}
 */
@FunctionalInterface
interface PreparedStatementBinder
{
	/**
	 * Binds values to placeholders in a prepared statement
	 *
	 * @param statement - the statement in which placeholders must be
	 * bound to values
	 *
	 * @throws SQLException is the statement is misused
	 *
	 * @see PreparedStatement
	 */
	void bind(PreparedStatement statement) throws SQLException;
}
