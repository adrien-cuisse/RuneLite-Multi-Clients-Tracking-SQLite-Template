package com.w385.demo.movements;

import com.w385.template.database.GenericRepository;
import com.w385.template.database.SQLiteDatabase;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

public final class MovementRepository extends GenericRepository
{
	public MovementRepository(SQLiteDatabase database)
	{
		super(database);
	}

	public void insert(Movement movement)
	{
		this.insert(this.map(movement));
	}

	public List<Movement> fetch()
	{
		return super.fetch()
			.stream()
			.map(this::unmap)
			.collect(toList());
	}

	@Override
	protected String table()
	{
		return "movements";
	}

	private Map<String, Object> map(Movement movement)
	{
		return Map.of(
			"emitter", movement.emitter(),
			"timestamp", movement.timestamp(),
			"abscissa", movement.abscissa(),
			"ordinate", movement.ordinate(),
			"floor", movement.floor());
	}

	private Movement unmap(Map<String, String> map)
	{
		long timestamp = Long.parseLong(map.get("timestamp"));
		long epochMillis = timestamp / 1_000_000;
		long epochNanos = timestamp % 1_000_000;

		return new Movement(
			map.get("emitter"),
			Instant.ofEpochMilli(epochMillis).plusNanos(epochNanos),
			Integer.parseInt(map.get("abscissa")),
			Integer.parseInt(map.get("ordinate")),
			Integer.parseInt(map.get("floor")));
	}
}
