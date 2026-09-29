package com.w385.template.domain;

public abstract class Entity<T>
{
	protected T identity;

	public T getIdentity()
	{
		return this.identity;
	}

	public void setIdentity(T id)
	{
		this.identity = id;
	}
}
