package com.w385.template.domain;

/**
 * An object that is uniquely identifiable
 *
 * @param <T> the type of the identity, e.g.: Integer or String
 */
public abstract class Entity<T>
{
	/**
	 * The identity, may be null, usually an Integer or String.
	 * This MUST NOT be used in equals() and hashCode(), otherwise
	 * the entity could be unreachable in hash-based collections and maps.
	 */
	protected T identity;

	/**
	 * @return the identity, null if entity has never been persisted.
	 */
	public T getIdentity()
	{
		return this.identity;
	}

	/**
	 * Assigns a new identity to the entity.
	 * This SHOULD NOT be called manually, unless you know what you're doing.
	 *
	 * @param id - the new identity to assign
	 */
	public void setIdentity(T id)
	{
		this.identity = id;
	}
}
