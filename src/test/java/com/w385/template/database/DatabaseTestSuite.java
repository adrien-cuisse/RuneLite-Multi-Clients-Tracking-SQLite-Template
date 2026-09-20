package com.w385.template.database;

import org.junit.ClassRule;
import org.junit.rules.TemporaryFolder;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Random;

abstract class DatabaseTestSuite
{
	/**
	 * The path to the directory the current test will take place in
	 */
	@ClassRule
	public static TemporaryFolder WORKING_DIRECTORY = new TemporaryFolder();

	/**
	 * The path to the database file the current test will use
	 */
	protected final Path path = WORKING_DIRECTORY.getRoot()
		.toPath()
		.resolve(randomString() + ".sqlite3");

	/**
	 * The SUT in itself, or the access used by the SUT
	 */
	protected final SQLiteDatabase database = new SQLiteDatabase(this.path);

	/**
	 * Creates a raw connection from the JDBC SQLite driver, bypassing the SUT.
	 * Caller is in charge of closing the connection when he's done with it.
	 *
	 * @return the opened connection
	 */
	protected Connection rawConnection() throws SQLException
	{
		return DriverManager.getConnection("JDBC:sqlite:" + this.path);
	}

	/**
	 * Generates a random string
	 *
	 * @return the random string
	 */
	private static String randomString()
	{
		return new Random().ints(97, 123)
			.limit(16)
			.collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
			.toString();
	}
}
