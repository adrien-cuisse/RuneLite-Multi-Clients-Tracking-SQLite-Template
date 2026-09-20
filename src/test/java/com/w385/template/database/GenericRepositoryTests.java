package com.w385.template.database;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import java.nio.file.Path;
import java.sql.*;
import java.util.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public final class GenericRepositoryTests
{
	@ClassRule
	public static TemporaryFolder WORKING_DIRECTORY = new TemporaryFolder();

	private final Path path = WORKING_DIRECTORY.getRoot()
		.toPath()
		.resolve(randomString() + ".sqlite3");

	private final SQLiteDatabase database = new SQLiteDatabase(this.path);

	private final GenericRepository repository = new GenericRepository(this.database, TABLE_NAME);

	private static final String TABLE_NAME = "beers";

	@Before
	public void buildDatabase() throws SQLException
	{
		String tableCreationQuery = "CREATE TABLE \"" + TABLE_NAME + "\" (\n"
			+ "    \"id\" INTEGER PRIMARY KEY AUTOINCREMENT,\n"
			+ "    \"brand\" TEXT NOT NULL,\n"
			+ "    \"abv\" DECIMAL NOT NULL,\n"
			+ "    \"volume\" INTEGER NOT NULL\n"
			+ ");";
		this.database.execute(tableCreationQuery);

		String insertionQuery = "INSERT INTO \"" + TABLE_NAME + "\"\n"
			+ "    (\"brand\", \"abv\", \"volume\")\n"
			+ "    VALUES\n"
			+ "    ('La Chouffe', 8.0, 500),"
			+ "    ('Duvel', 8.5, 750),"
			+ "    ('Tripel LEFORT', 8.8, 750)";
		this.database.execute(insertionQuery);
	}

	@Test
	public void insertsRow() throws SQLException
	{
		// given: an object to persist
		var beer = new HashMap<String, Object>();
		beer.put("brand", "3 Monts");
		beer.put("abv", 8.5D);
		beer.put("volume", 500);

		// when: persisting it
		this.repository.insert(beer);

		// then: it should be in the database afterward
		Connection rawConnection = rawConnection();
		Statement fetchStatement = rawConnection.createStatement();
		ResultSet rows = fetchStatement.executeQuery("SELECT * FROM \"" + TABLE_NAME + "\" WHERE \"brand\" = '3 Monts'");
		assertThat(rows.next(), is(true));
		assertThat(rows.getString("brand"), is("3 Monts"));
		assertThat(rows.getDouble("abv"), is(8.5));
		assertThat(rows.getInt("volume"), is(500));

		// clean-up
		rows.close();
		fetchStatement.close();
		rawConnection.close();
	}

	@Test
	public void fetchesWholeTable() throws SQLException
	{
		// given: no filters
		WhereCondition[] where = {};
		// when: fetching the table
		Collection<Map<String, String>> beers = this.repository.fetch(where);
		// then: it should return every row
		assertThat(beers.size(), is(3));
	}

	@Test
	public void fetchesMatchingRows() throws SQLException
	{
		// given: filters to apply
		WhereCondition[] where =
		{
			new WhereCondition("abv", SqlOperator.EQUALS, 8.0D),
			new WhereCondition("volume", SqlOperator.LESSER_OR_EQUAL_TO, 660)
		};

		// when: fetching the table
		ArrayList<Map<String, String>> beers = new ArrayList<>(this.repository.fetch(where));

		// then: it only returns matching rows
		assertThat(beers.size(), is(1));
		assertThat(beers.get(0).get("brand"), is("La Chouffe"));
	}

	@Test
	public void updatesNothing() throws SQLException
	{
		// given: nothing to update
		var updates = new HashMap<String, Object>();
		// when: trying to launch the query
		this.repository.update(updates);

		// then: it should be a no-op
		Connection rawConnection = rawConnection();
		Statement fetchStatement = rawConnection.createStatement();
		ResultSet rows = fetchStatement.executeQuery("SELECT * FROM \"" + TABLE_NAME + "\"");
		assertThat(rows.next(), is(true));
		assertThat(rows.getString("brand"), is("La Chouffe"));
		assertThat(rows.getDouble("abv"), is(8.0));
		assertThat(rows.getInt("volume"), is(500));
		assertThat(rows.next(), is(true));
		assertThat(rows.getString("brand"), is("Duvel"));
		assertThat(rows.getDouble("abv"), is(8.5));
		assertThat(rows.getInt("volume"), is(750));
		assertThat(rows.next(), is(true));
		assertThat(rows.getString("brand"), is("Tripel LEFORT"));
		assertThat(rows.getDouble("abv"), is(8.8));
		assertThat(rows.getInt("volume"), is(750));

		// clean-up
		rows.close();
		fetchStatement.close();
		rawConnection.close();

	}

	@Test
	public void updatesWholeTable() throws SQLException
	{
		// given: columns to update
		var updates = new HashMap<String, Object>();
		updates.put("abv", 4.5);
		updates.put("volume", 330);

		// when: updating
		this.repository.update(updates);

		// then: the whole table should have been updated
		Connection rawConnection = rawConnection();
		Statement fetchStatement = rawConnection.createStatement();
		ResultSet rows = fetchStatement.executeQuery("SELECT * FROM \"" + TABLE_NAME + "\"");
		assertThat(rows.next(), is(true));
		assertThat(rows.getString("brand"), is("La Chouffe"));
		assertThat(rows.getDouble("abv"), is(4.5));
		assertThat(rows.getInt("volume"), is(330));
		assertThat(rows.next(), is(true));
		assertThat(rows.getString("brand"), is("Duvel"));
		assertThat(rows.getDouble("abv"), is(4.5));
		assertThat(rows.getInt("volume"), is(330));
		assertThat(rows.next(), is(true));
		assertThat(rows.getString("brand"), is("Tripel LEFORT"));
		assertThat(rows.getDouble("abv"), is(4.5));
		assertThat(rows.getInt("volume"), is(330));

		// clean-up
		rows.close();
		fetchStatement.close();
		rawConnection.close();
	}

	@Test
	public void updatesMatchingRows() throws SQLException
	{
		// given: columns to update...
		var columns = new HashMap<String, Object>();
		columns.put("abv", 6.25);
		// ... and row filters
		WhereCondition[] where =
		{
			new WhereCondition("volume", SqlOperator.GREATER_THAN, 660),
			new WhereCondition("volume", SqlOperator.LESSER_THAN, 1000),
		};

		// when: updating
		this.repository.update(columns, where);

		// then: ...
		Connection rawConnection = rawConnection();
		Statement fetchStatement = rawConnection.createStatement();
		ResultSet rows = fetchStatement.executeQuery("SELECT * FROM \"" + TABLE_NAME + "\"");
		// ...non-matching row should remain unchanged...
		assertThat(rows.next(), is(true));
		assertThat(rows.getString("brand"), is("La Chouffe"));
		assertThat(rows.getDouble("abv"), is(8.0));
		assertThat(rows.getInt("volume"), is(500));
		// ... and matching rows should be updated
		assertThat(rows.next(), is(true));
		assertThat(rows.getString("brand"), is("Duvel"));
		assertThat(rows.getDouble("abv"), is(6.25));
		assertThat(rows.getInt("volume"), is(750));
		assertThat(rows.next(), is(true));
		assertThat(rows.getString("brand"), is("Tripel LEFORT"));
		assertThat(rows.getDouble("abv"), is(6.25));
		assertThat(rows.getInt("volume"), is(750));

		// clean-up
		rows.close();
		fetchStatement.close();
		rawConnection.close();
	}

	@Test
	public void deletesWholeTable() throws SQLException
	{
		// given: no filters

		// when: deleting rows in the table
		this.repository.delete();

		// then: the table should be empty
		Connection rawConnection = rawConnection();
		Statement fetchStatement = rawConnection.createStatement();
		ResultSet rows = fetchStatement.executeQuery("SELECT * FROM \"" + TABLE_NAME + "\"");
		assertThat(rows.next(), is(false));

		// clean-up
		rows.close();
		fetchStatement.close();
		rawConnection.close();
	}

	@Test
	public void deletesMatchingRows() throws SQLException
	{
		// given: filters to apply
		WhereCondition[] where =
		{
			new WhereCondition("brand", SqlOperator.DIFFERENT_FROM, "Duvel"),
			new WhereCondition("volume", SqlOperator.GREATER_OR_EQUAL_TO, 600),
		};

		// when: deleting rows in the table
		this.repository.delete(where);

		// then: only matching rows should have been deleted
		Connection rawConnection = rawConnection();
		Statement fetchStatement = rawConnection.createStatement();
		ResultSet rows = fetchStatement.executeQuery("SELECT * FROM \"" + TABLE_NAME + "\"");
		assertThat(rows.next(), is(true));
		assertThat(rows.getString("brand"), is("La Chouffe"));
		assertThat(rows.next(), is(true));
		assertThat(rows.getString("brand"), is("Duvel"));
		assertThat(rows.next(), is(false));

		// clean-up
		rows.close();
		fetchStatement.close();
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
}
