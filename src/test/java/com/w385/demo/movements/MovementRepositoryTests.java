package com.w385.demo.movements;

import com.w385.template.database.DatabaseTestSuite;
import com.w385.template.database.SqlResourceReader;
import com.w385.template.database.WhereCondition;
import org.junit.Before;
import org.junit.Test;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import static com.w385.template.database.SqlOperator.EQUALS;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public final class MovementRepositoryTests extends DatabaseTestSuite
{
	private final MovementRepository repository = new MovementRepository(this.database);

	private static final Instant NOW = Instant.now();

	@Before
	public void createTable() throws IOException
	{
		String tableCreationQuery = new SqlResourceReader()
			.read("/demo/sql/create-database.sql")
			.get(0);
		this.database.execute(tableCreationQuery);
	}

	@Test
	public void emitterMapping()
	{
		// given: a movement stored by any client
		String emitter = "any client";
		var movement = new Movement(emitter, NOW, 0, 0, 0);

		// when: reading it back
		this.repository.insert(movement);
		Movement persisted = this.repository.fetch().get(0);

		// then: the emitter should be as set
		assertThat(persisted.emitter(), is(emitter));
	}

	@Test
	public void timestampMapping()
	{
		// given: a movement stored with a given timestamp
		Instant timestamp = Instant.now();
		var movement = new Movement("", timestamp, 0, 0, 0);

		// when: reading it back
		this.repository.insert(movement);
		Movement persisted = this.repository.fetch().get(0);

		// then: the timestamp should be as set
		assertThat(persisted.timestamp(), is(timestamp));
	}

	@Test
	public void abscissaMapping()
	{
		// given: a movement stored with a given abscissa
		int abscissa = 42;
		var movement = new Movement("", NOW, abscissa, 0, 0);

		// when: reading it back
		this.repository.insert(movement);
		Movement persisted = this.repository.fetch().get(0);

		// then: the abscissa should be as set
		assertThat(persisted.abscissa(), is(abscissa));
	}

	@Test
	public void ordinateMapping()
	{
		// given: a movement stored with a given ordinate
		int ordinate = 42;
		var movement = new Movement("", NOW, 0, ordinate, 0);

		// when: reading it back
		this.repository.insert(movement);
		Movement persisted = this.repository.fetch().get(0);

		// then: the ordinate should be as set
		assertThat(persisted.ordinate(), is(ordinate));
	}

	@Test
	public void floorMapping()
	{
		// given: a movement stored with a given floor
		int floor = 42;
		var movement = new Movement("", NOW, 0, 0, floor);

		// when: reading it back
		this.repository.insert(movement);
		Movement persisted = this.repository.fetch().get(0);

		// then: the floor should be as set
		assertThat(persisted.floor(), is(floor));
	}

	@Test
	public void idIsSetAfterInsertion()
	{
		// given: a movement that has not been persisted yet
		var movement = new Movement("any", NOW, 0, 0, 0);

		// when: inserting it into the database
		this.repository.insert(movement);

		// then: its ID should now be assigned
		assertThat(movement.getId(), is(1));
	}

	@Test
	public void idIsFetched()
	{
		// given: a movement stored in database
		var movement = new Movement("any", NOW, 0, 0, 0);
		this.repository.insert(movement);

		// when: reading it from the database
		var persisted = this.repository.fetch().get(0);

		// then: its ID should now be assigned
		assertThat(persisted.getId(), is(1));
	}

	@Test
	public void idIsIgnoredForInsertion()
	{
		// given: a movement stored in database
		var movement = new Movement("any", NOW, 0, 0, 0);
		this.repository.insert(movement);

		// when: persisting it again
		this.repository.insert(movement);

		// then: newly persisted movement should have its own ID
		List<Movement> moves = this.repository.fetch();
		assertThat(moves.get(0).getId(), is(not(moves.get(1).getId())));
	}

	@Test
	public void fetchesWholeTable()
	{
		// given: some movements stored in database
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
		// given: some movements stored in database
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
		// given: a movement stored in database
		var movement = new Movement("as before", NOW, 0, 0, 0);
		this.repository.insert(movement);

		// when: modifying it and saving changes
		var updated = new Movement(movement.getId(), "updated", NOW, 0, 0, 0);
		this.repository.update(updated);

		// then: the existing row should have been updated
		Movement persisted = this.repository.fetch().get(0);
		assertThat(persisted.emitter(), is("updated"));
	}

	@Test
	public void untargetedIdsAreNotUpdated()
	{
		// given: some movements stored in database
		var target = new Movement("as before", NOW, 0, 0, 0);
		this.repository.insert(target);
		var other = new Movement("unchanged", NOW, 0, 0, 0);
		this.repository.insert(other);

		// when: modifying only one and saving changes
		var updated = new Movement(target.getId(), "updated", NOW, 0, 0, 0);
		this.repository.update(updated);

		// then: the existing row should have been updated
		Movement untargeted = this.repository.fetch().get(1);
		assertThat(untargeted.emitter(), is("unchanged"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void updatingNonPersistedMovementThrows()
	{
		// given: a movement that has not been persisted yet
		var movement = new Movement("in memory", NOW, 0, 0, 0);

		// when: trying to update it
		this.repository.update(movement);

		// then: it should throw to warn about the mistake
	}

	@Test
	public void deletesFromId()
	{
		// given: a movement stored in database
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
		// given: some movements stored in database
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
		// given: a movement that has not been persisted yet
		var movement = new Movement("in memory", NOW, 0, 0, 0);

		// when: trying to delete it
		this.repository.delete(movement);

		// then: it should throw to warn about the mistake
	}
}
