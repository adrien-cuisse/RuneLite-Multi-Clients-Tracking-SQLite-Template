package com.w385.template.database;

import com.w385.template.domain.Entity;
import org.junit.Before;
import org.junit.Test;
import java.util.List;
import java.util.Map;
import static com.w385.template.database.SqlOperator.EQUALS;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public final class RepositoryTests extends DatabaseTestSuite
{
	@Before
	public void createTable()
	{
		String tableCreationQuery = "CREATE TABLE \"foos\" ("
			+ "    \"primary-key\" INTEGER PRIMARY KEY AUTOINCREMENT,"
			+ "    \"bar\" TEXT NOT NULL"
			+ ")";
		this.database.execute(tableCreationQuery);
	}

	@Test
	public void idIsSetAfterInsertion()
	{
		// given: an entity that has not been persisted yet
		var entity = new Foo("");

		// when: inserting it into the database
		this.repository.insert(entity);

		// then: its ID should now be assigned
		assertThat(entity.getIdentity(), is(1));
	}

	@Test
	public void idIsFetched()
	{
		// given: an entity stored in database
		var entity = new Foo("stored in database");
		this.repository.insert(entity);

		// when: fetching it
		var persisted = this.repository.fetch().get(0);

		// then: its ID should now be assigned
		assertThat(persisted.getIdentity(), is(1));
	}

	@Test
	public void idIsIgnoredForInsertion()
	{
		// given: an entity stored in database
		var entity = new Foo("to persist again");
		this.repository.insert(entity);

		// when: persisting it again
		this.repository.insert(entity);

		// then: newly persisted entity should have its own ID
		List<Foo> foos = this.repository.fetch();
		assertThat(foos.get(0).getIdentity(), is(not(foos.get(1).getIdentity())));
	}

	@Test
	public void fetchesWholeTable()
	{
		// given: some entities stored in database
		this.repository.insert(new Foo("first"));
		this.repository.insert(new Foo("second"));

		// when: fetching without filter
		List<Foo> entities = this.repository.fetch();

		// then: it should return the whole table
		assertThat(entities.size(), is(2));
	}

	@Test
	public void fetchesRelevantRows()
	{
		// given: some entities stored in database
		this.repository.insert(new Foo("first"));
		this.repository.insert(new Foo("second"));

		// when: fetching from a condition
		var condition = new WhereCondition("bar", EQUALS, "first");
		List<Foo> matches = this.repository.fetch(condition);

		// then: it should return only the matching rows
		assertThat(matches.size(), is(1));
		assertThat(matches.get(0).bar, is("first"));
	}

	@Test
	public void updatesFromId()
	{
		// given: an entity stored in database
		var entity = new Foo("as before");
		this.repository.insert(entity);

		// when: modifying it and saving changes
		var updated = new Foo(entity.getIdentity(), "updated");
		this.repository.update(updated);

		// then: the existing row should have been updated
		Foo persisted = this.repository.fetch().get(0);
		assertThat(persisted.bar, is("updated"));
	}

	@Test
	public void untargetedIdsAreNotUpdated()
	{
		// given: some entities stored in database
		var target = new Foo("as before");
		this.repository.insert(target);
		var other = new Foo("unchanged");
		this.repository.insert(other);

		// when: modifying only one and saving changes
		var updated = new Foo(target.getIdentity(), "updated");
		this.repository.update(updated);

		// then: non-matching rows shouldn't have been updated
		Foo untargeted = this.repository.fetch().get(1);
		assertThat(untargeted.bar, is("unchanged"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void updatingNonPersistedMovementThrows()
	{
		// given: an entity that has not been persisted yet
		var entity = new Foo("in memory");

		// when: trying to update it
		this.repository.update(entity);

		// then: it should throw to warn about the mistake
	}

	@Test
	public void deletesFromId()
	{
		// given: an entity stored in database
		var target = new Foo("deletion target");
		this.repository.insert(target);

		// when: deleting it
		this.repository.delete(target);

		// then: it shouldn't be in database anymore
		List<Foo> entities = this.repository.fetch();
		assertThat(entities.size(), is(0));
	}

	@Test
	public void untargetedIdsAreNotDeleted()
	{
		// given: 2 entities stored in database
		var target = new Foo("deletion target");
		this.repository.insert(target);
		var other = new Foo("to keep");
		this.repository.insert(other);

		// when: deleting only one of them
		this.repository.delete(target);

		// then: there should still be 1 row left...
		List<Foo> entities = this.repository.fetch();
		assertThat(entities.size(), is(1));
		// ...and that row should be the untargeted one
		assertThat(entities.get(0).bar, is("to keep"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void deletingNonPersistedMovementThrows()
	{
		// given: an entity that has not been persisted yet
		var entity = new Foo("in memory");

		// when: trying to delete it
		this.repository.delete(entity);

		// then: it should throw to warn about the mistake
	}

	private static class Foo extends Entity<Integer>
	{
		final String bar;

		Foo(String bar)
		{
			this(null, bar);
		}

		Foo(Integer identity, String bar)
		{
			this.identity = identity;
			this.bar = bar;
		}
	}

	private final Repository<Foo, Integer> repository = new Repository<>(this.database)
	{
		@Override
		protected String table()
		{
			return "foos";
		}

		@Override
		protected String primaryKey()
		{
			return "primary-key";
		}

		@Override
		protected Map<String, Object> map(Foo foo)
		{
			return Map.of("bar", foo.bar);
		}

		@Override
		protected Foo unmap(Map<String, String> map)
		{
			String id = map.get(this.primaryKey());

			return new Foo(
				id == null ? null : this.parseIdentity(id),
				map.get("bar"));
		}

		@Override
		protected Integer parseIdentity(String identity)
		{
			return Integer.parseInt(identity);
		}
	};
}
