package com.w385.demo.movements;

import java.time.Instant;

public class Movement
{
	private final String emitter;

	private final Instant timestamp;

	private final int abscissa;

	private final int ordinate;

	private final int floor;

	public Movement(String emitter, Instant timestamp, int abscissa, int ordinate, int floor)
	{
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
}
