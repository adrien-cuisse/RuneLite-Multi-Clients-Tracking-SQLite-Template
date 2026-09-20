package com.w385.template.database;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Very thin layer above sqlite-jdbc.
 * Offers minimal access to a database, and provides the following functionalities:
 * <ul>
 *	 <li>execute arbitrary queries (eg, tables creation)</li>
 *	 <li>inserting rows</li>
 *	 <li>fetching rows</li>
 *	 <li>updating rows</li>
 *	 <li>deleting rows</li>
 * </ul>
 * The underlying connection is auto-handled and no direct access is provided.
 */
public final class SQLiteDatabase implements AutoCloseable
{
	/**
	 * The current connection
	 */
	private Connection connection;

	/**
	 * The fully-qualified URL to connect to the database
	 */
	private final String url;

	/**
	 * @param path the path to where the database file will be saved, should
	 *  point to your plugin's directory inside RUNELITE_DIR.
	 */
	public SQLiteDatabase(Path path)
	{
		this.url = "JDBC:sqlite:" + path;
	}

	/**
	 * Executes a single SQLite instruction.
	 * This function must <i style="color:#F00">NEVER</i> be called with
	 * something else than a hardcoded query-string.
	 * This function must <i style="color:#F80">NOT</i> be called in the same thread
	 * as the Plugin class to comply with RuneLite restrictions.
	 * To execute a query from inputs (e.g., some file content), use the following:
	 *  <ul>
	 *	  <li>{@link SQLiteDatabase#insert(String query, PreparedStatementBinder binder) insert}</li>
	 *	  <li>{@link SQLiteDatabase#fetch(String query, ResultSetReader reader) fetch}</li>
	 *	  <li>{@link SQLiteDatabase#update(String query, PreparedStatementBinder binder) update}</li>
	 *	  <li>{@link SQLiteDatabase#delete(String query, PreparedStatementBinder binder) delete}</li>
	 *  </ul>
	 * If no connection is currently open, a new one is created.
	 * If this is the first connection to the database and the file doesn't
	 * exist, it is created as blank.
	 * If the query was a write-operation and succeeded, the SQLite header is
	 * added to the file if it's still blank.
	 *
	 * @param queryString the raw query-string to execute
	 *
	 * @throws SQLException if the connection could not be opened
	 */
	public void execute(String queryString) throws SQLException
	{
		try (Statement statement = this.connection().createStatement())
		{
			statement.execute(queryString);
		}
	}

	/**
	 * Executes an INSERT statement.
	 * This function must <i style="color:#F80">NOT</i> be called in the same thread
	 * as the Plugin class to comply with RuneLite restrictions.
	 * If no connection is currently open, a new one is created.
	 * If this is the first connection to the database and the file doesn't
	 * exist, it is created as blank.
	 * If this write-operation succeeds, the SQLite header is added to the file
	 * if it's still blank.
	 *
	 * @param query the query containing the INSERT statement to execute,
	 *  which should contain placeholders
	 * @param binder the function to bind actual values to the placeholders
	 *
	 * @throws SQLException if a database access error occurs or if query was
	 *  a read-operation, or if the provided binder misused the statement
	 *
	 * @see PreparedStatement
	 */
	public void insert(String query, PreparedStatementBinder binder) throws SQLException
	{
		try (PreparedStatement statement = this.connection().prepareStatement(query))
		{
			binder.bind(statement);
			statement.executeUpdate();
		}
	}

	/**
	 * Executes a SELECT statement without placeholders, fetching the whole table.
	 * This function must <i style="color:#F80">NOT</i> be called in the same thread
	 * as the Plugin class to comply with RuneLite restrictions.
	 * If no connection is currently open, a new one is created.
	 * If this is the first connection to the database and the file doesn't
	 * exist, it is created as blank.
	 *
	 * @param query the query containing the SELECT statement to execute
	 * @param reader the function to read from the ResultSet
	 *
	 * @throws SQLException if a database access error occurs or if query was
	 *  a write-operation, or if the provided reader misused the results
	 *
	 * @see ResultSet
	 */
	public void fetch(String query, ResultSetReader reader) throws SQLException
	{
		try (Statement statement = this.connection().createStatement())
		{
			ResultSet rows = statement.executeQuery(query);
			reader.supply(rows);
			rows.close();
		}
	}

	/**
	 * Executes a SELECT statement with placeholders, fetching only matching rows.
	 * This function must <i style="color:#F80">NOT</i> be called in the same thread
	 * as the Plugin class to comply with RuneLite restrictions.
	 * If no connection is currently open, a new one is created.
	 * If this is the first connection to the database and the file doesn't
	 * exist, it is created as blank.
	 *
	 * @param query the query containing the SELECT statement to execute
	 * @param binder the function to bind parameters to the statement
	 * @param reader the function to read from the ResultSet
	 *
	 * @throws SQLException if a database access error occurs or if query was
	 *  a write-operation, if the provided binder misuses the statement, or if
	 *  the provided reader misused the results
	 *
	 * @see PreparedStatement
	 * @see ResultSet
	 */
	public void fetch(String query, PreparedStatementBinder binder, ResultSetReader reader) throws SQLException
	{
		try (PreparedStatement statement = this.connection().prepareStatement(query))
		{
			binder.bind(statement);
			ResultSet rows = statement.executeQuery();
			reader.supply(rows);
			rows.close();
		}
	}

	/**
	 * Executes an UPDATE statement.
	 * This function must <i style="color:#F80">NOT</i> be called in the same thread
	 * as the Plugin class to comply with RuneLite restrictions.
	 * If no connection is currently open, a new one is created.
	 * If this is the first connection to the database and the file doesn't
	 * exist, it is created as blank.
	 * If this write-operation succeeds, the SQLite header is added to the file
	 * if it's still blank.
	 *
	 * @param query the query containing the UPDATE statement to execute,
	 *  which must contain placeholders
	 * @param binder the function to bind actual values to the placeholders
	 *
	 * @throws SQLException if a database access error occurs or if query was
	 *  a read-operation, or if the provided binder misused the statement
	 *
	 * @see PreparedStatement
	 */
	public void update(String query, PreparedStatementBinder binder) throws SQLException
	{
		try (PreparedStatement statement = this.connection().prepareStatement(query))
		{
			binder.bind(statement);
			statement.executeUpdate();
		}
	}

	/**
	 * Executes a DELETE statement.
	 * This function must <i style="color:#F80">NOT</i> be called in the same thread
	 * as the Plugin class to comply with RuneLite restrictions.
	 * If no connection is currently open, a new one is created.
	 * If this is the first connection to the database and the file doesn't
	 * exist, it is created as blank.
	 * If this write-operation succeeds, the SQLite header is added to the file
	 * if it's still blank.
	 *
	 * @param query the query containing the DELETE statement to execute,
	 *  which may contain placeholders
	 * @param binder the function to bind actual values to the placeholders
	 *
	 * @throws SQLException if a database access error occurs or if query was
	 *  a read-operation, or if the provided binder misused the statement
	 *
	 * @see PreparedStatement
	 */
	public void delete(String query, PreparedStatementBinder binder) throws SQLException
	{
		try (PreparedStatement statement = this.connection().prepareStatement(query))
		{
			binder.bind(statement);
			statement.executeUpdate();
		}
	}

	/**
	 * Closes the underlying connection.
	 * If connection is already closed, this is a no-op.
	 *
	 * @throws Exception if a database access error occurs
	 */
	@Override
	public void close() throws Exception
	{
		if (!this.isOpen())
			return;

		this.connection.close();
		this.connection = null;
	}

	/**
	 * Returns the connection to the database, if none, a new one is created.
	 *
	 * @return the connection to the database
	 *
	 * @throws SQLException if a database access error occurs while opening
	 *  a new connection if needed
	 */
	private Connection connection() throws SQLException
	{
		if (!this.isOpen())
			this.connect();
		return this.connection;
	}

	/**
	 * Creates a new connection to the database if none is currently open.
	 * If this is the first connection to the database, the blank file is created.
	 *
	 * @throws SQLException if a database access error occurs
	 */
	private void connect() throws SQLException
	{
		if (this.isOpen())
			return;
		this.connection = DriverManager.getConnection(this.url);
	}

	/**
	 * Checks whether there is an open connection to the database.
	 *
	 * @return true is the connection exists, false otherwise
	 */
	private boolean isOpen()
	{
		return this.connection != null;
	}

	/**
	 * A callback to bind values to be inserted in the database.
	 */
	@FunctionalInterface
	public interface PreparedStatementBinder
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

	/**
	 * A callback to read queried rows.
	 */
	@FunctionalInterface
	public interface ResultSetReader
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
}
