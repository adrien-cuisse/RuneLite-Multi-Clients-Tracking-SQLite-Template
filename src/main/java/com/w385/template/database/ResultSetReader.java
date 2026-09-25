package com.w385.template.database;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * A callback to read queried rows.
 */
@FunctionalInterface
interface ResultSetReader
{
	/**
	 * Reads from a ResultSet
	 *
	 * @param resultSet - the ResultSet to read from
	 *
	 * @throws SQLException is the ResultSet is misused
	 *
	 * @see ResultSet
	 */
	void supply(ResultSet resultSet) throws SQLException;
}
