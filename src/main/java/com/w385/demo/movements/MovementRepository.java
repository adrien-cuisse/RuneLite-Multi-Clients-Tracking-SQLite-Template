package com.w385.demo.movements;

import com.w385.template.database.Repository;
import com.w385.template.database.SQLiteDatabase;
import java.time.Instant;
import java.util.Map;

public final class MovementRepository extends Repository<Movement, Integer>
{
	public MovementRepository(SQLiteDatabase database)
	{
		super(database);
	}

	@Override
	protected String table()
	{
		return "movements";
	}

	@Override
	protected Map<String, Object> map(Movement movement)
	{
		return Map.of(
			"emitter", movement.emitter(),
			"timestamp", movement.timestamp(),
			"abscissa", movement.abscissa(),
			"ordinate", movement.ordinate(),
			"floor", movement.floor());
	}

	@Override
	protected Movement unmap(Map<String, String> map)
	{
		String id = map.get(this.primaryKey());

		long timestamp = Long.parseLong(map.get("timestamp"));
		long epochMillis = timestamp / 1_000_000;
		long epochNanos = timestamp % 1_000_000;

		return new Movement(
			id == null ? null : Integer.parseInt(id),
			map.get("emitter"),
			Instant.ofEpochMilli(epochMillis).plusNanos(epochNanos),
			Integer.parseInt(map.get("abscissa")),
			Integer.parseInt(map.get("ordinate")),
			Integer.parseInt(map.get("floor")));
	}

	@Override
	protected Integer parseIdentity(String identity)
	{
		return Integer.parseInt(identity);
	}
}
