package com.w385.template.database;

import com.w385.template.domain.Entity;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static com.w385.template.database.SqlOperator.EQUALS;
import static java.util.stream.Collectors.toList;

/**
 * Provides database operations for domain/business models.
 *
 * @param <T> the bound entity-class
 * @param <U> the identity-type of the bound entity-class
 */
public abstract class Repository<T extends Entity<U>, U> extends GenericRepository
{
	protected Repository(SQLiteDatabase database)
	{
		super(database);
	}

	/**
	 * Inserts a new entity in database and assigns its new identity
	 * Any previously assigned identity is ignored.
	 *
	 * @param entity - the entity to insert in database
	 *
	 * @throws IllegalArgumentException if the entity already has an identity
	 * @throws UncheckedSQLException if a constraint violation occurs, as
	 *	listed by {@link #map}
	 */
	public final void insert(T entity)
	{
		U identity = entity.getIdentity();
		if (identity != null)
			throw new IllegalArgumentException("entity has already been persisted");

		String id = super.insertMap(this.map(entity));
		entity.setIdentity(this.parseIdentity(id));
	}

	/**
	 * Fetches entities from filters.
	 * If no filters are provided, the whole table is returned.
	 *
	 * @param conditions the filters to match against entities
	 *
	 * @return the matching entities
	 */
	public final List<T> fetch(WhereCondition ...conditions)
	{
		return super.fetchMap(conditions)
			.stream()
			.map(this::unmap)
			.collect(toList());
	}

	/**
	 * Updates an entity in database.
	 * The entity should have been modified after it has been fetched.
	 * If the identity of the entity is not found in database, this is a no-op.
	 *
	 * @param entity the entity to update in database
	 *
	 * @throws IllegalArgumentException if the entity has no identity
	 * @throws UncheckedSQLException if a constraint violation occurs, as
	 *	listed by {@link #map}
	 */
	public final void update(T entity)
	{
		U identity = entity.getIdentity();
		if (identity == null)
			throw new IllegalArgumentException("entity hasn't been persisted");

		var where = new WhereCondition(this.primaryKey(), EQUALS, identity);
		Map<String, Object> map = this.withoutPrimaryKey(this.map(entity));
		super.updateMap(map, where);
	}

	/**
	 * Deletes an entity in database.
	 * If the identity of the entity is not found in database, this is a no-op.
	 *
	 * @param entity the entity to delete from the database
	 *
	 * @throws IllegalArgumentException if the entity has no identity
	 */
	public final void delete(T entity)
	{
		U identity = entity.getIdentity();
		if (identity == null)
			throw new IllegalArgumentException("entity hasn't been persisted");

		var where = new WhereCondition(this.primaryKey(), EQUALS, identity);
		super.delete(where);
	}

	/**
	 * Creates a Map representing the given entity, where keys are columns' name
	 *  in the SQL table, and values the corresponding values in the entity.
	 * Values must conform with the SQL schema:
	 * 	- non-NULLABLE columns must be set,
	 * 	- NULLABLE columns may be omitted or set to null,
	 * 	- columns with DEFAULT values may be omitted,
	 * 	- AUTOINCREMENT integer primary keys should not be set.
	 *
	 * @param entity the entity turn into a Map
	 *
	 * @return a generic representation of the entity
	 */
	protected abstract Map<String, Object> map(T entity);

	/**
	 * Creates a new entity from its generic Map representation
	 * The Map is guaranteed to conform with the bound SQL schema:
	 * - non-NULLABLE columns have a key, and value is non-null,
	 * - NULLABLE columns have a key, and value may be null.
	 *
	 * @param map - the map representing the entity, where keys are columns' name
	 * 	in the SQL table, and values the corresponding values in the entity stored
	 *	in Strings.
	 *
	 * @return the created entity
	 */
	protected abstract T unmap(Map<String, String> map);

	/**
	 * Parses an identity stored in a String.
	 *
	 * @param identity the identity to parse
	 *
	 * @return the parsed identity
	 */
	protected abstract U parseIdentity(String identity);

	/**
	 * Removes the primary key from a Map representation
	 *
	 * @param map the map to remove primary key from
	 *
	 * @return the provided map without primary key
	 */
	private Map<String, Object> withoutPrimaryKey(Map<String, Object> map)
	{
		if (!map.containsKey(primaryKey()))
			return map;

		var modifiable = new HashMap<>(map);
		modifiable.remove(this.primaryKey());
		return Collections.unmodifiableMap(modifiable);
	}
}
