package com.w385.template.database;

/**
 * An operator to target a column
 */
public enum SqlOperator
{
	EQUALS("="),
	DIFFERENT_FROM("<>"),
	LESSER_THAN("<"),
	LESSER_OR_EQUAL_TO("<="),
	GREATER_THAN(">"),
	GREATER_OR_EQUAL_TO(">=");

	private final String symbol;

	SqlOperator(String symbol)
	{
		this.symbol = symbol;
	}

	public String symbol()
	{
		return this.symbol;
	}
}
