package com.w385.demo.movements;

import com.w385.template.domain.Entity;
import java.time.Instant;
import java.util.Objects;

public final class Movement extends Entity<Integer>
{
	private final String emitter;

	private final Instant timestamp;

	private final int abscissa;

	private final int ordinate;

	private final int floor;

	public Movement(String emitter, Instant timestamp, int abscissa, int ordinate, int floor)
	{
		this(null, emitter, timestamp, abscissa, ordinate, floor);
	}

	public Movement(Integer id, String emitter, Instant timestamp, int abscissa, int ordinate, int floor)
	{
		this.identity = id;
		this.emitter = emitter;
		this.timestamp = timestamp;
		this.abscissa = abscissa;
		this.ordinate = ordinate;
		this.floor = floor;
	}

	public String emitter()
	{
		return this.emitter;
	}

	public Instant timestamp()
	{
		return this.timestamp;
	}

	public int abscissa()
	{
		return this.abscissa;
	}

	public int ordinate()
	{
		return this.ordinate;
	}

	public int floor()
	{
		return this.floor;
	}

	@Override
	public boolean equals(Object other)
	{
		if (other == null)
			return false;
		if (!(other instanceof Movement))
			return false;

		var movement = (Movement) other;

		return Objects.equals(this.emitter, movement.emitter)
			&& Objects.equals(this.timestamp, movement.timestamp)
			&& this.abscissa == movement.abscissa
			&& this.ordinate == movement.ordinate
			&& this.floor == movement.floor;
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(
			this.emitter,
			this.timestamp,
			this.abscissa,
			this.ordinate,
			this.floor);
	}
}
