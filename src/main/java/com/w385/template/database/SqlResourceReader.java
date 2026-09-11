package com.w385.template.database;

import lombok.extern.slf4j.Slf4j;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

@Slf4j
public final class SqlResourceReader
{
	private static final String INPUT_LINE_SEPARATOR = "\\R";

	private static final String OUTPUT_LINE_SEPARATOR = System.lineSeparator();

	private static final String STATEMENT_DELIMITER = ";";

	private static final String COMMENT_MARKER = "--";

	private static final String TRAILING_COMMENT = "\\s*" + COMMENT_MARKER + ".*$";

	private static final String TRAILING_LINE_TERMINATIONS = "\\R*\\z";

	/**
	 * Returns the content of a valid UTF-8 SQL resource, as a list of usable non-empty UTF-8 statements.
	 * The resource must not contain semicolon except to delimit statements.
	 * Statements must not contain comment markers, such as inside string-values.
	 * The resource may contain comments, but they must NOT contain semicolon.
	 * No parsing is performed but statements are normalized.
	 * If a list is returned, it is guaranteed to be non-empty.
	 *
	 * @param resourcePath - the path inside the resources/ directory, e.g. "/database/bar.sql"
	 *
	 * @return - a non-empty list of non-empty SQL statements, or null if:
	 * 	- null path,
	 * 	- resource not found,
	 *  - resource not readable,
	 *  - blank resource (i.e., no SQL instruction left after removing comments).
	 *
	 * @throws IOException - if I/O error occurs
	 * 	@see InputStream::close()
	 * 	@see InputStream::readAllBytes()
	 */
	public List<String> read(String resourcePath) throws IOException
	{
		String sql = readContent(resourcePath);
		if (sql == null)
			return null;

		List<String> statements = splitStatements(sql);
		if (statements.isEmpty())
			return null;

		return statements;
	}

	/**
	 * Returns the content of a text resource, as a single UTF-8 non-blank String.
	 *
	 * @param path - the path inside the resources/ directory, e.g. "/img/bar.foo"
	 *
	 * @return - the content of the resource without trailing line-terminations, or null if:
	 * 	- null path,
	 * 	- resource not found,
	 * 	- resource not readable,
	 * 	- blank resource.
	 *
	 * @throws IOException - if I/O error occurs
	 * 	@see InputStream::close()
	 * 	@see InputStream::readAllBytes()
	 */
	private String readContent(String path) throws IOException
	{
		if (path == null)
			return null;

		try (InputStream stream = getClass().getResourceAsStream(path))
		{
			if (stream == null)
			{
				log.debug("SQL resource {} not found or not readable", path);
				return null;
			}

			String content = new String(stream.readAllBytes(), UTF_8).replaceFirst(TRAILING_LINE_TERMINATIONS, "");
			if (content.isBlank())
			{
				log.debug("SQL resource {} is blank", path);
				return null;
			}

			return content;
		}
	}

	/**
	 * Splits an SQL string into separate non-blank statements
	 * Doesn't handle semicolons inside statements
	 *
	 * @param sql - the SQL String containing the statements to split
	 *
	 * @return - the SQL statements as a list
	 */
	private List<String> splitStatements(String sql)
	{
		return Arrays.stream(sql.split(STATEMENT_DELIMITER))
			.map(this::stripComments)
			.map(String::strip)
			.filter(this::isNotBlank)
			.collect(toList());
	}

	/**
	 * Removes comments from the given statement, whether they are on their own
	 * line or after some text.
	 *
	 * @param statement - the statement to strip
	 *
	 * @return - the statement without comments
	 */
	private String stripComments(String statement)
	{
		return Arrays.stream(statement.split(INPUT_LINE_SEPARATOR))
			.filter(this::isNotComment)
			.map(this::stripTrailingComment)
			.collect(joining(OUTPUT_LINE_SEPARATOR));
	}

	/**
	 * Checks whether the string is NOT blank
	 *
	 * @param string - the string to check
	 *
	 * @return - true if the string is NOT blank, false otherwise
	 */
	private boolean isNotBlank(String string)
	{
		return !string.isBlank();
	}

	/**
	 * Checks whether the given line is a comment.
	 *
	 * @param line - the line to check
	 *
	 * @return - false if the first non-blank characters are "--", true otherwise
	 */
	private boolean isNotComment(String line)
	{
		return !line.strip().startsWith(COMMENT_MARKER);
	}

	/**
	 * Removes the comments at the end of the line.
	 *
	 * @param line - the line to strip
	 *
	 * @return - the line without trailing comments
	 */
	private String stripTrailingComment(String line)
	{
		return line.replaceFirst(TRAILING_COMMENT, "");
	}
}
