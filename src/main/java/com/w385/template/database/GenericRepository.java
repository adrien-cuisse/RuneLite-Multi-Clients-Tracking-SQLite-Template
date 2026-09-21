package com.w385.template.database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

/**
 * Handles a single SQL table and related queries.
 * Identifiers are escaped so any symbol can be used (e.g., dashes, spaces, etc.)
 * Only boxed types and Instant are currently supported for mapping.
 */
public final class GenericRepository implements AutoCloseable
{
	/**
	 * The database the repository is connected to
	 */
	private final SQLiteDatabase database;

	/**
	 * The SQL table this repository is bound to
	 */
	private final String table;

	public GenericRepository(SQLiteDatabase database, String table)
	{
		this.database = database;
		this.table = table;
	}

	/**
	 * Insert a new row in the table.
	 * This function must <i style="color:#F80">NOT</i> be called in the same thread
	 * as the Plugin class to comply with RuneLite restrictions.
	 *
	 * @param map - the map of the object to insert, with keys being column names,
	 *	and values the values to insert in the corresponding column, null
	 * 	values are accepted if corresponding column in nullable, and nullable
	 * 	columns may be omitted
	 *
	 * @throws SQLException if a database access error occurs
	 */
	public void insert(Map<String, Object> map) throws SQLException
	{
		String query = createInsertionQuery(map);
		List<Object> values = new ArrayList<>(map.values());
		this.database.insert(query, this.bindParameters(values));
	}

	/**
	 * Fetches rows from the table, in order.
	 * This function must <i style="color:#F80">NOT</i> be called in the same thread
	 * as the Plugin class to comply with RuneLite restrictions.
	 *
	 * @param where - the conditions to filter the rows, if none is provided the
	 * whole table will be fetched
	 *
	 * @return - a map for every row, values may be null if corresponding column
	 * 	was nullable
	 *
	 * @throws SQLException if a database access error occurs
	 */
	public List<Map<String, String>> fetch(WhereCondition ...where) throws SQLException
	{
		String query = createFetchQuery(where);

		List<Map<String, String>> results = new ArrayList<>();

		if (where.length == 0)
			this.database.fetch(query, this.readResults(results));
		else
			this.database.fetch(query, this.bindParameters(where), this.readResults(results));

		return results;
	}

	/**
	 * Updates rows from the table.
	 * This function must <i style="color:#F80">NOT</i> be called in the same thread
	 * as the Plugin class to comply with RuneLite restrictions.
	 *
	 * @param columns the new values to write, with keys being columns name,
	 *	and values the values to write in the corresponding column
	 * @param where only rows matching these conditions will be updated.
	 *	If no conditions are provided, every row will be updated.
	 *
	 * @throws SQLException if a database access error occurs
	 */
	public void update(Map<String, Object> columns, WhereCondition ...where) throws SQLException
	{
		if (columns.isEmpty())
			return;

		String query = createUpdateQuery(columns, where);

		var values = new ArrayList<>(columns.values());
		stream(where)
			.map(condition -> condition.value)
			.forEach(values::add);

		this.database.update(query, this.bindParameters(values));
	}

	/**
	 * Deletes rows from the table.
	 * This function must <i style="color:#F80">NOT</i> be called in the same thread
	 * as the Plugin class to comply with RuneLite restrictions.
	 *
	 * @param where only rows matching these conditions will be deleted.
	 *	If no conditions are provided, every row will be deleted.
	 *
	 * @throws SQLException if a database access error occurs
	 */
	public void delete(WhereCondition ...where) throws SQLException
	{
		String query = createDeleteQuery(where);

		List<Object> values = stream(where)
			.map(condition -> condition.value)
			.collect(toList());

		this.database.delete(query, this.bindParameters(values));
	}

	@Override
	public void close() throws Exception
	{
		this.database.close();
	}

	/**
	 * Binds a parameter to the statement.
	 *
	 * @param statement the statement to bind a parameter to
	 * @param value the value to bind
	 * @param index the index of the parameter, starting from 1
	 *
	 * @throws SQLException if a database access error occurs, if statement is
	 * 	already closed, if index doesn't match
	 * @throws IllegalArgumentException if the type of the value isn't supported
	 */
	private void bindParameter(PreparedStatement statement, Object value, int index) throws SQLException
	{
		if (value == null)
			statement.setNull(index, java.sql.Types.NULL);
		else if (value instanceof Integer)
			statement.setInt(index, (int) value);
		else if (value instanceof String)
			statement.setString(index, (String) value);
		else if (value instanceof Double)
			statement.setDouble(index, (double) value);
		else if (value instanceof Instant)
			statement.setLong(index, ((Instant) value).getEpochSecond());
		else if (value instanceof Boolean)
			statement.setBoolean(index, (boolean) value);
		else if (value instanceof Long)
			statement.setLong(index, (long) value);
		else if (value instanceof Float)
			statement.setFloat(index, (float) value);
		else if (value instanceof Short)
			statement.setShort(index, (short) value);
		else if (value instanceof Byte)
			statement.setByte(index, (byte) value);
		else if (value instanceof Character)
			statement.setString(index, String.valueOf(value));
		else
			throw new IllegalArgumentException("Type " + value.getClass().getName() + " not supported");
	}

	/**
	 * Creates a binder that will bind the provided values, in order.
	 * The binder will throw if a database access error occurs or if statement is
	 * already closed.
	 *
	 * @param values the values to bind to the statement
	 *
	 * @return the created binder
	 */
	private SQLiteDatabase.PreparedStatementBinder bindParameters(List<Object> values)
	{
		return statement ->
		{
			int index = 1;
			for (Object value : values)
				this.bindParameter(statement, value, index++);
		};
	}

	/**
	 * Creates a binder that will bind the values from the provided conditions.
	 * The binder will throw if a database access error occurs or if statement is
	 * already closed.
	 *
	 * @param where the conditions containing the values to bind
	 *
	 * @return the created binder
	 */
	private SQLiteDatabase.PreparedStatementBinder bindParameters(WhereCondition ...where)
	{
		return statement ->
		{
			for (int i = 0; i < where.length; i++)
				this.bindParameter(statement, where[i].value, i + 1);
		};
	}

	/**
	 * Reads the row the ResultSet is currently pointing to.
	 *
	 * @param meta the metadata of the ResultSet
	 * @param resultSet the ResultSet to read
	 *
	 * @return the extracted row
	 *
	 * @throws SQLException if database access error occurs, or if the ResultSet
	 * 	is already closed
	 */
	private Map<String, String> readRow(ResultSetMetaData meta, ResultSet resultSet) throws SQLException
	{
		var map = new HashMap<String, String>();

		for (int i = 0; i < meta.getColumnCount(); i++)
			map.put(meta.getColumnName(i + 1), resultSet.getString(i + 1));

		return map;
	}

	/**
	 * Creates a reader that will fill the provided collection, in row-order.
	 * The reader will throw if a database access error occurs or if statement is
	 * already closed.
	 *
	 * @param rows the collection to fill
	 *
	 * @return the created reader
	 */
	private SQLiteDatabase.ResultSetReader readResults(Collection<Map<String, String>> rows)
	{
		return resultSet ->
		{
			ResultSetMetaData meta = resultSet.getMetaData();
			while (resultSet.next())
				rows.add(this.readRow(meta, resultSet));
		};
	}

	/**
	 * Creates an INSERT query with placeholders to be filled from a binder.
	 *
	 * @param map - the columns to add in the query
	 *
	 * @return the created query
	 */
	private String createInsertionQuery(Map<String, Object> map)
	{
		String columns = map.keySet().stream()
			.map(column -> "\"" + column + "\"")
			.collect(joining(", ", " (", ") "));

		String placeholders = map.values().stream()
			.map(unused -> "?")
			.collect(joining(", ", " (", ")"));

		return "INSERT INTO \"" + this.table + "\""
			+ columns
			+ "VALUES"
			+ placeholders;
	}

	/**
	 * Creates a SELECT query.
	 *
	 * @param where the conditions to add in the WHERE clause, if no conditions
	 * are provided then no WHERE clause will be added and no binder will be
	 * needed
	 *
	 * @return the created query
	 */
	private String createFetchQuery(WhereCondition[] where)
	{
		String query = "SELECT * FROM \"" + this.table + "\"";

		if (where.length == 0)
			return query;

		String whereClause = stream(where)
			.map(WhereCondition::toString)
			.collect(joining(" AND "));

		return query + " WHERE " + whereClause;
	}

	/**
	 * Creates an UPDATE query.
	 *
	 * @param columns the columns to update, with names as keys and values to
	 * insert for corresponding keys
	 * @param where - the conditions to add in the WHERE clause, if no conditions
	 * are provided then no WHERE clause will be added and no binder will be needed
	 *
	 * @return the created query
	 */
	private String createUpdateQuery(Map<String, Object> columns, WhereCondition ...where)
	{
		String query = "UPDATE \"" + this.table + "\" SET ";

		String assignments = columns.keySet().stream()
			.map(column -> "\"" + column + "\"")
			.map(column -> column + " = ?")
			.collect(joining(", "));

		if (where.length == 0)
			return query + assignments;

		String whereClause = stream(where)
			.map(WhereCondition::toString)
			.collect(joining(" AND "));

		return query + assignments + " WHERE " + whereClause;
	}

	/**
	 * Creates a DELETE query.
	 *
	 * @param where - the conditions to add in the WHERE clause, if no conditions
	 * are provided then no WHERE clause will be added and no binder will be needed
	 *
	 * @return the created query
	 */
	private String createDeleteQuery(WhereCondition ...where)
	{
		String query = "DELETE FROM \"" + this.table + "\"";

		if (where.length == 0)
			return query;

		String whereClause = stream(where)
			.map(WhereCondition::toString)
			.collect(joining(" AND "));

		return query + " WHERE " + whereClause;
	}
}
