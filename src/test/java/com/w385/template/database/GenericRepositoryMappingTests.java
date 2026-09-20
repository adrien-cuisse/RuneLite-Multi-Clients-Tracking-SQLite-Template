package com.w385.template.database;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public final class GenericRepositoryMappingTests
{
	@ClassRule
	public static TemporaryFolder WORKING_DIRECTORY = new TemporaryFolder();

	private final Path path = WORKING_DIRECTORY.getRoot()
		.toPath()
		.resolve(randomString() + ".sqlite3");

	private final SQLiteDatabase database = new SQLiteDatabase(this.path);

	private final GenericRepository repository = new GenericRepository(this.database, TABLE_NAME);

	private static final String TABLE_NAME = "mappings-test";

	@Test
	public void supportsIntegerInsertMapping() throws SQLException
	{
		// given:
		createTableWithContentColumnOfType("INTEGER");

		// when:
		Map<String, Object> map = Map.of("content", 42);
		this.repository.insert(map);

		// then:
		Connection rawConnection = rawConnection();
		Statement fetchStatement = rawConnection.createStatement();
		ResultSet rows = fetchStatement.executeQuery("SELECT * FROM \"" + TABLE_NAME + "\"");
		assertThat(rows.next(), is(true));
		assertThat(rows.getInt("content"), is(42));

		// clean-up
		rows.close();
		fetchStatement.close();
		rawConnection.close();
	}

	@Test
	public void supportsLongInsertMapping() throws SQLException
	{
		// given:
		createTableWithContentColumnOfType("INTEGER");

		// when:
		Map<String, Object> map = Map.of("content", 42L);
		this.repository.insert(map);

		// then:
		Connection rawConnection = rawConnection();
		Statement fetchStatement = rawConnection.createStatement();
		ResultSet rows = fetchStatement.executeQuery("SELECT * FROM \"" + TABLE_NAME + "\"");
		assertThat(rows.next(), is(true));
		assertThat(rows.getLong("content"), is(42L));

		// clean-up
		rows.close();
		fetchStatement.close();
		rawConnection.close();
	}

	@Test
	public void supportsShortInsertMapping() throws SQLException
	{
		// given:
		createTableWithContentColumnOfType("INTEGER");

		// when:
		Map<String, Object> map = Map.of("content", (short) 42);
		this.repository.insert(map);

		// then:
		Connection rawConnection = rawConnection();
		Statement fetchStatement = rawConnection.createStatement();
		ResultSet rows = fetchStatement.executeQuery("SELECT * FROM \"" + TABLE_NAME + "\"");
		assertThat(rows.next(), is(true));
		assertThat(rows.getShort("content"), is((short) 42));

		// clean-up
		rows.close();
		fetchStatement.close();
		rawConnection.close();
	}

	@Test
	public void supportsByteInsertMapping() throws SQLException
	{
		// given:
		createTableWithContentColumnOfType("INTEGER");

		// when:
		Map<String, Object> map = Map.of("content", (byte) 42);
		this.repository.insert(map);

		// then:
		Connection rawConnection = rawConnection();
		Statement fetchStatement = rawConnection.createStatement();
		ResultSet rows = fetchStatement.executeQuery("SELECT * FROM \"" + TABLE_NAME + "\"");
		assertThat(rows.next(), is(true));
		assertThat(rows.getByte("content"), is((byte) 42));

		// clean-up
		rows.close();
		fetchStatement.close();
		rawConnection.close();
	}

	@Test
	public void supportsBooleanInsertMapping() throws SQLException
	{
		// given:
		createTableWithContentColumnOfType("INTEGER");

		// when:
		Map<String, Object> map = Map.of("content", true);
		this.repository.insert(map);

		// then:
		Connection rawConnection = rawConnection();
		Statement fetchStatement = rawConnection.createStatement();
		ResultSet rows = fetchStatement.executeQuery("SELECT * FROM \"" + TABLE_NAME + "\"");
		assertThat(rows.next(), is(true));
		assertThat(rows.getBoolean("content"), is(true));

		// clean-up
		rows.close();
		fetchStatement.close();
		rawConnection.close();
	}

	@Test
	public void supportsCharInsertMapping() throws SQLException
	{
		// given:
		createTableWithContentColumnOfType("TEXT");

		// when:
		Map<String, Object> map = Map.of("content", 'é');
		this.repository.insert(map);

		// then:
		Connection rawConnection = rawConnection();
		Statement fetchStatement = rawConnection.createStatement();
		ResultSet rows = fetchStatement.executeQuery("SELECT * FROM \"" + TABLE_NAME + "\"");
		assertThat(rows.next(), is(true));
		assertThat(rows.getString("content").charAt(0), is('é'));

		// clean-up
		rows.close();
		fetchStatement.close();
		rawConnection.close();
	}

	@Test
	public void supportsInstantInsertMappingWithSecondsAccuracy() throws SQLException
	{
		// given:
		createTableWithContentColumnOfType("LONG");

		// when:
		Instant now = Instant.now();
		Map<String, Object> map = Map.of("content", now);
		this.repository.insert(map);

		// then:
		Connection rawConnection = rawConnection();
		Statement fetchStatement = rawConnection.createStatement();
		ResultSet rows = fetchStatement.executeQuery("SELECT * FROM \"" + TABLE_NAME + "\"");
		assertThat(rows.next(), is(true));
		assertThat(rows.getLong("content"), is(now.getEpochSecond()));

		// clean-up
		rows.close();
		fetchStatement.close();
		rawConnection.close();
	}

	@Test
	public void supportsFloatInsertMapping() throws SQLException
	{
		// given:
		createTableWithContentColumnOfType("REAL");

		// when:
		Map<String, Object> map = Map.of("content", 42.0f);
		this.repository.insert(map);

		// then:
		Connection rawConnection = rawConnection();
		Statement fetchStatement = rawConnection.createStatement();
		ResultSet rows = fetchStatement.executeQuery("SELECT * FROM \"" + TABLE_NAME + "\"");
		assertThat(rows.next(), is(true));
		assertThat(rows.getFloat("content"), is(42.0f));

		// clean-up
		rows.close();
		fetchStatement.close();
		rawConnection.close();
	}

	@Test
	public void supportsDoubleInsertMapping() throws SQLException
	{
		// given:
		createTableWithContentColumnOfType("REAL");

		// when:
		Map<String, Object> map = Map.of("content", "string");
		this.repository.insert(map);

		// then:
		Connection rawConnection = rawConnection();
		Statement fetchStatement = rawConnection.createStatement();
		ResultSet rows = fetchStatement.executeQuery("SELECT * FROM \"" + TABLE_NAME + "\"");
		assertThat(rows.next(), is(true));
		assertThat(rows.getString("content"), is("string"));

		// clean-up
		rows.close();
		fetchStatement.close();
		rawConnection.close();
	}

	@Test
	public void supportsStringInsertMapping() throws SQLException
	{
		// given:
		createTableWithContentColumnOfType("TEXT");

		// when:
		Map<String, Object> map = Map.of("content", 42.0d);
		this.repository.insert(map);

		// then:
		Connection rawConnection = rawConnection();
		Statement fetchStatement = rawConnection.createStatement();
		ResultSet rows = fetchStatement.executeQuery("SELECT * FROM \"" + TABLE_NAME + "\"");
		assertThat(rows.next(), is(true));
		assertThat(rows.getDouble("content"), is(42.0d));

		// clean-up
		rows.close();
		fetchStatement.close();
		rawConnection.close();
	}

	@Test
	public void supportsNullInsertMapping() throws SQLException
	{
		// given:
		createTableWithContentColumnOfType("INTEGER");

		// when:
		Map<String, Object> map = new HashMap<>() {{ put("content", null); }};
		this.repository.insert(map);

		// then:
		Connection rawConnection = rawConnection();
		Statement fetchStatement = rawConnection.createStatement();
		ResultSet rows = fetchStatement.executeQuery("SELECT * FROM \"" + TABLE_NAME + "\"");
		assertThat(rows.next(), is(true));
		rows.getInt("content");
		assertThat(rows.wasNull(), is(true));

		// clean-up
		rows.close();
		fetchStatement.close();
		rawConnection.close();
	}

	@Test
	public void supportsNullFetchMapping() throws SQLException
	{
		// given:
		createTableWithContentColumnOfType("INTEGER");
		Connection rawConnection = rawConnection();
		Statement insertStatement = rawConnection.createStatement();
		insertStatement.executeUpdate("INSERT INTO \"" + TABLE_NAME + "\" (\"content\") VALUES (null)");

		// when:
		List<Map<String, String>> rows = this.repository.fetch();

		// then:
		assertThat(rows.size(), is(1));
		assertThat(rows.get(0).get("content"), is(nullValue()));

		// clean-up
		insertStatement.close();
		rawConnection.close();
	}

	/**
	 * Creates a raw connection from the JDBC SQLite driver.
	 * Caller is in charge of closing the connection when he's done with it.
	 *
	 * @return the opened connection
	 */
	private Connection rawConnection() throws SQLException
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

	/**
	 * Creates a table with a non-nullable "content" column of given type
	 *
	 * @param type the type of the column: "INTEGER" / "REAL" / "TEXT"
	 *
	 * @throws SQLException if database access error occurs
	 */
	private void createTableWithContentColumnOfType(String type) throws SQLException
	{
		String tableQuery = "CREATE TABLE \"" + TABLE_NAME + "\" (\n"
			+ "    \"id\" INTEGER PRIMARY KEY AUTOINCREMENT,\n"
			+ "    \"content\" " + type + "\n"
			+ ");";
		this.database.execute(tableQuery);
	}
}
