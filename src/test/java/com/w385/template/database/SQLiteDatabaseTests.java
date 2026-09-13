package com.w385.template.database;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public final class SQLiteDatabaseTests
{
    @ClassRule
    public static TemporaryFolder WORKING_DIRECTORY = new TemporaryFolder();

    private final Path path = WORKING_DIRECTORY.getRoot()
        .toPath()
        .resolve(randomString() + ".sqlite3");

    private final SQLiteDatabase database = new SQLiteDatabase(this.path);

    @Test
    public void executesSql() throws SQLException
    {
        // given: a query to create a table
        String query = tableCreationQueryString("the one we just created");

        // when: executing the query
        this.database.execute(query);

        // then: the table should exist afterward, hence the query got executed successfully
        Connection connection = this.rawConnection();
        DatabaseMetaData meta = connection.getMetaData();
        ResultSet result = meta.getTables(null, null, null, new String[] { "table" });
        result.next();
        assertThat(result.getString("TABLE_NAME"), is("the one we just created"));

        // clean-up
        result.close();
        connection.close();
    }

    @Test
    public void insertsRow() throws SQLException
    {
        // given: a database containing an empty table
        Connection connection = rawConnection();
        Statement creationStatement = connection.createStatement();
        creationStatement.execute(tableCreationQueryString("insertion"));

        // when: inserting a new row with a single value into that empty table
        this.database.insert(
            "INSERT INTO \"insertion\" (content) VALUES (?);",
            statement -> statement.setString(1, "the inserted content"));

        // then: the table should contain the row afterward, hence insertion succeeded
        Statement fetchStatement = connection.createStatement();
        ResultSet rows = fetchStatement.executeQuery("SELECT * FROM \"insertion\"");
        rows.next();
        assertThat(rows.getString("content"), is("the inserted content"));

        // clean-up
        rows.close();
        fetchStatement.close();
        creationStatement.close();
        connection.close();
    }

    @Test
    public void fetchesRow() throws SQLException
    {
        // given: a database containing a table containing rows
        Connection connection = rawConnection();
        Statement creationStatement = connection.createStatement();
        creationStatement.execute(tableCreationQueryString("fetch"));
        Statement insertionStatement = connection.createStatement();
        insertionStatement.execute("INSERT INTO \"fetch\" (content, author) VALUES ('correct', 'awesome')");

        // when: fetching all the rows from that specific table
        Map<String, String> row = new HashMap<>();
        this.database.fetch(
            "SELECT * FROM \"fetch\"",
            resultSet ->
            {
                resultSet.next();
                row.put("content", resultSet.getString("content"));
                row.put("author", resultSet.getString("author"));
            }
        );

        // then: we should have the rows, hence fetch succeeded
        assertThat(row.get("content"), is("correct"));
        assertThat(row.get("author"), is("awesome"));

        // clean-up
        insertionStatement.close();
        creationStatement.close();
        connection.close();
    }

    @Test
    public void updatesRow() throws SQLException
    {
        // given: a database containing a table containing rows
        Connection connection = rawConnection();
        Statement creationStatement = connection.createStatement();
        creationStatement.execute(tableCreationQueryString("update"));
        Statement insertionStatement = connection.createStatement();
        insertionStatement.execute("INSERT INTO \"update\" (content, author) VALUES ('old code', 'refactoring')");

        // when: deleting every row in that specific table
        this.database.update(
            "UPDATE \"update\" SET content = ? WHERE author = ?",
            s ->
            {
                s.setString(1, "new code");
                s.setString(2, "refactoring");
            }
        );

        // then: the rows should have been updated, hence update succeeded
        Statement fetchStatement = connection.createStatement();
        ResultSet rows = fetchStatement.executeQuery("SELECT * FROM \"update\"");
        rows.next();
        assertThat(rows.getString("content"), is("new code"));

        // clean-up
        rows.close();
        fetchStatement.close();
        insertionStatement.close();
        creationStatement.close();
        connection.close();
    }

    @Test
    public void deletesRow() throws SQLException
    {
        // given: a database containing a table containing a single row
        Connection connection = rawConnection();
        Statement creationStatement = connection.createStatement();
        creationStatement.execute(tableCreationQueryString("deletion"));
        Statement insertionStatement = connection.createStatement();
        insertionStatement.execute("INSERT INTO \"deletion\" (content) VALUES ('obsolete')");

        // when: deleting that row in that specific table
        this.database.delete(
            "DELETE FROM \"deletion\" WHERE content = ?",
            statement -> statement.setString(1, "obsolete"));

        // then: the table should be empty, hence deletion succeeded
        Statement fetchStatement = connection.createStatement();
        ResultSet rows = fetchStatement.executeQuery("SELECT * FROM \"deletion\"");
        assertThat(rows.next(), is(false));

        // clean-up
        rows.close();
        fetchStatement.close();
        insertionStatement.close();
        creationStatement.close();
        connection.close();
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
     * Creates a query-string to create a table containing:
     *  - a non-nullable "content" text column.
     *  - a nullable "author" text column, which defaults to NULL if no value specified.
     *
     * @param table the name of the table, will be quoted so special symbols SQL keywords may be used
     *
     * @return the created query-string
     */
    private static String tableCreationQueryString(String table)
    {
        // peak efficiency - workaround for missing text-blocks in Java 11
        return "CREATE TABLE \"" + table + "\" (\n"
            + "    \"id\" INTEGER PRIMARY KEY AUTOINCREMENT,\n"
            + "    \"content\" TEXT NOT NULL,\n"
            + "    \"author\" TEXT DEFAULT NULL\n"
            + ");";
    }
}
