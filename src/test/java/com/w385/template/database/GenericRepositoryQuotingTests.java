package com.w385.template.database;

import org.junit.Before;
import org.junit.Test;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public final class GenericRepositoryQuotingTests extends DatabaseTestSuite
{
	private static final String TABLE_NAME = "quoting-tests";

	private final GenericRepository repository = new GenericRepository(this.database)
	{
		@Override
		protected String table()
		{
			return TABLE_NAME;
		}
	};

	/**
	 * Creates a table with a TEXT column whose name needs to be quoted inside
	 * queries to avoid syntax error
	 *
	 * @throws SQLException if database access error occurs
	 */
	@Before
	public void createTable() throws SQLException
	{
		String tableQuery = "CREATE TABLE \"" + TABLE_NAME + "\" (\n"
			+ "    \"id\" INTEGER PRIMARY KEY AUTOINCREMENT,\n"
			+ "    \"TABLE\" TEXT NOT NULL\n"
			+ ");";
		this.database.execute(tableQuery);
	}

	@Test
	public void columnsAreQuotedInInsertQueries() throws SQLException
	{
		// given: a column whose name is a reserved keyword
		var entry = new HashMap<String, Object>();
		entry.put("TABLE", "identifiers should be quoted");

		// when: using it in a query
		this.repository.insertMap(entry);

		// then: it should have been quoted, and no exception should be thrown
	}

	@Test
	public void columnsAreQuotedInSelectQueriesWhereClause() throws SQLException
	{
		// given: a condition targeting a column whose name is a reserved keyword
		var where = new WhereCondition("TABLE", SqlOperator.EQUALS, "foo");

		// when: using it in a query
		this.repository.fetchMap(where);

		// then: it should have been quoted, and no exception should be thrown
	}

	@Test
	public void columnsAreQuotedInUpdateQueries() throws SQLException
	{
		// given: a column whose name is a reserved keyword
		Map<String, Object> updates = Map.of("TABLE", "bar");

		// when: using it in a query
		this.repository.updateMap(updates);

		// then: it should have been quoted, and no exception should be thrown
	}

	@Test
	public void columnsAreQuotedInUpdateQueriesWhereClause() throws SQLException
	{
		// given: a condition targeting a column whose name is a reserved keyword
		var where = new WhereCondition("TABLE", SqlOperator.EQUALS, "baz");

		// when: using it in a query
		Map<String, Object> updates = Map.of("TABLE", "bar");
		this.repository.updateMap(updates, where);

		// then: it should have been quoted, and no exception should be thrown
	}

	@Test
	public void columnsAreQuotedInDeleteQueries() throws SQLException
	{
		// given: a condition targeting a column whose name is a reserved keyword
		var where = new WhereCondition("TABLE", SqlOperator.EQUALS, "foobar");

		// when: using it in a query
		this.repository.delete(where);

		// then: it should have been quoted, and no exception should be thrown
	}
}
