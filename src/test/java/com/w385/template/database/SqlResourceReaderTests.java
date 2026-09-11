package com.w385.template.database;

import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public final class SqlResourceReaderTests
{
	private final SqlResourceReader reader = new SqlResourceReader();

	@Test
	public void nullPath_nullResult() throws IOException
	{
		String resource = null;
		List<String> statements = this.reader.read(resource);
		assertThat(statements, is(nullValue()));
	}

	@Test
	public void resourceDoesNotExist_nullResult() throws IOException
	{
		String resource = "/resource-which-does-not-exist";
		List<String> statements = this.reader.read(resource);
		assertThat(statements, is(nullValue()));
	}

	@Test
	public void emptyResource_nullResult() throws IOException
	{
		String resource = "/sqlite/empty.sql";
		List<String> statements = this.reader.read(resource);
		assertThat(statements, is(nullValue()));
	}

	@Test
	public void blankResource_nullResult() throws IOException
	{
		String resource = "/sqlite/blank.sql";
		List<String> statements = this.reader.read(resource);
		assertThat(statements, is(nullValue()));
	}

	@Test
	public void singleInstructionWithoutSemiColon() throws IOException
	{
		String resource = "/sqlite/single-instruction.sql";
		List<String> statements = this.reader.read(resource);
		assertThat(statements, is(List.of("SELECT * FROM my_table")));
	}

	@Test
	public void singleInstructionWithSemiColon() throws IOException
	{
		String resource = "/sqlite/single-instruction-semicolon.sql";
		List<String> statements = this.reader.read(resource);
		assertThat(statements, is(List.of("SELECT \"something\" FROM \"some_table\"")));
	}

	@Test
	public void singleInstructionOnSeveralLines() throws IOException
	{
		String resource = "/sqlite/single-instruction-several-lines.sql";
		List<String> statements = this.reader.read(resource);
		assertThat(statements, is(List.of("SELECT\n\t*\nFROM\n\tfree_stuff")));
	}

	@Test
	public void severalInstructionOnSeveralLines() throws IOException
	{
		String resource = "/sqlite/several-instructions.sql";
		List<String> statements = this.reader.read(resource);
		assertThat(statements, is(List.of("PRAGMA foreign_keys = OFF", "DELETE FROM items")));
	}

	@Test
	public void commentsAreStripped() throws IOException
	{
		String resource = "/sqlite/script-with-comments.sql";
		List<String> statements = this.reader.read(resource);
		assertThat(statements, is(List.of("DELETE FROM bank_account")));
	}

	@Test
	public void emptyStatementsAreStripped() throws IOException
	{
		String resource = "/sqlite/empty-statements.sql";
		List<String> statements = this.reader.read(resource);
		assertThat(statements, is(List.of("PRAGMA foreign_keys = OFF", "DELETE FROM some_table")));
	}

	@Test
	public void noStatements_nullResult() throws IOException {
		String resource = "/sqlite/no-statements.sql";
		List<String> statements = this.reader.read(resource);
		assertThat(statements, is(nullValue()));
	}
}
