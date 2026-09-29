package com.w385.template.domain;

/**
 * An object that is uniquely identifiable
 *
 * @param <T> the type of the identity, e.g.: Integer or String
 */
public abstract class Entity<T>
{
	/**
	 * The identity, may be null, usually an Integer or String
	 */
	protected T identity;

	/**
	 * @return the identity
	 */
	public T getIdentity()
	{
		return this.identity;
	}

	/**
	 * Assigns a new identity to the entity
	 *
	 * @param id - the new identity to assign
	 */
	public void setIdentity(T id)
	{
		this.identity = id;
	}
}
