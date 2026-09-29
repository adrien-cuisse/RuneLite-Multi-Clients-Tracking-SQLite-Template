package com.w385.demo.movements;

import com.w385.template.database.GenericRepository;
import com.w385.template.database.SQLiteDatabase;
import com.w385.template.database.WhereCondition;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import static com.w385.template.database.SqlOperator.EQUALS;
import static java.util.stream.Collectors.toList;

public final class MovementRepository extends GenericRepository
{
	public MovementRepository(SQLiteDatabase database)
	{
		super(database);
	}

	public void insert(Movement movement)
	{
		String id = super.insert(this.map(movement));
		movement.setId(Integer.parseInt(id));
	}

	public List<Movement> fetch()
	{
		return super.fetch()
			.stream()
			.map(this::unmap)
			.collect(toList());
	}

	public List<Movement> fetch(WhereCondition condition, WhereCondition ...conditions)
	{
		WhereCondition[] copy = Arrays.copyOf(conditions, conditions.length + 1);
		copy[copy.length - 1] = condition;

		return super.fetch(copy)
			.stream()
			.map(this::unmap)
			.collect(toList());
	}

	public void update(Movement movement)
	{
		if (movement.getId() == null)
			throw new IllegalArgumentException("movement has no ID");

		var where = new WhereCondition(this.primaryKey(), EQUALS, movement.getId());
		super.update(this.map(movement), where);
	}

	public void delete(Movement movement)
	{
		if (movement.getId() == null)
			throw new IllegalArgumentException("movement has no ID");

		var where = new WhereCondition(this.primaryKey(), EQUALS, movement.getId());
		super.delete(where);
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
}
