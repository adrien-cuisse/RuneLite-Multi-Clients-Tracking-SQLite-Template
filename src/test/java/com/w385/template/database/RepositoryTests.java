package com.w385.template.database;

import com.w385.demo.movements.Movement;
import com.w385.demo.movements.MovementRepository;
import org.junit.Test;
import java.time.Instant;
import java.util.List;
import static com.w385.template.database.SqlOperator.EQUALS;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class RepositoryTests extends DatabaseTestSuite
{
	private final MovementRepository repository = new MovementRepository(this.database);

	private static final Instant NOW = Instant.now();

	@Test
	public void idIsSetAfterInsertion()
	{
		// given: an entity that has not been persisted yet
		var movement = new Movement("any", NOW, 0, 0, 0);

		// when: inserting it into the database
		this.repository.insert(movement);

		// then: its ID should now be assigned
		assertThat(movement.getIdentity(), is(1));
	}

	@Test
	public void idIsFetched()
	{
		// given: an entity stored in database
		var movement = new Movement("any", NOW, 0, 0, 0);
		this.repository.insert(movement);

		// when: fetching it
		var persisted = this.repository.fetch().get(0);

		// then: its ID should now be assigned
		assertThat(persisted.getIdentity(), is(1));
	}

	@Test
	public void idIsIgnoredForInsertion()
	{
		// given: an entity stored in database
		var movement = new Movement("any", NOW, 0, 0, 0);
		this.repository.insert(movement);

		// when: persisting it again
		this.repository.insert(movement);

		// then: newly persisted entity should have its own ID
		List<Movement> moves = this.repository.fetch();
		assertThat(moves.get(0).getIdentity(), is(not(moves.get(1).getIdentity())));
	}

	@Test
	public void fetchesWholeTable()
	{
		// given: some entities stored in database
		this.repository.insert(new Movement("first", NOW, 0, 0, 0));
		this.repository.insert(new Movement("second", NOW, 0, 0, 0));

		// when: fetching without filter
		List<Movement> movements = this.repository.fetch();

		// then: it should return the whole table
		assertThat(movements.size(), is(2));
	}

	@Test
	public void fetchesRelevantRows()
	{
		// given: some entities stored in database
		this.repository.insert(new Movement("first", NOW, 0, 0, 0));
		this.repository.insert(new Movement("second", NOW, 0, 0, 0));

		// when: fetching from a condition
		var condition = new WhereCondition("emitter", EQUALS, "first");
		List<Movement> matches = this.repository.fetch(condition);

		// then: it should return only the matching rows
		assertThat(matches.size(), is(1));
		assertThat(matches.get(0).emitter(), is("first"));
	}

	@Test
	public void updatesFromId()
	{
		// given: an entity stored in database
		var movement = new Movement("as before", NOW, 0, 0, 0);
		this.repository.insert(movement);

		// when: modifying it and saving changes
		var updated = new Movement(movement.getIdentity(), "updated", NOW, 0, 0, 0);
		this.repository.update(updated);

		// then: the existing row should have been updated
		Movement persisted = this.repository.fetch().get(0);
		assertThat(persisted.emitter(), is("updated"));
	}

	@Test
	public void untargetedIdsAreNotUpdated()
	{
		// given: some entities stored in database
		var target = new Movement("as before", NOW, 0, 0, 0);
		this.repository.insert(target);
		var other = new Movement("unchanged", NOW, 0, 0, 0);
		this.repository.insert(other);

		// when: modifying only one and saving changes
		var updated = new Movement(target.getIdentity(), "updated", NOW, 0, 0, 0);
		this.repository.update(updated);

		// then: non-matching rows shouldn't have been updated
		Movement untargeted = this.repository.fetch().get(1);
		assertThat(untargeted.emitter(), is("unchanged"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void updatingNonPersistedMovementThrows()
	{
		// given: an entity that has not been persisted yet
		var movement = new Movement("in memory", NOW, 0, 0, 0);

		// when: trying to update it
		this.repository.update(movement);

		// then: it should throw to warn about the mistake
	}

	@Test
	public void deletesFromId()
	{
		// given: an entity stored in database
		var target = new Movement("deletion target", NOW, 0, 0, 0);
		this.repository.insert(target);

		// when: deleting it
		this.repository.delete(target);

		// then: it shouldn't be in database anymore
		List<Movement> movements = this.repository.fetch();
		assertThat(movements.size(), is(0));
	}

	@Test
	public void untargetedIdsAreNotDeleted()
	{
		// given: 2 entities stored in database
		var target = new Movement("deletion target", NOW, 0, 0, 0);
		this.repository.insert(target);
		var other = new Movement("to keep", NOW, 0, 0, 0);
		this.repository.insert(other);

		// when: deleting only one of then
		this.repository.delete(target);

		// then: there should still be 1 row left...
		List<Movement> movements = this.repository.fetch();
		assertThat(movements.size(), is(1));
		// ...and that row should be the untargeted one
		assertThat(movements.get(0).emitter(), is("to keep"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void deletingNonPersistedMovementThrows()
	{
		// given: an entity that has not been persisted yet
		var movement = new Movement("in memory", NOW, 0, 0, 0);

		// when: trying to delete it
		this.repository.delete(movement);

		// then: it should throw to warn about the mistake
	}
}
