package com.w385.template.database;

import com.w385.template.domain.Entity;
import java.util.List;
import java.util.Map;
import static com.w385.template.database.SqlOperator.EQUALS;
import static java.util.stream.Collectors.toList;

public abstract class Repository<T extends Entity<U>, U> extends GenericRepository
{
	protected Repository(SQLiteDatabase database)
	{
		super(database);
	}

	public final void insert(T entity)
	{
		String id = super.insertMap(this.map(entity));
		entity.setIdentity(this.parseIdentity(id));
	}

	public final List<T> fetch(WhereCondition ...conditions)
	{
		return super.fetchMap(conditions)
			.stream()
			.map(this::unmap)
			.collect(toList());
	}

	public final void update(T entity)
	{
		U identity = entity.getIdentity();
		if (identity == null)
			throw new IllegalArgumentException("entity hasn't been persisted");

		var where = new WhereCondition(this.primaryKey(), EQUALS, identity);
		super.updateMap(this.map(entity), where);
	}

	public final void delete(T entity)
	{
		U identity = entity.getIdentity();
		if (identity == null)
			throw new IllegalArgumentException("entity hasn't been persisted");

		var where = new WhereCondition(this.primaryKey(), EQUALS, identity);
		super.delete(where);
	}

	protected abstract Map<String, Object> map(T entity);

	protected abstract T unmap(Map<String, String> map);

	protected abstract U parseIdentity(String identity);
}
