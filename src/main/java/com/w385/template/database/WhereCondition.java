package com.w385.template.database;

/**
 * A condition in a WHERE clause
 */
public class WhereCondition
{
	/**
	 * The targeted column
	 */
	public final String column;

	/**
	 * The operator to apply
	 */
	public final SqlOperator operator;

	/**
	 * The value to match against the column
	 */
	public final Object value;

	public WhereCondition(String column, SqlOperator operator, Object value)
	{
		this.column = column;
		this.operator = operator;
		this.value = value;
	}

	@Override
	public String toString()
	{
		return String.format("\"%s\" %s ?", this.column, this.operator.symbol());
	}
}
